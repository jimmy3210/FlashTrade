package com.tradex.flashtrade.broker.etrade.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePreviewOrderResponse {

    @JsonProperty("PreviewOrderResponse")
    private PreviewOrderResponse previewOrderResponse;

    public PreviewOrderResponse getPreviewOrderResponse() { return previewOrderResponse; }
    public void setPreviewOrderResponse(PreviewOrderResponse previewOrderResponse) { this.previewOrderResponse = previewOrderResponse; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreviewOrderResponse {

        @JsonProperty("previewId")
        private String previewId;

        @JsonProperty("previewTime")
        private Long previewTime;

        @JsonProperty("totalCommission")
        private Double totalCommission;

        @JsonProperty("totalValue")
        private Double totalValue;

        @JsonProperty("marginRequirement")
        private Double marginRequirement;

        @JsonProperty("marginTotalBuyPower")
        private Double marginTotalBuyPower;

        @JsonProperty("warnings")
        private List<String> warnings;

        @JsonProperty("errors")
        private List<EtradeApiError> errors;

        @JsonProperty("Order")
        private List<EtradeOrder> order;

        public String getPreviewId() { return previewId; }
        public Long getPreviewTime() { return previewTime; }
        public Double getTotalCommission() { return totalCommission; }
        public Double getTotalValue() { return totalValue; }
        public Double getMarginRequirement() { return marginRequirement; }
        public Double getMarginTotalBuyPower() { return marginTotalBuyPower; }
        public List<String> getWarnings() { return warnings; }
        public List<EtradeApiError> getErrors() { return errors; }
        public List<EtradeOrder> getOrder() { return order; }

        public void setPreviewId(String previewId) { this.previewId = previewId; }
        public void setPreviewTime(Long previewTime) { this.previewTime = previewTime; }
        public void setTotalCommission(Double totalCommission) { this.totalCommission = totalCommission; }
        public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
        public void setMarginRequirement(Double marginRequirement) { this.marginRequirement = marginRequirement; }
        public void setMarginTotalBuyPower(Double marginTotalBuyPower) { this.marginTotalBuyPower = marginTotalBuyPower; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        public void setErrors(List<EtradeApiError> errors) { this.errors = errors; }
        public void setOrder(List<EtradeOrder> order) { this.order = order; }
    }
}
