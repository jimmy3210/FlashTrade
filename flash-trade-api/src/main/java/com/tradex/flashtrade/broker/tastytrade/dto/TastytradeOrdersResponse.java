package com.tradex.flashtrade.broker.tastytrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TastytradeOrdersResponse {
    private List<TastytradeOrder> data;

    public List<TastytradeOrder> getData() { return data; }
    public void setData(List<TastytradeOrder> data) { this.data = data; }
}
