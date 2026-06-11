package bg.fibank.assignment.transfer.repository;

import bg.fibank.assignment.transfer.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>, JpaSpecificationExecutor<LedgerEntry> {

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.accountIban = :iban AND l.transactionType = 'DEBIT' AND l.createdAt >= :startOfDay")
    BigDecimal sumOutgoingTransfersFromDate(@Param("iban") String iban, @Param("startOfDay") LocalDateTime startOfDay);
}