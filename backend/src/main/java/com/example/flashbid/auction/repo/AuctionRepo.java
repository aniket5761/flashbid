package com.example.flashbid.auction.repo;

import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.product.entity.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepo extends JpaRepository<Auction, Long> {

    Optional<Auction> findByProductId(Long productId);

    List<Auction> findByProductIdIn(List<Long> productIds);

    List<Auction> findByStatusAndStartTimeLessThanEqual(ProductStatus status, LocalDateTime time);

    List<Auction> findByStatusAndEndTimeLessThanEqual(ProductStatus status, LocalDateTime time);

    List<Auction> findByStatus(ProductStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.product.id = :productId")
    Optional<Auction> findByProductIdForUpdate(@Param("productId") Long productId);

    void deleteByProductId(Long productId);
}
