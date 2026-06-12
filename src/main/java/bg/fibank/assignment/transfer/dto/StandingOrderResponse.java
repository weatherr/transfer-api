package bg.fibank.assignment.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StandingOrderResponse(
    UUID id,
    String sourceIban,
    String destinationIban,
    BigDecimal amount,
    String cronSchedule,
    String status,
    LocalDateTime createdAt
) {}