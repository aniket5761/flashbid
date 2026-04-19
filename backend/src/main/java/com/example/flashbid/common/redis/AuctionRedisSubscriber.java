package com.example.flashbid.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionRedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleMessage(String payload) {
        try {
            AuctionLiveEvent event = objectMapper.readValue(payload, AuctionLiveEvent.class);
            if (event.getSummary() == null || event.getSummary().getProductId() == null) {
                return;
            }

            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + event.getSummary().getProductId(),
                    event
            );
        } catch (Exception exception) {
            log.warn("Failed to broadcast Redis auction update. Raw payload: {}", payload, exception);
        }
    }
}
