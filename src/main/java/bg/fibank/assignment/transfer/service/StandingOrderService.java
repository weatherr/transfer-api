package bg.fibank.assignment.transfer.service;

import bg.fibank.assignment.transfer.domain.Account;
import bg.fibank.assignment.transfer.domain.StandingOrder;
import bg.fibank.assignment.transfer.dto.StandingOrderRequest;
import bg.fibank.assignment.transfer.dto.TransferRequest;
import bg.fibank.assignment.transfer.repository.AccountRepository;
import bg.fibank.assignment.transfer.repository.StandingOrderRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StandingOrderService {

    private static final Logger log = LogManager.getLogger(StandingOrderService.class);

    private final StandingOrderRepository standingOrderRepository;
    private final AccountRepository accountRepository;
    private final TransferService transferService;

    @Transactional
    public UUID createStandingOrder(StandingOrderRequest request) {
        if (!CronExpression.isValidExpression(request.cronSchedule())) {
            throw new IllegalArgumentException("Invalid cron expression provided");
        }

        StandingOrder order = new StandingOrder();
        order.setId(UUID.randomUUID());
        order.setSourceIban(request.sourceIban());
        order.setDestinationIban(request.destinationIban());
        order.setAmount(request.amount());
        order.setCronSchedule(request.cronSchedule());
        order.setStatus(StandingOrder.Status.ACTIVE);
        order.setCreatedAt(LocalDateTime.now());

        standingOrderRepository.save(order);
        log.info("Created active standing order [{}]", order.getId());
        return order.getId();
    }

    public List<StandingOrder> getAllActive() {
        return standingOrderRepository.findAllByStatus(StandingOrder.Status.ACTIVE);
    }

    public StandingOrder getById(UUID id) {
        return standingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Standing order not found"));
    }

    @Transactional
    public void cancelStandingOrder(UUID id) {
        StandingOrder order = getById(id);
        order.setStatus(StandingOrder.Status.CANCELLED);
        standingOrderRepository.save(order);
        log.info("Cancelled standing order [{}]", id);
    }

    // Runs at the top of every minute
    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "processStandingOrdersLock", lockAtLeastFor = "30s", lockAtMostFor = "2m")
    public void processStandingOrders() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<StandingOrder> activeOrders = getAllActive();

        for (StandingOrder order : activeOrders) {
            try {
                CronExpression cron = CronExpression.parse(order.getCronSchedule());
                LocalDateTime nextRun = cron.next(now.minusMinutes(1));

                if (nextRun != null && nextRun.truncatedTo(ChronoUnit.MINUTES).equals(now)) {
                    log.info("Cron match for Standing Order [{}]. Executing...", order.getId());
                    executeOrder(order);
                }
            } catch (Exception e) {
                // Catches the error to prevent the loop from breaking, logging it so it will safely retry next time
                log.error("Failed to execute standing order [{}]. Will retry on next scheduled run. Reason: {}", 
                        order.getId(), e.getMessage());
            }
        }
    }

    private void executeOrder(StandingOrder order) {
        Account sourceAccount = accountRepository.findById(order.getSourceIban())
                .orElseThrow(() -> new IllegalStateException("Source account no longer exists"));

        TransferRequest transferRequest = new TransferRequest(
                order.getSourceIban(),
                order.getDestinationIban(),
                order.getAmount(),
                sourceAccount.getCurrency() // Inherits source currency for the transfer request
        );

        transferService.executeTransfer(transferRequest);
    }
}