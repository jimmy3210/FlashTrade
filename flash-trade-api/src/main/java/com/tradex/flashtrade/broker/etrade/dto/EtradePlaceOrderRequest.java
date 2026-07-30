package com.tradex.flashtrade.broker.etrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePlaceOrderRequest {

    @JsonProperty("PlaceOrderRequest")
    private PlaceOrderRequest placeOrderRequest;

    public EtradePlaceOrderRequest() {}

    public EtradePlaceOrderRequest(PlaceOrderRequest placeOrderRequest) {
        this.placeOrderRequest = placeOrderRequest;
    }

    public PlaceOrderRequest getPlaceOrderRequest() { return placeOrderRequest; }
    public void setPlaceOrderRequest(PlaceOrderRequest placeOrderRequest) { this.placeOrderRequest = placeOrderRequest; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceOrderRequest {

        @JsonProperty("orderType")
        private String orderType;

        @JsonProperty("clientOrderId")
        private String clientOrderId;

        @JsonProperty("previewId")
        private String previewId;

        @JsonProperty("Order")
        private List<EtradeOrderRequestEntry> order;

        public String getOrderType() { return orderType; }
        public String getClientOrderId() { return clientOrderId; }
        public String getPreviewId() { return previewId; }
        public List<EtradeOrderRequestEntry> getOrder() { return order; }

        public void setOrderType(String orderType) { this.orderType = orderType; }
        public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
        public void setPreviewId(String previewId) { this.previewId = previewId; }
        public void setOrder(List<EtradeOrderRequestEntry> order) { this.order = order; }
    }
}
