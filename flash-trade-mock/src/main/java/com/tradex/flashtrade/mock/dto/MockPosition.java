package com.tradex.flashtrade.mock.dto;

public record MockPosition(
        String symbol,
        int quantity,
        double price,
        double marketValue
) {
}
