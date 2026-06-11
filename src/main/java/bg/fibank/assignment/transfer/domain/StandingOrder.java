package bg.fibank.assignment.transfer.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "standing_orders")
public class StandingOrder {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_iban", nullable = false)
    private String sourceIban;

    @Column(name = "destination_iban", nullable = false)
    private String destinationIban;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "cron_schedule", nullable = false)
    private String cronSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE, CANCELLED
    }

    public StandingOrder() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSourceIban() { return sourceIban; }
    public void setSourceIban(String sourceIban) { this.sourceIban = sourceIban; }

    public String getDestinationIban() { return destinationIban; }
    public void setDestinationIban(String destinationIban) { this.destinationIban = destinationIban; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCronSchedule() { return cronSchedule; }
    public void setCronSchedule(String cronSchedule) { this.cronSchedule = cronSchedule; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}