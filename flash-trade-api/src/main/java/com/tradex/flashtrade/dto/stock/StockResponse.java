package com.tradex.flashtrade.dto.stock;

/**
 * 股票搜索返回结构。
 */
public class StockResponse {

    private String symbol;
    private String companyName;
    private String exchange;
    private boolean active;

    public StockResponse() {}

    public StockResponse(String symbol, String companyName, String exchange, boolean active) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.exchange = exchange;
        this.active = active;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
