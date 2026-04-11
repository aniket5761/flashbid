package com.example.flashbid.auction.service;

import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.common.redis.AuctionLiveUpdateService;
import com.example.flashbid.product.entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {

    private final AuctionRepo auctionRepo;
    private final AuctionLiveUpdateService auctionLiveUpdateService;
    private final AuctionStatusService auctionStatusService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmAuctionIndexes() {
        bootstrapStatus(ProductStatus.SCHEDULED);
        bootstrapStatus(ProductStatus.OPEN);
    }

    @Scheduled(fixedRate = 5000)
    public void updateAuctions() {
        try {
            auctionStatusService.openScheduledAuctions();
        } catch (Exception exception) {
            log.error("Failed to open scheduled auctions from Redis state", exception);
        }

        try {
            auctionStatusService.closeExpiredAuctions();
        } catch (Exception exception) {
            log.error("Failed to close expired auctions from Redis state", exception);
        }
    }

    private void bootstrapStatus(ProductStatus status) {
        auctionRepo.findByStatus(status)
                .forEach(auction -> auctionLiveUpdateService.refreshAndPublish(auction.getProduct().getId(), "BOOTSTRAP"));
    }
}
