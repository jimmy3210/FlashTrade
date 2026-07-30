package com.tradex.flashtrade.broker.tastytrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TastytradePositionsResponse {
    private List<TastytradePosition> data;

    public List<TastytradePosition> getData() { return data; }
    public void setData(List<TastytradePosition> data) { this.data = data; }
}
