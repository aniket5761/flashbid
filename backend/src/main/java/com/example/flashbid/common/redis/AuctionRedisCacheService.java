package com.example.flashbid.common.redis;

import com.example.flashbid.bid.dto.BidDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.flashbid.product.entity.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionRedisCacheService {

    private static final int RECENT_BIDS_LIMIT = 20;
    private static final String OPEN_AUCTIONS_KEY = "auction:open";
    private static final String SCHEDULED_AUCTIONS_KEY = "auction:scheduled";
    private static final String CLOSING_AUCTIONS_KEY = "auction:closing";

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuctionSummaryCache getSummary(Long productId) {
        try {
            Object value = redisTemplate.opsForValue().get(summaryKey(productId));
            return convertValue(value, AuctionSummaryCache.class, summaryKey(productId));
        } catch (Exception exception) {
            log.warn("Redis read failed for summary key {}", summaryKey(productId), exception);
            return null;
        }
    }

    public void cacheSummary(AuctionSummaryCache summary) {
        try {
            redisTemplate.opsForValue().set(summaryKey(summary.getProductId()), summary);
            syncAuctionSets(summary);
        } catch (Exception exception) {
            log.warn("Redis write failed for summary key {}", summaryKey(summary.getProductId()), exception);
        }
    }

    public List<BidDto> getRecentBids(Long productId) {
        try {
            List<Object> values = redisTemplate.opsForList().range(recentBidsKey(productId), 0, RECENT_BIDS_LIMIT - 1);
            if (values == null || values.isEmpty()) {
                return List.of();
            }

            return values.stream()
                    .map(value -> convertValue(value, BidDto.class, recentBidsKey(productId)))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            log.warn("Redis read failed for recent bids key {}", recentBidsKey(productId), exception);
            return List.of();
        }
    }

    public void cacheRecentBids(Long productId, List<BidDto> bids) {
        try {
            String key = recentBidsKey(productId);
            redisTemplate.delete(key);
            if (!bids.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, List.copyOf(bids).toArray());
                redisTemplate.opsForList().trim(key, 0, RECENT_BIDS_LIMIT - 1);
            }
        } catch (Exception exception) {
            log.warn("Redis write failed for recent bids key {}", recentBidsKey(productId), exception);
        }
    }

    public List<BidDto> prependRecentBid(Long productId, BidDto bid) {
        List<BidDto> updated = getRecentBids(productId).stream()
                .filter(existing -> !existing.getId().equals(bid.getId()))
                .toList();

        List<BidDto> merged = new java.util.ArrayList<>();
        merged.add(bid);
        merged.addAll(updated.stream().limit(RECENT_BIDS_LIMIT - 1).toList());
        cacheRecentBids(productId, merged);
        return merged;
    }

    public List<Long> getDueScheduledAuctionIds(LocalDateTime now) {
        try {
            return getDueAuctionIds(SCHEDULED_AUCTIONS_KEY, now);
        } catch (Exception exception) {
            log.warn("Redis read failed for scheduled auction index", exception);
            return List.of();
        }
    }

    public List<Long> getDueClosingAuctionIds(LocalDateTime now) {
        try {
            return getDueAuctionIds(CLOSING_AUCTIONS_KEY, now);
        } catch (Exception exception) {
            log.warn("Redis read failed for closing auction index", exception);
            return List.of();
        }
    }

    private void syncAuctionSets(AuctionSummaryCache summary) {
        String productId = String.valueOf(summary.getProductId());
        redisTemplate.opsForSet().remove(OPEN_AUCTIONS_KEY, productId);
        redisTemplate.opsForZSet().remove(SCHEDULED_AUCTIONS_KEY, productId);
        redisTemplate.opsForZSet().remove(CLOSING_AUCTIONS_KEY, productId);

        if (summary.getStatus() == ProductStatus.OPEN) {
            redisTemplate.opsForSet().add(OPEN_AUCTIONS_KEY, productId);
            addToClosing(summary.getProductId(), summary.getEndTime());
            return;
        }

        if (summary.getStatus() == ProductStatus.SCHEDULED) {
            addToScheduled(summary.getProductId(), summary.getStartTime());
        }
    }

    private void addToScheduled(Long productId, LocalDateTime startTime) {
        redisTemplate.opsForZSet().add(SCHEDULED_AUCTIONS_KEY, String.valueOf(productId), toEpochMilli(startTime));
    }

    private void addToClosing(Long productId, LocalDateTime endTime) {
        redisTemplate.opsForZSet().add(CLOSING_AUCTIONS_KEY, String.valueOf(productId), toEpochMilli(endTime));
    }

    private double toEpochMilli(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private List<Long> getDueAuctionIds(String key, LocalDateTime now) {
        Set<Object> values = redisTemplate.opsForZSet().rangeByScore(key, 0, toEpochMilli(now));
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .map(String::valueOf)
                .map(Long::valueOf)
                .toList();
    }

    private String summaryKey(Long productId) {
        return "auction:" + productId + ":summary";
    }

    private String recentBidsKey(Long productId) {
        return "auction:" + productId + ":recent_bids";
    }

    private <T> T convertValue(Object value, Class<T> targetType, String key) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }

        try {
            return objectMapper.convertValue(value, targetType);
        } catch (IllegalArgumentException exception) {
            log.warn("Failed to deserialize Redis value for key {} into {}", key, targetType.getSimpleName(), exception);
            return null;
        }
    }
}
