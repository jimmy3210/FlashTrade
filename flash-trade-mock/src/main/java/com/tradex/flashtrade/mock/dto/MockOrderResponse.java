package com.tradex.flashtrade.mock.dto;

public record MockOrderResponse(
        String id,
        String symbol,
        String side,
        String orderType,
        int quantity,
        Double limitPrice,
        String status
) {
}
