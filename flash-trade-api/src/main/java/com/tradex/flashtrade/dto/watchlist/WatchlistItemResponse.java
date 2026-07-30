package com.tradex.flashtrade.dto.watchlist;

import java.math.BigDecimal;

public record WatchlistItemResponse(
        String symbol,
        String companyName,
        String exchange,
        boolean active,
        BigDecimal lastPrice,
        BigDecimal bidPrice,
        BigDecimal askPrice,
        Double changePercent,
        long timestamp) {
}
