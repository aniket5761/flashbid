package com.example.flashbid.bid.repo;

import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidRepo extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByProductOrderByAmountDesc(Product product);
    Page<Bid> findByProductIdOrderByAmountDesc(Long productId, Pageable pageable);
}
