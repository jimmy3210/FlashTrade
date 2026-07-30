package com.tradex.flashtrade.broker.etrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePlaceOrderResponse {

    @JsonProperty("PlaceOrderResponse")
    private PlaceOrderResponse placeOrderResponse;

    public PlaceOrderResponse getPlaceOrderResponse() { return placeOrderResponse; }
    public void setPlaceOrderResponse(PlaceOrderResponse placeOrderResponse) { this.placeOrderResponse = placeOrderResponse; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaceOrderResponse {

        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("orderDate")
        private Long orderDate;

        @JsonProperty("errors")
        private List<EtradeApiError> errors;

        public String getOrderId() { return orderId; }
        public Long getOrderDate() { return orderDate; }
        public List<EtradeApiError> getErrors() { return errors; }

        public void setOrderId(String orderId) { this.orderId = orderId; }
        public void setOrderDate(Long orderDate) { this.orderDate = orderDate; }
        public void setErrors(List<EtradeApiError> errors) { this.errors = errors; }
    }
}
