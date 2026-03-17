package _2280600764_NguyenTruongGiang.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _2280600764_NguyenTruongGiang.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsByNameIgnoreCase(String name);

	Optional<Category> findByNameIgnoreCase(String name);
}


