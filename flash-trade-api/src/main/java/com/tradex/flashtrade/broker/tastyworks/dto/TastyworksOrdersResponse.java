package com.tradex.flashtrade.broker.tastyworks.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tastyworks 委托订单列表响应
 * <pre>
 * GET /accounts/{account-number}/orders
 * 响应: { "data": { "items": [ { ... } ] } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TastyworksOrdersResponse {

    @JsonProperty("data")
    private OrdersData data;

    public OrdersData getData() { return data; }
    public void setData(OrdersData v) { this.data = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrdersData {

        @JsonProperty("items")
        private List<TastyworksOrder> items;

        public List<TastyworksOrder> getItems() { return items; }
        public void setItems(List<TastyworksOrder> v) { this.items = v; }
    }
}
