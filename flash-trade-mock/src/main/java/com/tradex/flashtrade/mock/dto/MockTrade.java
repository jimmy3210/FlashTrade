package com.tradex.flashtrade.mock.dto;

public record MockTrade(
        String id,
        String symbol,
        String side,
        int quantity,
        double price,
        String timestamp
) {
}
