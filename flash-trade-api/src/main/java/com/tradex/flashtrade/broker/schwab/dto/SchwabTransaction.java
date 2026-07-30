package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Schwab 成交/交易记录
 * <pre>
 * GET /accounts/{accountNumber}/transactions?types=TRADE
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabTransaction {

    /** 活动唯一 ID */
    @JsonProperty("activityId")
    private Long activityId;

    /** 关联订单 ID */
    @JsonProperty("orderId")
    private Long orderId;

    /** 交易类型: TRADE / RECEIVE_AND_DELIVER / DIVIDEND_OR_INTEREST / ... */
    @JsonProperty("type")
    private String type;

    /** 交易描述 */
    @JsonProperty("description")
    private String description;

    /** 账户编号 */
    @JsonProperty("accountNumber")
    private String accountNumber;

    /** 交易发生时间（ISO-8601） */
    @JsonProperty("time")
    private String time;

    /** 成交日期 */
    @JsonProperty("tradeDate")
    private String tradeDate;

    /** 结算日期 */
    @JsonProperty("settleDate")
    private String settleDate;

    /** 净金额（正=收入，负=支出） */
    @JsonProperty("netAmount")
    private Double netAmount;

    /** 资产转移明细列表 */
    @JsonProperty("transferItems")
    private List<SchwabTransferItem> transferItems;

    public Long getActivityId()                        { return activityId; }
    public Long getOrderId()                           { return orderId; }
    public String getType()                            { return type; }
    public String getDescription()                     { return description; }
    public String getAccountNumber()                   { return accountNumber; }
    public String getTime()                            { return time; }
    public String getTradeDate()                       { return tradeDate; }
    public String getSettleDate()                      { return settleDate; }
    public Double getNetAmount()                       { return netAmount; }
    public List<SchwabTransferItem> getTransferItems() { return transferItems; }

    public void setActivityId(Long v)                        { this.activityId    = v; }
    public void setOrderId(Long v)                           { this.orderId       = v; }
    public void setType(String v)                            { this.type          = v; }
    public void setDescription(String v)                     { this.description   = v; }
    public void setAccountNumber(String v)                   { this.accountNumber = v; }
    public void setTime(String v)                            { this.time          = v; }
    public void setTradeDate(String v)                       { this.tradeDate     = v; }
    public void setSettleDate(String v)                      { this.settleDate    = v; }
    public void setNetAmount(Double v)                       { this.netAmount     = v; }
    public void setTransferItems(List<SchwabTransferItem> v) { this.transferItems = v; }
}
