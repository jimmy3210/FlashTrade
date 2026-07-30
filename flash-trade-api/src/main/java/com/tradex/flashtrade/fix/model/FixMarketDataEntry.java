package com.tradex.flashtrade.fix.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FIX 行情データエントリ（MarketDataSnapshotFullRefresh / IncrementalRefresh の 1 行）
 *
 * MDEntryType (Tag 269) の値:
 *   '0' = Bid（買値）
 *   '1' = Offer（売値）
 *   '2' = Trade（約定値）
 *   '4' = OpeningPrice（始値）
 *   '5' = ClosingPrice（終値）
 *   '7' = TradingSessionHighPrice（高値）
 *   '8' = TradingSessionLowPrice（安値）
 *   'B' = TradingSessionVWAPPrice（VWAP）
 *
 * @param symbol     銘柄コード（Tag 55）
 * @param entryType  MDEntryType (Tag 269)
 * @param price      MDEntryPx (Tag 270)
 * @param size       MDEntrySize (Tag 271)
 * @param fixVersion FIX バージョン識別子
 * @param receivedAt 受信タイムスタンプ（UTC）
 */
public record FixMarketDataEntry(
        String symbol,
        char entryType,
        BigDecimal price,
        BigDecimal size,
        String fixVersion,
        Instant receivedAt
) {
    /** Bid エントリか判定 */
    public boolean isBid()   { return entryType == '0'; }

    /** Offer（Ask）エントリか判定 */
    public boolean isOffer() { return entryType == '1'; }

    /** 約定値エントリか判定 */
    public boolean isTrade() { return entryType == '2'; }
}
