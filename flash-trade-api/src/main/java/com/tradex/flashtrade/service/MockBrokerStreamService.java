package com.tradex.flashtrade.service;

import java.math.BigDecimal;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tradex.flashtrade.broker.dto.MarketData;

/**
 * 模拟券商流式行情推送：对当前已订阅的 symbol 做小幅随机价格波动，并通过 MarketPushService 推送。
 */
@Service
public class MockBrokerStreamService {

    private static final Logger log = LoggerFactory.getLogger(MockBrokerStreamService.class);

    private final SubscriptionManager subscriptionManager;
    private final MarketDataService marketDataService;
    private final MarketPushService marketPushService;

    public MockBrokerStreamService(SubscriptionManager subscriptionManager,
                                   MarketDataService marketDataService,
                                   MarketPushService marketPushService) {
        this.subscriptionManager = subscriptionManager;
        this.marketDataService = marketDataService;
        this.marketPushService = marketPushService;
    }

    @Scheduled(fixedRate = 1000)
    public void publishMockStream() {
        Set<String> symbols = subscriptionManager.getSubscribedSymbols();
        if (symbols.isEmpty()) return;

        for (String sym : symbols) {
            try {
                BigDecimal base = marketDataService.get(sym).map(MarketData::getLastPrice)
                        .orElseGet(() -> BigDecimal.valueOf(100 + Math.abs(sym.hashCode()) % 400));
                double deltaPct = (Math.random() - 0.5) * 0.01; // ±0.5%
                BigDecimal next = base.multiply(BigDecimal.valueOf(1.0 + deltaPct)).setScale(2, java.math.RoundingMode.HALF_UP);
                MarketData md = MarketData.builder()
                        .symbol(sym)
                        .lastPrice(next)
                        .bidPrice(next.subtract(BigDecimal.valueOf(0.01)))
                        .askPrice(next.add(BigDecimal.valueOf(0.01)))
                        .timestamp(System.currentTimeMillis())
                        .build();

                marketDataService.put(md);
                marketPushService.pushMarketData(md);
            } catch (Exception e) {
                log.warn("Mock stream failed for {}: {}", sym, e.getMessage());
            }
        }
    }
}
