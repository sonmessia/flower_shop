package _2280600764_NguyenTruongGiang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _2280600764_NguyenTruongGiang.model.BlogImage;

@Repository
public interface BlogImageRepository extends JpaRepository<BlogImage, Long> {
}
