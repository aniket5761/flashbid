package com.example.flashbid.auction.controller;

import com.example.flashbid.auction.dto.AuctionWinnerDto;
import com.example.flashbid.auction.service.AuctionWinnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionWinnerController {

    private final AuctionWinnerService auctionWinnerService;

    @GetMapping("/winner/{productId}")
    public ResponseEntity<AuctionWinnerDto> getAuctionWinner(@PathVariable Long productId) {
        return ResponseEntity.ok(auctionWinnerService.getAuctionWinner(productId));
    }
}
