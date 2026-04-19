package com.example.flashbid.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.flashbid.bid.dto.BidDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionLiveUpdateService {

    private final AuctionLiveStateService auctionLiveStateService;
    private final AuctionRedisCacheService auctionRedisCacheService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic auctionUpdatesTopic;
    private final ObjectMapper objectMapper;

    public void scheduleRefresh(Long productId, String eventType) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshAndPublish(productId, eventType);
                }
            });
            return;
        }

        refreshAndPublish(productId, eventType);
    }

    public void scheduleBidPlaced(Long productId, BidDto latestBid) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishBidPlaced(productId, latestBid);
                }
            });
            return;
        }

        publishBidPlaced(productId, latestBid);
    }

    public void refreshAndPublish(Long productId, String eventType) {
        try {
            AuctionLiveEvent event = auctionLiveStateService.buildEvent(productId, eventType);
            auctionRedisCacheService.cacheSummary(event.getSummary());
            auctionRedisCacheService.cacheRecentBids(productId, event.getRecentBids());
            stringRedisTemplate.convertAndSend(auctionUpdatesTopic.getTopic(), objectMapper.writeValueAsString(event));
        } catch (Exception exception) {
            log.warn("Failed to refresh live auction state for product {}", productId, exception);
        }
    }

    private void publishBidPlaced(Long productId, BidDto latestBid) {
        try {
            AuctionLiveEvent event = auctionLiveStateService.buildBidPlacedEvent(productId, latestBid);
            auctionRedisCacheService.cacheSummary(event.getSummary());
            stringRedisTemplate.convertAndSend(auctionUpdatesTopic.getTopic(), objectMapper.writeValueAsString(event));
        } catch (Exception exception) {
            log.warn("Failed to publish bid-placed live update for product {}", productId, exception);
            refreshAndPublish(productId, "BID_PLACED");
        }
    }
}
