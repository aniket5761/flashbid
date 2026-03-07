package com.example.flashbid.product.repo;

import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> findByUser(User user);

    Page<Product> findByNameContainingIgnoreCaseAndProductStatus(String name, ProductStatus status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);

    List<Product> findByProductStatusAndStartTimeLessThanEqual(ProductStatus status, LocalDateTime time);

    List<Product> findByProductStatusAndEndTimeLessThanEqual(ProductStatus status, LocalDateTime time);
}
