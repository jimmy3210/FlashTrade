package com.tradex.flashtrade.broker.tastytrade;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.BrokerPortfolio;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.broker.tastytrade.config.TastytradeProperties;
import com.tradex.flashtrade.service.BrokerConfigService;

/**
 * Tastytrade BrokerClient 实现（支持 mock 和 real 两种模式）。
 */
@Component
public class TastytradeBrokerClient implements BrokerClient {

    private final TastytradeProperties props;
    private final TastytradeRestClient restClient;
    private final TastytradeMapper mapper;
    private final BrokerConfigService configService;

    public TastytradeBrokerClient(TastytradeProperties props, TastytradeRestClient restClient, TastytradeMapper mapper,
                                  BrokerConfigService configService) {
        this.props = props;
        this.restClient = restClient;
        this.mapper = mapper;
        this.configService = configService;
    }

    @Override
    public String getBrokerName() { return "TASTYTRADE"; }

    @Override
    public BrokerPortfolio getPortfolio(String accountId) {
        if (isMockEnabled()) {
            return BrokerPortfolio.builder().accountId(accountId).brokerName("TASTYTRADE").positions(Collections.emptyList()).build();
        }
        var resp = restClient.getPositions(accountId);
        return mapper.toPortfolio(resp, accountId);
    }

    @Override
    public List<BrokerOrder> getOrders(String accountId) {
        if (isMockEnabled()) return Collections.emptyList();
        var resp = restClient.getOrders(accountId);
        return mapper.toOrders(resp);
    }

    @Override
    public java.util.List<com.tradex.flashtrade.broker.dto.BrokerTrade> getTrades(String accountId) {
        if (isMockEnabled()) return Collections.emptyList();
        return mapper.toTrades(restClient.getOrders(accountId));
    }

    @Override
    public java.util.List<com.tradex.flashtrade.broker.dto.BrokerWatchlistItem> getWatchlist(String accountId) {
        if (isMockEnabled()) return Collections.emptyList();
        var resp = restClient.getWatchlist(accountId);
        if (resp == null || resp.getData() == null) {
            return Collections.emptyList();
        }
        return resp.getData().stream()
                .map(item -> new com.tradex.flashtrade.broker.dto.BrokerWatchlistItem(
                        getBrokerName(), item.getName(), item.getSymbol()))
                .toList();
    }

    @Override
    public PlaceOrderResult placeOrder(String accountId, OrderRequest request) {
        if (isMockEnabled()) {
            return PlaceOrderResult.builder().success(false).message("mock mode").build();
        }
        var resp = restClient.placeOrder(accountId, request);
        return mapper.toPlaceOrderResult(resp);
    }

    @Override
    public void cancelOrder(String accountId, String externalOrderId) {
        if (isMockEnabled()) throw new UnsupportedOperationException("Tastytrade cancelOrder not implemented in mock mode");
        boolean ok = restClient.cancelOrder(accountId, externalOrderId);
        if (!ok) throw new RuntimeException("Tastytrade cancelOrder failed for " + externalOrderId);
    }

    private boolean isMockEnabled() {
        return configService.isMockEnabled(getBrokerName(), props.isMockEnabled());
    }
}

