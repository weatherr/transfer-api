package bg.fibank.assignment.transfer.dto;

import java.util.UUID;

public record TransferResponse(
    UUID transferId,
    String status,
    String message
) {}