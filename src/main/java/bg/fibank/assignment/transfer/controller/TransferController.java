package bg.fibank.assignment.transfer.controller;

import bg.fibank.assignment.transfer.dto.TransferRequest;
import bg.fibank.assignment.transfer.dto.TransferResponse;
import bg.fibank.assignment.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    
    private final Map<String, ResponseEntity<TransferResponse>> idempotencyCache = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        if (idempotencyCache.containsKey(idempotencyKey)) {
            return idempotencyCache.get(idempotencyKey);
        }

        UUID correlationId = transferService.executeTransfer(request);

        TransferResponse responseBody = new TransferResponse(
                correlationId, 
                "SUCCESS", 
                "Transfer executed successfully"
        );
        
        ResponseEntity<TransferResponse> response = ResponseEntity.ok(responseBody);

        idempotencyCache.put(idempotencyKey, response);

        return response;
    }
}