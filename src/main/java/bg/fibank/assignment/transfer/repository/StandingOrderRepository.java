package bg.fibank.assignment.transfer.repository;

import bg.fibank.assignment.transfer.domain.StandingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandingOrderRepository extends JpaRepository<StandingOrder, UUID> {
    List<StandingOrder> findAllByStatus(StandingOrder.Status status);
}