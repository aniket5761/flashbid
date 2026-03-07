package com.example.flashbid.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidDto {
    private Long id;
    private Long productId;
    private Long amount;
    private String bidderUsername;
    private LocalDateTime timestamp;
}
