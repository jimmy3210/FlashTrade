package com.tradex.flashtrade.broker.tastytrade.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TastytradePosition {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal average_cost;
    private BigDecimal last_price;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getAverage_cost() { return average_cost; }
    public void setAverage_cost(BigDecimal average_cost) { this.average_cost = average_cost; }
    public BigDecimal getLast_price() { return last_price; }
    public void setLast_price(BigDecimal last_price) { this.last_price = last_price; }
}
