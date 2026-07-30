package com.tradex.flashtrade.broker.etrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePreviewOrderRequest {

    @JsonProperty("PreviewOrderRequest")
    private PreviewOrderRequest previewOrderRequest;

    public EtradePreviewOrderRequest() {}

    public EtradePreviewOrderRequest(PreviewOrderRequest previewOrderRequest) {
        this.previewOrderRequest = previewOrderRequest;
    }

    public PreviewOrderRequest getPreviewOrderRequest() { return previewOrderRequest; }
    public void setPreviewOrderRequest(PreviewOrderRequest previewOrderRequest) { this.previewOrderRequest = previewOrderRequest; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreviewOrderRequest {

        @JsonProperty("orderType")
        private String orderType;

        @JsonProperty("clientOrderId")
        private String clientOrderId;

        @JsonProperty("Order")
        private List<EtradeOrderRequestEntry> order;

        public String getOrderType() { return orderType; }
        public String getClientOrderId() { return clientOrderId; }
        public List<EtradeOrderRequestEntry> getOrder() { return order; }

        public void setOrderType(String orderType) { this.orderType = orderType; }
        public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
        public void setOrder(List<EtradeOrderRequestEntry> order) { this.order = order; }
    }
}
