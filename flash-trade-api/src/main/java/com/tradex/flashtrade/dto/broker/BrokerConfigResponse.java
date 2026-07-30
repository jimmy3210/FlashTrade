package com.tradex.flashtrade.dto.broker;

public class BrokerConfigResponse {

    private String brokerName;
    private String mode;
    private String baseUrl;
    private boolean mockEnabled;
    private String updatedAt;

    public BrokerConfigResponse() {
    }

    public BrokerConfigResponse(String brokerName, String mode,
                                String baseUrl, boolean mockEnabled,
                                String updatedAt) {
        this.brokerName = brokerName;
        this.mode = mode;
        this.baseUrl = baseUrl;
        this.mockEnabled = mockEnabled;
        this.updatedAt = updatedAt;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

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

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
