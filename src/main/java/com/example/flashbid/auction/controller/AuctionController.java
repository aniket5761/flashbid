package com.example.flashbid.auction.controller;

import com.example.flashbid.auction.service.AuctionManagementService;
import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionManagementService auctionManagementService;
    private final ProductService productService;

    @PostMapping("/{productId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> forceCloseAuction(@PathVariable Long productId) {
        auctionManagementService.forceCloseAuction(productId);
        return ResponseEntity.ok(productService.getProductById(productId));
    }
}
