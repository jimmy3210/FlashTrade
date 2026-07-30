package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * E*TRADE 订单列表响应包装
 * <pre>
 * { "OrdersResponse": { "Order": [...], "next": "...", "marker": "..." } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeOrdersResponse {

    @JsonProperty("OrdersResponse")
    private OrdersResponse ordersResponse;

    public OrdersResponse getOrdersResponse() { return ordersResponse; }
    public void setOrdersResponse(OrdersResponse v) { this.ordersResponse = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrdersResponse {

        @JsonProperty("Order")
        private List<EtradeOrder> orders;

        @JsonProperty("marker")
        private String marker;

        public List<EtradeOrder> getOrders() { return orders; }
        public String getMarker()            { return marker; }

        public void setOrders(List<EtradeOrder> v) { this.orders = v; }
        public void setMarker(String v)            { this.marker = v; }
    }
}
