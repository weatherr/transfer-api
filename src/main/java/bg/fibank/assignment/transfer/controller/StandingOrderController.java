package bg.fibank.assignment.transfer.controller;

import bg.fibank.assignment.transfer.domain.StandingOrder;
import bg.fibank.assignment.transfer.dto.StandingOrderRequest;
import bg.fibank.assignment.transfer.dto.StandingOrderResponse;
import bg.fibank.assignment.transfer.service.StandingOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/standing-orders")
@RequiredArgsConstructor
public class StandingOrderController {

    private final StandingOrderService standingOrderService;

    @PostMapping
    public ResponseEntity<StandingOrderResponse> createStandingOrder(@Valid @RequestBody StandingOrderRequest request) {
        UUID id = standingOrderService.createStandingOrder(request);
        StandingOrder order = standingOrderService.getById(id);
        
        StandingOrderResponse response = new StandingOrderResponse(
                order.getId(), order.getSourceIban(), order.getDestinationIban(),
                order.getAmount(), order.getCronSchedule(), order.getStatus().name(), order.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StandingOrder>> getActiveStandingOrders() {
        return ResponseEntity.ok(standingOrderService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandingOrder> getStandingOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(standingOrderService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelStandingOrder(@PathVariable UUID id) {
        standingOrderService.cancelStandingOrder(id);
        return ResponseEntity.noContent().build();
    }
}