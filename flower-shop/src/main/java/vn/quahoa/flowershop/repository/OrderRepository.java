package vn.quahoa.flowershop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.quahoa.flowershop.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findByIdAndUser_Id(Long orderId, Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusInAndCreatedAtBetween(List<vn.quahoa.flowershop.model.OrderStatus> statuses, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
