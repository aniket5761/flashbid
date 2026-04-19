package com.example.flashbid.product.repo;

import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Product> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<Product> findByUser(User user);

    @EntityGraph(attributePaths = "user")
    Page<Product> findByNameContainingIgnoreCaseAndProductStatus(String name, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);

    List<Product> findByProductStatusAndStartTimeLessThanEqual(ProductStatus status, LocalDateTime time);

    List<Product> findByProductStatusAndEndTimeLessThanEqual(ProductStatus status, LocalDateTime time);
}
