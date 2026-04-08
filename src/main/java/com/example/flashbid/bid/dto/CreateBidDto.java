package com.example.flashbid.bid.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBidDto {

    @NotNull(message = "Invalid type for product id.")
    private Long productId;

    @NotNull(message = "Set amount.")
    @Positive(message = "Bid amount must be greater than 0.")
    private Long amount;
}
