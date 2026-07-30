package com.tradex.flashtrade.fix.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FIX 内部注文モデル（バージョン非依存の統一形式）
 *
 * @param clOrdId          クライアント注文 ID
 * @param origClOrdId      元の注文 ID（キャンセル/修正時）
 * @param symbol           銘柄コード（例: AAPL）
 * @param side             売買区分（'1'=買, '2'=売）
 * @param orderType        注文種別（'1'=成行, '2'=指値）
 * @param quantity         注文数量
 * @param price            指値価格（成行の場合は null）
 * @param timeInForce      注文有効期間（'0'=当日, '1'=GTC 等）
 * @param account          口座 ID
 * @param brokerKey        接続ブローカーキー（設定ファイルのキー）
 * @param fixVersion       使用 FIX バージョン（"FIX.4.2" 等）
 * @param createdAt        注文作成日時（UTC）
 */
public record FixInternalOrder(
        String clOrdId,
        String origClOrdId,
        String symbol,
        char side,
        char orderType,
        BigDecimal quantity,
        BigDecimal price,
        char timeInForce,
        String account,
        String brokerKey,
        String fixVersion,
        Instant createdAt
) {
    /**
     * 新規注文用ファクトリメソッド
     */
    public static FixInternalOrder newOrder(
            String clOrdId,
            String symbol,
            char side,
            char orderType,
            BigDecimal quantity,
            BigDecimal price,
            char timeInForce,
            String account,
            String brokerKey,
            String fixVersion
    ) {
        return new FixInternalOrder(
                clOrdId, null, symbol, side, orderType,
                quantity, price, timeInForce, account, brokerKey,
                fixVersion, Instant.now()
        );
    }

    /**
     * キャンセル注文用ファクトリメソッド
     */
    public static FixInternalOrder cancelOrder(
            String clOrdId,
            String origClOrdId,
            String symbol,
            char side,
            BigDecimal quantity,
            String account,
            String brokerKey,
            String fixVersion
    ) {
        return new FixInternalOrder(
                clOrdId, origClOrdId, symbol, side, '1',
                quantity, null, '0', account, brokerKey,
                fixVersion, Instant.now()
        );
    }

    /**
     * 注文修正（OrderCancelReplaceRequest）用ファクトリメソッド
     * origClOrdId に変更前の ClOrdID を、clOrdId に新しい ClOrdID を設定する。
     */
    public static FixInternalOrder modifyOrder(
            String clOrdId,
            String origClOrdId,
            String symbol,
            char side,
            char orderType,
            BigDecimal newQuantity,
            BigDecimal newPrice,
            char timeInForce,
            String account,
            String brokerKey,
            String fixVersion
    ) {
        return new FixInternalOrder(
                clOrdId, origClOrdId, symbol, side, orderType,
                newQuantity, newPrice, timeInForce, account, brokerKey,
                fixVersion, Instant.now()
        );
    }
}
