package com.example.flashbid.bid.controller;

import com.example.flashbid.bid.dto.BidDto;
import com.example.flashbid.bid.dto.CreateBidDto;
import com.example.flashbid.bid.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidDto> placeBid(@Valid @RequestBody CreateBidDto createBidDto) {
        return ResponseEntity.ok(bidService.placeBid(createBidDto));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<BidDto>> getBidsByProductId(
            @PathVariable Long productId,
            @RequestParam Optional<Integer> page) {
        return ResponseEntity.ok(bidService.getBidsByProductId(productId, page));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BidDto>> getBidsByUserId(
            @PathVariable Long userId,
            @RequestParam Optional<Integer> page) {
        return ResponseEntity.ok(bidService.getBidsByUserId(userId, page));
    }
}
