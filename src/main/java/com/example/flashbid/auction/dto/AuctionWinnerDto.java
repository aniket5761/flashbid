package com.example.flashbid.auction.dto;

import com.example.flashbid.product.dto.ProductDto;
import com.example.flashbid.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionWinnerDto {
    private Long id;
    private Long amount;
    private UserDto user;
    private ProductDto product;
}
