package com.example.flashbid.auction.service;

import com.example.flashbid.auction.entity.Auction;
import com.example.flashbid.auction.dto.AuctionWinnerDto;
import com.example.flashbid.auction.entity.AuctionWinner;
import com.example.flashbid.auction.repo.AuctionRepo;
import com.example.flashbid.auction.repo.AuctionWinnerRepo;
import com.example.flashbid.bid.entity.Bid;
import com.example.flashbid.bid.repo.BidRepo;
import com.example.flashbid.common.exception.BidException;
import com.example.flashbid.common.exception.ResourceNotFoundException;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.entity.Product;
import com.example.flashbid.product.entity.ProductStatus;
import com.example.flashbid.user.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionWinnerService {

    private final AuctionWinnerRepo auctionWinnerRepo;
    private final AuctionRepo auctionRepo;
    private final BidRepo bidRepo;

    @Transactional
    public AuctionWinner createWinner(Product product) {
        Optional<AuctionWinner> existingWinner = auctionWinnerRepo.findByProductId(product.getId());
        if (existingWinner.isPresent()) {
            return existingWinner.get();
        }

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

    @Transactional
    public AuctionWinnerDto getAuctionWinner(Long productId) {
        Auction auction = auctionRepo.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found for product ID: " + productId));

        if (auction.getStatus() != ProductStatus.CLOSED && !LocalDateTime.now().isBefore(auction.getEndTime())) {
            auction.setStatus(ProductStatus.CLOSED);
            auction.getProduct().setProductStatus(ProductStatus.CLOSED);
        }

        if (auction.getStatus() != ProductStatus.CLOSED) {
            throw new BidException("Auction is still ongoing for product ID: " + productId);
        }

        AuctionWinner winner = auctionWinnerRepo.findByProductId(productId)
                .orElseGet(() -> createWinner(auction.getProduct()));

        return mapToDto(winner);
    }

    private AuctionWinnerDto mapToDto(AuctionWinner winner) {
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
