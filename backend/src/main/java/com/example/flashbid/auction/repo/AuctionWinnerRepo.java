package com.example.flashbid.auction.repo;

import com.example.flashbid.auction.entity.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionWinnerRepo extends JpaRepository<AuctionWinner, Long> {
    Optional<AuctionWinner> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
