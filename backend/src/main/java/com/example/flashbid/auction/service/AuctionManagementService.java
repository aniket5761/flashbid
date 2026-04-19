package com.example.flashbid.auction.service;

import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.common.redis.AuctionLiveUpdateService;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionManagementService {

    private final AuctionRepo auctionRepo;
    private final AuctionWinnerService auctionWinnerService;
    private final AuctionLiveUpdateService auctionLiveUpdateService;

    @Transactional
    public Auction forceCloseAuction(Long productId) {
        Auction auction = auctionRepo.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found for product id: " + productId));
        closeAuction(auction);
        return auctionRepo.save(auction);
    }

    @Transactional
    public ProductStatus syncAuctionStatus(Auction auction) {
        if (auction.getStatus() == ProductStatus.CLOSED
                || auction.getProduct().getProductStatus() == ProductStatus.CLOSED) {
            auction.setStatus(ProductStatus.CLOSED);
            auction.getProduct().setProductStatus(ProductStatus.CLOSED);
            return ProductStatus.CLOSED;
        }

        ProductStatus effectiveStatus = resolveStatus(auction, LocalDateTime.now());
        Product product = auction.getProduct();

        if (effectiveStatus == ProductStatus.CLOSED) {
            closeAuction(auction);
            return auction.getStatus();
        }

        if (auction.getStatus() != effectiveStatus) {
            auction.setStatus(effectiveStatus);
            auctionLiveUpdateService.scheduleRefresh(auction.getProduct().getId(), "STATUS_CHANGED");
        }

        if (product.getProductStatus() != effectiveStatus) {
            product.setProductStatus(effectiveStatus);
        }

        return effectiveStatus;
    }

    @Transactional
    public void closeAuction(Auction auction) {
        if (auction.getStatus() == ProductStatus.CLOSED) {
            return;
        }

        Product product = auction.getProduct();
        auction.setStatus(ProductStatus.CLOSED);
        product.setProductStatus(ProductStatus.CLOSED);

        try {
            if (auctionWinnerService.createWinner(product) != null) {
                log.info("Winner determined and auction closed for product ID: {}", product.getId());
            } else {
                log.info("Auction closed without a winner for product ID: {}", product.getId());
            }
        } catch (Exception exception) {
            log.warn("No winner found for product ID: {} - {}", product.getId(), exception.getMessage());
        }

        auctionLiveUpdateService.scheduleRefresh(product.getId(), "STATUS_CHANGED");
    }

    public ProductStatus resolveStatus(Auction auction) {
        return resolveStatus(auction, LocalDateTime.now());
    }

    private ProductStatus resolveStatus(Auction auction, LocalDateTime now) {
        if (auction.getStatus() == ProductStatus.CLOSED
                || auction.getProduct().getProductStatus() == ProductStatus.CLOSED) {
            return ProductStatus.CLOSED;
        }

        if (now.isBefore(auction.getStartTime())) {
            return ProductStatus.SCHEDULED;
        }

        if (!now.isBefore(auction.getEndTime())) {
            return ProductStatus.CLOSED;
        }

        return ProductStatus.OPEN;
    }
}
