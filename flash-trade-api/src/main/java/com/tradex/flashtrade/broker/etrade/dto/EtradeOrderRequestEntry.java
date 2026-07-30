package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tradex.flashtrade.broker.dto.OrderRequest;

/**
 * E*TRADE 下单请求中的单条委托条目。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeOrderRequestEntry {

    @JsonProperty("allOrNone")
    private boolean allOrNone = false;

    @JsonProperty("stopPrice")
    private Double stopPrice = 0.00;

    @JsonProperty("accountType")
    private String accountType = "CASH";

    @JsonProperty("clientId")
    private String clientId = "FlashTrade";

    @JsonProperty("limitPrice")
    private Double limitPrice;

    @JsonProperty("orderType")
    private String orderType;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("side")
    private String side;

    @JsonProperty("timeInForce")
    private String timeInForce = "Day";

    @JsonProperty("orderId")
    private Long orderId = 0L;

    public static EtradeOrderRequestEntry fromOrderRequest(OrderRequest request) {
        EtradeOrderRequestEntry entry = new EtradeOrderRequestEntry();
        entry.orderType = request.getOrderType() != null
                ? switch (request.getOrderType()) {
                    case MARKET -> "Market";
                    case LIMIT -> "Limit";
                }
                : "Market";
        entry.symbol = request.getSymbol();
        entry.quantity = request.getQuantity();
        entry.side = request.getSide() != null ? request.getSide().name() : null;
        entry.limitPrice = request.getLimitPrice() != null ? request.getLimitPrice().doubleValue() : null;
        entry.stopPrice = request.getStopPrice() != null ? request.getStopPrice().doubleValue() : null;
        entry.timeInForce = request.getTimeInForce() != null ? request.getTimeInForce() : "Day";
        return entry;
    }

    public boolean isAllOrNone() { return allOrNone; }
    public Double getStopPrice() { return stopPrice; }
    public String getAccountType() { return accountType; }
    public String getClientId() { return clientId; }
    public Double getLimitPrice() { return limitPrice; }
    public String getOrderType() { return orderType; }
    public String getSymbol() { return symbol; }
    public Integer getQuantity() { return quantity; }
    public String getSide() { return side; }
    public String getTimeInForce() { return timeInForce; }
    public Long getOrderId() { return orderId; }

    public void setAllOrNone(boolean allOrNone) { this.allOrNone = allOrNone; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setLimitPrice(Double limitPrice) { this.limitPrice = limitPrice; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setSide(String side) { this.side = side; }
    public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
