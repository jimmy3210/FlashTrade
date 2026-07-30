package com.tradex.flashtrade.fix.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FIX 在仓回報モデル（PositionReport MsgType=AP 対応）
 *
 * FIX 4.4 以降で使用可能（RequestForPositions MsgType=AN への応答）。
 * FIX 4.2 では PositionReport は未定義のため、
 * FIX 4.2 セッションでは getPortfolio は UnsupportedOperationException をスローする。
 *
 * @param posMaintRptId   PosMaintRptID (Tag 721)
 * @param posReqId        PosReqID (Tag 710)（リクエスト ID）
 * @param account         Account (Tag 1)
 * @param symbol          Symbol (Tag 55)
 * @param longQty         LongQty (Tag 704)
 * @param shortQty        ShortQty (Tag 705)
 * @param settledLongQty  決済済み Long 数量（PosType=ALC など）
 * @param settledShortQty 決済済み Short 数量
 * @param totalNumReports TotalNumPosReports (Tag 727) - バッチ応答の合計数
 * @param fixVersion      FIX バージョン識別子
 * @param receivedAt      受信タイムスタンプ（UTC）
 */
public record FixPositionReport(
        String posMaintRptId,
        String posReqId,
        String account,
        String symbol,
        BigDecimal longQty,
        BigDecimal shortQty,
        BigDecimal settledLongQty,
        BigDecimal settledShortQty,
        int totalNumReports,
        String fixVersion,
        Instant receivedAt
) {
    /** ネットポジション（Long - Short）を計算して返す */
    public BigDecimal netQty() {
        BigDecimal l = longQty  != null ? longQty  : BigDecimal.ZERO;
        BigDecimal s = shortQty != null ? shortQty : BigDecimal.ZERO;
        return l.subtract(s);
    }
}
