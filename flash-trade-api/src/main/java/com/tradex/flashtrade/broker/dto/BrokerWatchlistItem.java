package com.tradex.flashtrade.broker.dto;

public class BrokerWatchlistItem {

    private String brokerName;
    private String watchlistName;
    private String symbol;

    public BrokerWatchlistItem() {
    }

    public BrokerWatchlistItem(String brokerName, String watchlistName, String symbol) {
        this.brokerName = brokerName;
        this.watchlistName = watchlistName;
        this.symbol = symbol;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getWatchlistName() {
        return watchlistName;
    }

    public void setWatchlistName(String watchlistName) {
        this.watchlistName = watchlistName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
