package bg.fibank.assignment.transfer.controller;

import bg.fibank.assignment.transfer.dto.TransferRequest;
import bg.fibank.assignment.transfer.dto.TransferResponse;
import bg.fibank.assignment.transfer.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    @Test
    void executeTransfer_DuplicateIdempotencyKey_ReturnsCachedResponse() {
        TransferRequest req = new TransferRequest("SRC", "DEST", new BigDecimal("100"), "USD");
        String key = "idempotency-key-123";
        UUID correlationId = UUID.randomUUID();

        when(transferService.executeTransfer(any())).thenReturn(correlationId);

        ResponseEntity<TransferResponse> response1 = transferController.executeTransfer(key, req);
        
        ResponseEntity<TransferResponse> response2 = transferController.executeTransfer(key, req);

        verify(transferService, times(1)).executeTransfer(any());
        assertSame(response1, response2);
        assertEquals(correlationId, response2.getBody().transferId());
    }
}