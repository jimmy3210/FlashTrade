package com.tradex.flashtrade.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tradex.flashtrade.broker.dto.MarketData;

@Service
public class MarketPushService {

    private static final Logger log = LoggerFactory.getLogger(MarketPushService.class);
    private static final String MARKET_TOPIC_PREFIX = "/topic/market/";

    private final SimpMessagingTemplate messagingTemplate;
    private final SubscriptionManager subscriptionManager;

    public MarketPushService(SimpMessagingTemplate messagingTemplate,
                             SubscriptionManager subscriptionManager) {
        this.messagingTemplate = messagingTemplate;
        this.subscriptionManager = subscriptionManager;
    }

    public void pushMarketData(MarketData data) {
        if (data == null || data.getSymbol() == null || data.getSymbol().isBlank()) {
            return;
        }

        String symbol = data.getSymbol().toUpperCase();
        int subscriberCount = subscriptionManager.getSubscriberCount(symbol);
        if (subscriberCount <= 0) {
            return;
        }

        try {
            messagingTemplate.convertAndSend(MARKET_TOPIC_PREFIX + symbol, data);
            log.debug("MarketData pushed to topic {} (subscribers={})", symbol, subscriberCount);
        } catch (Exception e) {
            log.warn("MarketData push failed symbol={} error={}", symbol, e.getMessage(), e);
        }
    }
}
