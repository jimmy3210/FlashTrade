package com.tradex.flashtrade.fix.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FIX 成交回報モデル（TradeCaptureReport MsgType=AE 対応）
 *
 * FIX 4.4 以降で使用可能。FIX 4.2 では TradeCaptureReport は未定義のため、
 * FIX 4.2 セッションでは getOrders/getTrades は ExecutionReport 経由で管理する。
 *
 * @param tradeReportId  TradeReportID (Tag 571)
 * @param execId         ExecID (Tag 17)
 * @param orderId        OrderID (Tag 37)
 * @param clOrdId        ClOrdID (Tag 11)
 * @param symbol         Symbol (Tag 55)
 * @param side           Side (Tag 54): '1'=Buy, '2'=Sell
 * @param lastQty        LastQty (Tag 32) - 約定数量
 * @param lastPx         LastPx (Tag 31) - 約定価格
 * @param commission     Commission (Tag 12)（null 可）
 * @param account        Account (Tag 1)
 * @param tradeDate      TradeDate (Tag 75) or TransactTime (Tag 60)
 * @param fixVersion     FIX バージョン識別子
 * @param receivedAt     受信タイムスタンプ（UTC）
 */
public record FixTradeReport(
        String tradeReportId,
        String execId,
        String orderId,
        String clOrdId,
        String symbol,
        char side,
        BigDecimal lastQty,
        BigDecimal lastPx,
        BigDecimal commission,
        String account,
        Instant tradeDate,
        String fixVersion,
        Instant receivedAt
) {
    /** 売買区分を文字列で返す */
    public String sideAsString() {
        return side == '1' ? "BUY" : (side == '2' ? "SELL" : "UNKNOWN");
    }
}
