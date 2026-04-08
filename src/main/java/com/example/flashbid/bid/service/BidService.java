package com.example.flashbid.bid.service;

import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.auction.service.AuctionManagementService;
import com.example.flashbid.bid.dto.BidDto;
import com.example.flashbid.bid.dto.CreateBidDto;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.BidAccessDeniedException;
import com.example.flashbid.common.exception.BidException;
import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.common.redis.AuctionLiveUpdateService;
import com.example.flashbid.common.redis.AuctionRedisCacheService;
import com.example.flashbid.common.redis.AuctionSummaryCache;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.user.entity.User;
import com.example.flashbid.user.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final AuctionRepo auctionRepo;
    private final AuctionManagementService auctionManagementService;
    private final BidRepo bidRepo;
    private final EntityFetcher entityFetcher;
    private final AuctionRedisCacheService auctionRedisCacheService;
    private final AuctionLiveUpdateService auctionLiveUpdateService;

    @Transactional
    public BidDto placeBid(CreateBidDto createBidDto) {
        Auction auction = auctionRepo.findByProductIdForUpdate(createBidDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found for product id: " + createBidDto.getProductId()));
        Product product = auction.getProduct();
        User user = entityFetcher.getCurrentUser();

        auctionManagementService.syncAuctionStatus(auction);

        Long currentHighest = bidRepo.findTopByProductOrderByAmountDesc(product)
                .map(Bid::getAmount)
                .orElse(product.getStartingPrice());

        if (auction.getStatus() != com.example.flashbid.product.entity.ProductStatus.OPEN) {
            throw new BidException("Bidding is not allowed. Auction is not open.");
        }

        if (product.getUser().getId().equals(user.getId())) {
            throw new BidAccessDeniedException("You cannot bid on your own product.");
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new BidException("Auction has already ended at: " + auction.getEndTime());
        }

        Long minimumAllowed = currentHighest + auction.getMinimumIncrement();
        if (createBidDto.getAmount() < minimumAllowed) {
            throw new BidException("Bid must be at least " + minimumAllowed + " for this auction.");
        }

        Bid bid = new Bid();
        bid.setAmount(createBidDto.getAmount());
        bid.setUser(user);
        bid.setProduct(product);
        bid.setTimestamp(LocalDateTime.now());

        Bid savedBid = bidRepo.save(bid);
        BidDto bidDto = mapToDto(savedBid);
        auctionLiveUpdateService.scheduleBidPlaced(product.getId(), bidDto);

        return bidDto;
    }

    @Transactional
    public Page<BidDto> getBidsByProductId(Long productId, Optional<Integer> page) {
        int pageNumber = page.orElse(0);
        PageRequest pageRequest = PageRequest.of(pageNumber, 12);

        if (pageNumber == 0) {
            List<BidDto> recentBids = auctionRedisCacheService.getRecentBids(productId);
            AuctionSummaryCache summary = auctionRedisCacheService.getSummary(productId);
            if (!recentBids.isEmpty() && summary != null) {
                return new PageImpl<>(recentBids, pageRequest, summary.getBidCount());
            }
        }

        Page<Bid> bids = bidRepo.findByProductIdOrderByTimestampDesc(productId, pageRequest);
        return bids.map(this::mapToDto);
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

    @Transactional
    public Page<BidDto> getBidsByUserId(Long userId, Optional<Integer> page) {
        User currentUser = entityFetcher.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new BidAccessDeniedException("You do not have permission to view this bid history.");
        }
        PageRequest pageRequest = PageRequest.of(page.orElse(0), 12);
        return bidRepo.findByUserIdOrderByTimestampDesc(userId, pageRequest).map(this::mapToDto);
    }
}
