package bg.fibank.assignment.transfer.service;

import bg.fibank.assignment.transfer.domain.Account;
import bg.fibank.assignment.transfer.dto.TransferRequest;
import bg.fibank.assignment.transfer.repository.AccountRepository;
import bg.fibank.assignment.transfer.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private TransferService transferService;

    private Account source;
    private Account dest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transferService, "usdToEurRate", new BigDecimal("0.86"));
        ReflectionTestUtils.setField(transferService, "eurToUsdRate", new BigDecimal("1.16"));

        source = new Account();
        source.setIban("BG_SRC");
        source.setBalance(new BigDecimal("5000.00"));
        source.setCurrency("USD");

        dest = new Account();
        dest.setIban("BG_DEST");
        dest.setBalance(new BigDecimal("1000.00"));
        dest.setCurrency("USD");
    }

    @Test
    void executeTransfer_SameCurrency_Success() {
        TransferRequest req = new TransferRequest("BG_SRC", "BG_DEST", new BigDecimal("1000.00"), "USD");
        when(accountRepository.findById("BG_SRC")).thenReturn(Optional.of(source));
        when(accountRepository.findById("BG_DEST")).thenReturn(Optional.of(dest));
        when(ledgerEntryRepository.sumOutgoingTransfersFromDate(eq("BG_SRC"), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        UUID id = transferService.executeTransfer(req);

        assertNotNull(id);
        assertEquals(new BigDecimal("4000.00"), source.getBalance());
        assertEquals(new BigDecimal("2000.00"), dest.getBalance());
        verify(ledgerEntryRepository, times(2)).save(any());
    }

    @Test
    void executeTransfer_CrossCurrency_AppliesFxRate() {
        dest.setCurrency("EUR");
        TransferRequest req = new TransferRequest("BG_SRC", "BG_DEST", new BigDecimal("1000.00"), "USD");
        
        when(accountRepository.findById("BG_SRC")).thenReturn(Optional.of(source));
        when(accountRepository.findById("BG_DEST")).thenReturn(Optional.of(dest));
        when(ledgerEntryRepository.sumOutgoingTransfersFromDate(eq("BG_SRC"), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        transferService.executeTransfer(req);

        assertEquals(new BigDecimal("4000.00"), source.getBalance());
        assertEquals(new BigDecimal("1860.00"), dest.getBalance());
    }

    @Test
    void executeTransfer_InsufficientFunds_ThrowsException() {
        TransferRequest req = new TransferRequest("BG_SRC", "BG_DEST", new BigDecimal("6000.00"), "USD");
        when(accountRepository.findById("BG_SRC")).thenReturn(Optional.of(source));
        when(accountRepository.findById("BG_DEST")).thenReturn(Optional.of(dest));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transferService.executeTransfer(req));
        assertEquals("Insufficient funds in source account", ex.getMessage());
    }

    @Test
    void executeTransfer_ExceedsDailyLimit_ThrowsException() {
        TransferRequest req = new TransferRequest("BG_SRC", "BG_DEST", new BigDecimal("500.00"), "USD");
        when(accountRepository.findById("BG_SRC")).thenReturn(Optional.of(source));
        when(accountRepository.findById("BG_DEST")).thenReturn(Optional.of(dest));
        
        when(ledgerEntryRepository.sumOutgoingTransfersFromDate(eq("BG_SRC"), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("19600.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transferService.executeTransfer(req));
        assertEquals("Daily transfer limit of 20000 exceeded", ex.getMessage());
    }
}