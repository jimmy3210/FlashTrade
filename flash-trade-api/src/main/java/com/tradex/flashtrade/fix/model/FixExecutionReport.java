package com.tradex.flashtrade.fix.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FIX ExecutionReport（Tag 8）の内部表現
 *
 * @param execId         約定 ID (ExecID Tag 17)
 * @param clOrdId        クライアント注文 ID (ClOrdID Tag 11)
 * @param orderId        ブローカー注文 ID (OrderID Tag 37)
 * @param symbol         銘柄コード (Symbol Tag 55)
 * @param side           売買区分 (Side Tag 54)
 * @param orderStatus    注文ステータス (OrdStatus Tag 39)
 * @param execType       約定タイプ (ExecType Tag 150)
 * @param ordQty         注文数量 (OrderQty Tag 38)
 * @param cumQty         累積約定数量 (CumQty Tag 14)
 * @param leavesQty      残数量 (LeavesQty Tag 151)
 * @param lastQty        今回約定数量 (LastQty Tag 32)
 * @param lastPx         今回約定価格 (LastPx Tag 31)
 * @param avgPx          平均約定価格 (AvgPx Tag 6)
 * @param account        口座 ID (Account Tag 1)
 * @param text           拒否理由等テキスト (Text Tag 58)
 * @param fixVersion     受信 FIX バージョン
 * @param receivedAt     受信日時（UTC）
 */
public record FixExecutionReport(
        String execId,
        String clOrdId,
        String orderId,
        String symbol,
        char side,
        FixOrderStatus orderStatus,
        char execType,
        BigDecimal ordQty,
        BigDecimal cumQty,
        BigDecimal leavesQty,
        BigDecimal lastQty,
        BigDecimal lastPx,
        BigDecimal avgPx,
        String account,
        String text,
        String fixVersion,
        Instant receivedAt
) {}
