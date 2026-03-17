package _2280600764_NguyenTruongGiang.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _2280600764_NguyenTruongGiang.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

  Optional<Admin> findByUsername(String username);

  boolean existsByUsername(String username);
}
