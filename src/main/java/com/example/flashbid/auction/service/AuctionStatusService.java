package com.example.flashbid.auction.service;

import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.common.redis.AuctionRedisCacheService;
import com.example.flashbid.product.entity.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusService {

    private final AuctionRepo auctionRepo;
    private final AuctionManagementService auctionManagementService;
    private final AuctionRedisCacheService auctionRedisCacheService;

    @Transactional
    public void openScheduledAuctions() {
        List<Long> dueAuctionIds = auctionRedisCacheService.getDueScheduledAuctionIds(LocalDateTime.now());
        if (dueAuctionIds.isEmpty()) return;

        dueAuctionIds.forEach(productId -> auctionRepo.findByProductIdForUpdate(productId).ifPresent(auction -> {
            auctionManagementService.syncAuctionStatus(auction);
            Product product = auction.getProduct();
            log.info("Auction opened for product ID: {}", product.getId());
        }));
    }

    @Transactional
    public void closeExpiredAuctions() {
        List<Long> dueAuctionIds = auctionRedisCacheService.getDueClosingAuctionIds(LocalDateTime.now());
        if (dueAuctionIds.isEmpty()) return;

        dueAuctionIds.forEach(productId -> auctionRepo.findByProductIdForUpdate(productId).ifPresent(auction -> {
            try {
                auctionManagementService.closeAuction(auction);
            } catch (Exception e) {
                log.error("Failed to close auction for product ID: {}", auction.getProduct().getId(), e);
            }
        }));
    }
}
