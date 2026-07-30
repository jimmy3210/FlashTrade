package com.tradex.flashtrade.broker.tastytrade.dto;

import java.math.BigDecimal;

public class TastytradePlaceOrderRequest {
    private String symbol;
    private String side; // BUY/SELL
    private String type; // MARKET/LIMIT
    private BigDecimal quantity;
    private BigDecimal limit_price;
    private String client_order_id;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getLimit_price() { return limit_price; }
    public void setLimit_price(BigDecimal limit_price) { this.limit_price = limit_price; }
    public String getClient_order_id() { return client_order_id; }
    public void setClient_order_id(String client_order_id) { this.client_order_id = client_order_id; }
}
