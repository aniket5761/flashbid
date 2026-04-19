package com.example.flashbid.common.redis;

import com.example.flashbid.auction.dto.AuctionWinnerDto;
import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.auction.entity.AuctionWinner;
import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.auction.repo.AuctionWinnerRepo;
import com.example.flashbid.bid.dto.BidDto;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.user.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionLiveStateService {

    private static final int RECENT_BIDS_LIMIT = 20;

    private final AuctionRepo auctionRepo;
    private final AuctionWinnerRepo auctionWinnerRepo;
    private final BidRepo bidRepo;
    private final AuctionRedisCacheService auctionRedisCacheService;

    @Transactional
    public AuctionLiveEvent buildEvent(Long productId, String type) {
        Auction auction = auctionRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found for product id: " + productId));
        Product product = auction.getProduct();

        AuctionSummaryCache cachedSummary = auctionRedisCacheService.getSummary(productId);
        List<BidDto> recentBids = auctionRedisCacheService.getRecentBids(productId);

        if (recentBids.isEmpty()) {
            recentBids = bidRepo.findByProductIdOrderByTimestampDesc(productId, PageRequest.of(0, RECENT_BIDS_LIMIT))
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }

        AuctionSummaryCache summary = buildSummary(product, auction, cachedSummary, recentBids);

        return AuctionLiveEvent.builder()
                .type(type)
                .summary(summary)
                .recentBids(recentBids)
                .winner(resolveWinner(productId, auction))
                .build();
    }

    @Transactional
    public AuctionLiveEvent buildBidPlacedEvent(Long productId, BidDto latestBid) {
        AuctionSummaryCache cachedSummary = auctionRedisCacheService.getSummary(productId);
        if (cachedSummary == null) {
            return buildEvent(productId, "BID_PLACED");
        }

        List<BidDto> recentBids = auctionRedisCacheService.prependRecentBid(productId, latestBid);
        AuctionSummaryCache updatedSummary = AuctionSummaryCache.builder()
                .productId(productId)
                .status(cachedSummary.getStatus())
                .currentBid(latestBid.getAmount())
                .nextMinimumBid(latestBid.getAmount() + cachedSummary.getMinimumIncrement())
                .minimumIncrement(cachedSummary.getMinimumIncrement())
                .bidCount(cachedSummary.getBidCount() + 1)
                .topBidderId(latestBid.getBidderId())
                .topBidderUsername(latestBid.getBidderUsername())
                .startTime(cachedSummary.getStartTime())
                .endTime(cachedSummary.getEndTime())
                .version(latestBid.getId())
                .build();

        return AuctionLiveEvent.builder()
                .type("BID_PLACED")
                .summary(updatedSummary)
                .recentBids(recentBids)
                .winner(null)
                .build();
    }

    private BidDto mapToDto(Bid bid) {
        return BidDto.builder()
                .id(bid.getId())
                .productId(bid.getProduct().getId())
                .amount(bid.getAmount())
                .bidderId(bid.getUser().getId())
                .bidderUsername(bid.getUser().getUsername())
                .timestamp(bid.getTimestamp())
                .build();
    }

    private AuctionSummaryCache buildSummary(
            Product product,
            Auction auction,
            AuctionSummaryCache cachedSummary,
            List<BidDto> recentBids
    ) {
        if (cachedSummary != null) {
            return AuctionSummaryCache.builder()
                    .productId(product.getId())
                    .status(auction.getStatus())
                    .currentBid(cachedSummary.getCurrentBid())
                    .nextMinimumBid(cachedSummary.getNextMinimumBid())
                    .minimumIncrement(auction.getMinimumIncrement())
                    .bidCount(cachedSummary.getBidCount())
                    .topBidderId(cachedSummary.getTopBidderId())
                    .topBidderUsername(cachedSummary.getTopBidderUsername())
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .version(cachedSummary.getVersion())
                    .build();
        }

        if (!recentBids.isEmpty()) {
            BidDto latestBid = recentBids.get(0);
            return AuctionSummaryCache.builder()
                    .productId(product.getId())
                    .status(auction.getStatus())
                    .currentBid(latestBid.getAmount())
                    .nextMinimumBid(latestBid.getAmount() + auction.getMinimumIncrement())
                    .minimumIncrement(auction.getMinimumIncrement())
                    .bidCount((long) recentBids.size())
                    .topBidderId(latestBid.getBidderId())
                    .topBidderUsername(latestBid.getBidderUsername())
                    .startTime(auction.getStartTime())
                    .endTime(auction.getEndTime())
                    .version(latestBid.getId())
                    .build();
        }

        return AuctionSummaryCache.builder()
                .productId(product.getId())
                .status(auction.getStatus())
                .currentBid(product.getStartingPrice())
                .nextMinimumBid(product.getStartingPrice() + auction.getMinimumIncrement())
                .minimumIncrement(auction.getMinimumIncrement())
                .bidCount(0L)
                .topBidderId(null)
                .topBidderUsername(null)
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .version(0L)
                .build();
    }

    private AuctionWinnerDto resolveWinner(Long productId, Auction auction) {
        if (auction.getStatus() != com.example.flashbid.product.entity.ProductStatus.CLOSED) {
            return null;
        }

        return auctionWinnerRepo.findByProductId(productId)
                .map(this::mapWinnerToDto)
                .orElse(null);
    }

    private AuctionWinnerDto mapWinnerToDto(AuctionWinner winner) {
        Product product = winner.getProduct();
        Auction auction = auctionRepo.findByProductId(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found for product ID: " + product.getId()));

        UserDto winnerDto = UserDto.builder()
                .id(winner.getUser().getId())
                .username(winner.getUser().getUsername())
                .email(winner.getUser().getEmail())
                .role(winner.getUser().getRole())
                .deleted(winner.getUser().isDeleted())
                .build();

        UserDto sellerDto = UserDto.builder()
                .id(product.getUser().getId())
                .username(product.getUser().getUsername())
                .firstName(product.getUser().getFirstName())
                .lastName(product.getUser().getLastName())
                .email(product.getUser().getEmail())
                .role(product.getUser().getRole())
                .registrationDate(product.getUser().getRegistrationDate())
                .deleted(product.getUser().isDeleted())
                .build();

        ProductDto productDto = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .startingPrice(product.getStartingPrice())
                .minimumIncrement(auction.getMinimumIncrement())
                .currentBid(winner.getAmount())
                .nextMinimumBid(winner.getAmount() + auction.getMinimumIncrement())
                .bidCount(bidRepo.countByProductId(product.getId()))
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .productStatus(auction.getStatus())
                .user(sellerDto)
                .build();

        return AuctionWinnerDto.builder()
                .id(winner.getId())
                .amount(winner.getAmount())
                .user(winnerDto)
                .product(productDto)
                .build();
    }
}
