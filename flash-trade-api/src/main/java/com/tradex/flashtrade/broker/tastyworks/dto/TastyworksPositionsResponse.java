package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tastyworks 账户持仓响应
 * <pre>
 * GET /accounts/{account-number}/positions
 * 响应: { "data": { "items": [ { ... } ] } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksPositionsResponse {

    @JsonProperty("data")
    private PositionsData data;

    public PositionsData getData() { return data; }
    public void setData(PositionsData v) { this.data = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionsData {

        @JsonProperty("items")
        private List<TastyworksPosition> items;

        public List<TastyworksPosition> getItems() { return items; }
        public void setItems(List<TastyworksPosition> v) { this.items = v; }
    }
}
