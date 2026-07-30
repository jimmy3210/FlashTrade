package com.tradex.flashtrade.broker.tastytrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TastytradeWatchlistResponse {

    private List<TastytradeWatchlist> data;

    public List<TastytradeWatchlist> getData() {
        return data;
    }

    public void setData(List<TastytradeWatchlist> data) {
        this.data = data;
    }
}
