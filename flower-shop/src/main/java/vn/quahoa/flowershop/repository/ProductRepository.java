package vn.quahoa.flowershop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.quahoa.flowershop.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_Id(Long categoryId);
}


