package com.example.flashbid.bid.service;

import com.example.flashbid.bid.dto.BidDto;
import com.example.flashbid.bid.dto.CreateBidDto;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.BidAccessDeniedException;
import com.example.flashbid.common.exception.BidException;
import com.example.flashbid.common.handlers.SocketConnectionHandler;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepo bidRepo;
    private final EntityFetcher entityFetcher;
    private final SocketConnectionHandler socketConnectionHandler;

    @Transactional
    public BidDto placeBid(CreateBidDto createBidDto) {
        Product product = entityFetcher.getProductById(createBidDto.getProductId());
        User user = entityFetcher.getCurrentUser();

        Long currentHighest = bidRepo.findTopByProductOrderByAmountDesc(product)
                .map(Bid::getAmount)
                .orElse(product.getStartingPrice());

        if (product.getProductStatus() != ProductStatus.OPEN) {
            throw new BidException("Bidding is not allowed. Auction is not open.");
        }

        if (product.getUser().getId().equals(user.getId())) {
            throw new BidAccessDeniedException("You cannot bid on your own product.");
        }

        if (LocalDateTime.now().isAfter(product.getEndTime())) {
            throw new BidException("Auction has already ended at: " + product.getEndTime());
        }

        if (createBidDto.getAmount() <= currentHighest) {
            throw new BidException("Bid must be higher than current highest bid: " + currentHighest);
        }

        Bid bid = new Bid();
        bid.setAmount(createBidDto.getAmount());
        bid.setUser(user);
        bid.setProduct(product);
        bid.setTimestamp(LocalDateTime.now());

        Bid savedBid = bidRepo.save(bid);
        BidDto bidDto = mapToDto(savedBid);

        try {
            socketConnectionHandler.sendBidToProduct(bidDto);
        } catch (IOException e) {
            // Log the error but don't fail the transaction as the bid is saved
            System.err.println("Failed to send real-time update: " + e.getMessage());
        }

        return bidDto;
    }

    public Page<BidDto> getBidsByProductId(Long productId, Optional<Integer> page) {
        PageRequest pageRequest = PageRequest.of(
                page.orElse(0),
                12
        );

        Page<Bid> bids = bidRepo.findByProductIdOrderByAmountDesc(productId, pageRequest);
        return bids.map(this::mapToDto);
    }

    private BidDto mapToDto(Bid bid) {
        return BidDto.builder()
                .id(bid.getId())
                .productId(bid.getProduct().getId())
                .amount(bid.getAmount())
                .bidderUsername(bid.getUser().getUsername())
                .timestamp(bid.getTimestamp())
                .build();
    }
}
