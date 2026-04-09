package com.example.flashbid.auction.service;

import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.common.redis.AuctionLiveUpdateService;
import com.example.flashbid.common.redis.AuctionRedisCacheService;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {

    private final AuctionRepo auctionRepo;
    private final AuctionManagementService auctionManagementService;
    private final AuctionRedisCacheService auctionRedisCacheService;
    private final AuctionLiveUpdateService auctionLiveUpdateService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmAuctionIndexes() {
        bootstrapStatus(ProductStatus.SCHEDULED);
        bootstrapStatus(ProductStatus.OPEN);
    }

    @Scheduled(fixedRate = 5000)
    public void updateAuctions() {
        try {
            openScheduledAuctions();
        } catch (Exception exception) {
            log.error("Failed to open scheduled auctions from Redis state", exception);
        }

        try {
            closeExpiredAuctions();
        } catch (Exception exception) {
            log.error("Failed to close expired auctions from Redis state", exception);
        }
    }

    @Transactional
    protected void openScheduledAuctions() {
        List<Long> dueAuctionIds = auctionRedisCacheService.getDueScheduledAuctionIds(LocalDateTime.now());
        if (dueAuctionIds.isEmpty()) return;

        dueAuctionIds.forEach(productId -> auctionRepo.findByProductIdForUpdate(productId).ifPresent(auction -> {
            auctionManagementService.syncAuctionStatus(auction);
            Product product = auction.getProduct();
            log.info("Auction opened for product ID: {}", product.getId());
        }));
    }

    @Transactional
    protected void closeExpiredAuctions() {
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

    private void bootstrapStatus(ProductStatus status) {
        auctionRepo.findByStatus(status)
                .forEach(auction -> auctionLiveUpdateService.refreshAndPublish(auction.getProduct().getId(), "BOOTSTRAP"));
    }
}
