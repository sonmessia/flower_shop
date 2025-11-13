package vn.quahoa.flowershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.quahoa.flowershop.model.BlogImage;

@Repository
public interface BlogImageRepository extends JpaRepository<BlogImage, Long> {
}
