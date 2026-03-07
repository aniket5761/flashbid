package com.example.flashbid.auction.service;

import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.product.repo.ProductRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {

    private final ProductRepo productRepo;
    private final AuctionWinnerService auctionWinnerService;

    @Scheduled(cron = "0 * * * * *")
    public void updateAuctions() {
        openScheduledAuctions();
        closeExpiredAuctions();
    }

    @Transactional
    protected void openScheduledAuctions() {
        List<Product> toOpen = productRepo.findByProductStatusAndStartTimeLessThanEqual(ProductStatus.SCHEDULED, LocalDateTime.now());
        if (!toOpen.isEmpty()) {
            toOpen.forEach(p -> {
                p.setProductStatus(ProductStatus.OPEN);
                log.info("Auction opened for product ID: {}", p.getId());
            });
            productRepo.saveAll(toOpen);
        }
    }

    @Transactional
    protected void closeExpiredAuctions() {
        List<Product> toClose = productRepo.findByProductStatusAndEndTimeLessThanEqual(ProductStatus.OPEN, LocalDateTime.now());
        if (toClose.isEmpty()) return;

        toClose.forEach(p -> {
            try {
                p.setProductStatus(ProductStatus.CLOSED);
                // Try to determine winner
                try {
                    auctionWinnerService.createWinner(p);
                    log.info("Winner determined and auction closed for product ID: {}", p.getId());
                } catch (Exception e) {
                    log.warn("No winner found for product ID: {} - {}", p.getId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("Failed to close auction for product ID: {}", p.getId(), e);
            }
        });

        productRepo.saveAll(toClose);
    }
}
