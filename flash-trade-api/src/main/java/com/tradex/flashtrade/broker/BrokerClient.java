package com.tradex.flashtrade.broker;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.BrokerTrade;
import com.tradex.flashtrade.broker.dto.BrokerWatchlistItem;
import com.tradex.flashtrade.broker.dto.MarketData;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 券商统一接口
 * 每个具体券商实现此接口，系统通过此接口屏蔽各券商 API 差异。
 */
public interface BrokerClient {

    /** 返回券商标识（与数据库中 broker_name 对应） */
    String getBrokerName();

    /** 获取指定账户的持仓信息 */
    BrokerPortfolio getPortfolio(String accountId);

    /** 获取指定账户的订单列表 */
    List<BrokerOrder> getOrders(String accountId);

    /** 获取指定账户的成交记录 */
    List<BrokerTrade> getTrades(String accountId);

    /** 获取指定账户的 Watchlist（券商若支持） */
    default List<BrokerWatchlistItem> getWatchlist(String accountId) {
        throw new UnsupportedOperationException(getBrokerName() + " 暂未实现 getWatchlist");
    }

    // -------------------------------------------------------------------------
    // 行情接口
    // -------------------------------------------------------------------------

    /**
     * 批量获取行情（账户池调度用）。
     * 默认抛出 UnsupportedOperationException，各券商按实际 API 覆盖。
     *
     * @param accountId 用于发起请求的账户
     * @param symbols   symbol 列表（不超过券商限制，通常 ≤30）
     * @return symbol → MarketData 映射
     */
    default Map<String, MarketData> getBatchMarketData(String accountId, List<String> symbols) {
        throw new UnsupportedOperationException(getBrokerName() + " 暂未实现 getBatchMarketData");
    }

    // -------------------------------------------------------------------------
    // 交易操作接口
    // -------------------------------------------------------------------------

    /**
     * 下单。
     * 默认抛出 UnsupportedOperationException，各券商按实际 API 覆盖。
     *
     * @param accountId 账户 ID
     * @param request   下单请求（含幂等 clientOrderId）
     * @return 下单结果（含券商侧 externalOrderId）
     */
    default PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        throw new UnsupportedOperationException(getBrokerName() + " 暂未实现 placeOrder");
    }

    /**
     * 撤单。
     * 默认抛出 UnsupportedOperationException，各券商按实际 API 覆盖。
     *
     * @param accountId       账户 ID
     * @param externalOrderId 券商侧订单 ID
     */
    default void cancelOrder(String accountId, String externalOrderId) {
        throw new UnsupportedOperationException(getBrokerName() + " 暂未实现 cancelOrder");
    }

    /**
     * 查询单笔订单状态。
     * 默认抛出 UnsupportedOperationException，各券商按实际 API 覆盖。
     */
    default BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        throw new UnsupportedOperationException(getBrokerName() + " 暂未实现 getOrderStatus");
    }

    // -------------------------------------------------------------------------
    // 异步辅助方法
    // -------------------------------------------------------------------------

    /** 异步获取持仓信息 */
    default CompletableFuture<BrokerPortfolio> getPortfolioAsync(String accountId) {
        return CompletableFuture.supplyAsync(() -> getPortfolio(accountId));
    }

    /** 异步获取订单列表 */
    default CompletableFuture<List<BrokerOrder>> getOrdersAsync(String accountId) {
        return CompletableFuture.supplyAsync(() -> getOrders(accountId));
    }
}
