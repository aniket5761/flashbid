package com.example.flashbid.common.redis;

import com.example.flashbid.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSummaryCache {
    private Long productId;
    private ProductStatus status;
    private Long currentBid;
    private Long nextMinimumBid;
    private Long minimumIncrement;
    private Long bidCount;
    private Long topBidderId;
    private String topBidderUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long version;
}
