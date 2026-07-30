package com.tradex.flashtrade.exception;

/**
 * 券商 API 调用失败时抛出的异常
 */
public class BrokerApiException extends RuntimeException {

    private final String brokerName;
    private final int    statusCode;

    public BrokerApiException(String brokerName, int statusCode, String message) {
        super(message);
        this.brokerName = brokerName;
        this.statusCode = statusCode;
    }

    public BrokerApiException(String brokerName, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.brokerName = brokerName;
        this.statusCode = statusCode;
    }

    public String getBrokerName() { return brokerName; }
    public int    getStatusCode() { return statusCode; }
}
