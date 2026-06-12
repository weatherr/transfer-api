package bg.fibank.assignment.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StandingOrderRequest(
    @NotBlank(message = "Source IBAN is required") 
    String sourceIban,
    
    @NotBlank(message = "Destination IBAN is required") 
    String destinationIban,
    
    @NotNull(message = "Amount is required") 
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero") 
    BigDecimal amount,
    
    @NotBlank(message = "Cron schedule is required") 
    String cronSchedule
) {}