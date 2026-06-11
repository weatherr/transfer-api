package bg.fibank.assignment.transfer.service;

import bg.fibank.assignment.transfer.domain.Account;
import bg.fibank.assignment.transfer.domain.LedgerEntry;
import bg.fibank.assignment.transfer.dto.TransferRequest;
import bg.fibank.assignment.transfer.repository.AccountRepository;
import bg.fibank.assignment.transfer.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Logger log = LogManager.getLogger(TransferService.class);
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("20000.00");

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Value("${fx.rate.usd-to-eur}")
    private BigDecimal usdToEurRate;

    @Value("${fx.rate.eur-to-usd}")
    private BigDecimal eurToUsdRate;

    @Transactional
    public UUID executeTransfer(TransferRequest request) {
        UUID correlationId = UUID.randomUUID();
        log.info("Initiating transfer [{}] from {} to {} for {} {}", 
                correlationId, request.sourceIban(), request.destinationIban(), request.amount(), request.currency());

        try {
            Account source = accountRepository.findById(request.sourceIban())
                    .orElseThrow(() -> new IllegalArgumentException("Source account does not exist"));
            Account destination = accountRepository.findById(request.destinationIban())
                    .orElseThrow(() -> new IllegalArgumentException("Destination account does not exist"));

            validateTransfer(source, request);

            BigDecimal debitAmount = request.amount();
            BigDecimal creditAmount = calculateCreditAmount(debitAmount, source.getCurrency(), destination.getCurrency());

            // 1. Update Balances (Optimistic Locking protects us here)
            source.setBalance(source.getBalance().subtract(debitAmount));
            destination.setBalance(destination.getBalance().add(creditAmount));

            accountRepository.save(source);
            accountRepository.save(destination);

            // 2. Create Double-Entry Ledger Records
            createLedgerEntry(correlationId, source.getIban(), LedgerEntry.TransactionType.DEBIT, debitAmount, source.getCurrency());
            createLedgerEntry(correlationId, destination.getIban(), LedgerEntry.TransactionType.CREDIT, creditAmount, destination.getCurrency());

            log.info("Transfer [{}] executed successfully", correlationId);
            return correlationId;

        } catch (Exception e) {
            log.error("Transfer [{}] failed: {}", correlationId, e.getMessage());
            throw e;
        }
    }

    private void validateTransfer(Account source, TransferRequest request) {
        if (!source.getCurrency().equalsIgnoreCase(request.currency())) {
            throw new IllegalArgumentException("Transfer currency must match the source account currency");
        }

        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds in source account");
        }

        BigDecimal todayDebits = ledgerEntryRepository.sumOutgoingTransfersFromDate(
                source.getIban(), LocalDate.now().atStartOfDay());
        
        if (todayDebits.add(request.amount()).compareTo(DAILY_LIMIT) > 0) {
            throw new IllegalStateException("Daily transfer limit of 20000 exceeded");
        }
    }

    private BigDecimal calculateCreditAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        if (fromCurrency.equalsIgnoreCase("USD") && toCurrency.equalsIgnoreCase("EUR")) {
            return amount.multiply(usdToEurRate).setScale(2, RoundingMode.HALF_EVEN);
        }
        if (fromCurrency.equalsIgnoreCase("EUR") && toCurrency.equalsIgnoreCase("USD")) {
            return amount.multiply(eurToUsdRate).setScale(2, RoundingMode.HALF_EVEN);
        }
        throw new IllegalArgumentException("Unsupported currency conversion");
    }

    private void createLedgerEntry(UUID correlationId, String iban, LedgerEntry.TransactionType type, BigDecimal amount, String currency) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID());
        entry.setCorrelationId(correlationId);
        entry.setAccountIban(iban);
        entry.setTransactionType(type);
        entry.setAmount(amount);
        entry.setCurrency(currency);
        entry.setCreatedAt(java.time.LocalDateTime.now());
        ledgerEntryRepository.save(entry);
    }
}