package _2280600764_NguyenTruongGiang.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import _2280600764_NguyenTruongGiang.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_IdOrderByDisplayOrderAsc(Long productId);
}
