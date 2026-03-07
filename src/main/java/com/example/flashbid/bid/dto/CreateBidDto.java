package com.example.flashbid.bid.dto;

import jakarta.validation.constraints.NotNull;
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
    private Long amount;
}
