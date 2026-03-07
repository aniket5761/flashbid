package com.example.flashbid.auction.service;

import com.example.flashbid.auction.dto.AuctionWinnerDto;
import com.example.flashbid.auction.entity.AuctionWinner;
import com.example.flashbid.auction.repo.AuctionWinnerRepo;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.BidException;
import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.common.util.EntityFetcher;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.user.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionWinnerService {

    private final AuctionWinnerRepo auctionWinnerRepo;
    private final BidRepo bidRepo;
    private final EntityFetcher entityFetcher;

    @Transactional
    public AuctionWinner createWinner(Product product) {
        Optional<Bid> winningBidOpt = bidRepo.findTopByProductOrderByAmountDesc(product);

        if (winningBidOpt.isEmpty()) {
            // This is handled by the scheduler (it won't create a winner, but will close the auction)
            throw new BidException("No bids found for product ID: " + product.getId());
        }

        Bid winningBid = winningBidOpt.get();

        AuctionWinner auctionWinner = new AuctionWinner();
        auctionWinner.setUser(winningBid.getUser());
        auctionWinner.setProduct(product);
        auctionWinner.setAmount(winningBid.getAmount());

        return auctionWinnerRepo.save(auctionWinner);
    }

    public AuctionWinnerDto getAuctionWinner(Long productId) {
        Product product = entityFetcher.getProductById(productId);

        if (product.getProductStatus() != ProductStatus.CLOSED) {
            throw new BidException("Auction is still ongoing for product ID: " + productId);
        }

        AuctionWinner winner = auctionWinnerRepo.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Winner not found for product ID: " + productId));

        return mapToDto(winner);
    }

    private AuctionWinnerDto mapToDto(AuctionWinner winner) {
        Product product = winner.getProduct();
        UserDto userDto = UserDto.builder()
                .id(winner.getUser().getId())
                .username(winner.getUser().getUsername())
                .email(winner.getUser().getEmail())
                .role(winner.getUser().getRole())
                .build();

        ProductDto productDto = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .startingPrice(product.getStartingPrice())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .productStatus(product.getProductStatus())
                .user(userDto)
                .build();

        return AuctionWinnerDto.builder()
                .id(winner.getId())
                .amount(winner.getAmount())
                .user(userDto)
                .product(productDto)
                .build();
    }
}
