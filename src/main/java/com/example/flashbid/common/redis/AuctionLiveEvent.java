package com.example.flashbid.common.redis;

import com.example.flashbid.auction.dto.AuctionWinnerDto;
import com.example.flashbid.bid.dto.BidDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionLiveEvent {
    private String type;
    private AuctionSummaryCache summary;
    private List<BidDto> recentBids;
    private AuctionWinnerDto winner;
}
