package _2280600764_NguyenTruongGiang.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _2280600764_NguyenTruongGiang.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser_IdOrderByCreatedAtAsc(Long userId);

    Optional<CartItem> findByIdAndUser_Id(Long cartItemId, Long userId);

    Optional<CartItem> findByUser_IdAndProduct_Id(Long userId, Long productId);

    void deleteByUser_Id(Long userId);
}
