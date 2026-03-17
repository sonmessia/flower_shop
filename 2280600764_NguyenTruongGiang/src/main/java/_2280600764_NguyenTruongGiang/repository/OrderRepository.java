package _2280600764_NguyenTruongGiang.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import _2280600764_NguyenTruongGiang.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findByIdAndUser_Id(Long orderId, Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findAllByOrderByCreatedAtDesc();
}
