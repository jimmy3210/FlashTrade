package com.tradex.flashtrade.dto.broker;

public class BrokerConfigRequest {

    private String mode;
    private String baseUrl;
    private Boolean mockEnabled;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Boolean getMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(Boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }
}
