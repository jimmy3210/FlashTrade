package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * E*TRADE 成交记录响应
 * <pre>
 * GET /v1/accounts/{accountIdKey}/transactions
 * 响应: { "TransactionListResponse": { "Transaction": [...] } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeTransactionResponse {

    @JsonProperty("TransactionListResponse")
    private TransactionListResponse transactionListResponse;

    public TransactionListResponse getTransactionListResponse() { return transactionListResponse; }
    public void setTransactionListResponse(TransactionListResponse v) {
        this.transactionListResponse = v;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionListResponse {

        @JsonProperty("Transaction")
        private List<EtradeTransaction> transactions;

        public List<EtradeTransaction> getTransactions() { return transactions; }
        public void setTransactions(List<EtradeTransaction> v) { this.transactions = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EtradeTransaction {

        @JsonProperty("transactionId")
        private Long transactionId;

        @JsonProperty("accountId")
        private String accountId;

        /** 交易类型: BOUGHT / SOLD / DIV / INT / DIVIDEND 等 */
        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("transactionDate")
        private Long transactionDate;

        @JsonProperty("settleDate")
        private Long settleDate;

        @JsonProperty("amount")
        private Double amount;

        @JsonProperty("description")
        private String description;

        @JsonProperty("brokerage")
        private BrokerageInfo brokerage;

        public Long getTransactionId()      { return transactionId; }
        public String getAccountId()        { return accountId; }
        public String getTransactionType()  { return transactionType; }
        public Long getTransactionDate()    { return transactionDate; }
        public Long getSettleDate()         { return settleDate; }
        public Double getAmount()           { return amount; }
        public String getDescription()      { return description; }
        public BrokerageInfo getBrokerage() { return brokerage; }

        public void setTransactionId(Long v)       { this.transactionId    = v; }
        public void setAccountId(String v)         { this.accountId        = v; }
        public void setTransactionType(String v)   { this.transactionType  = v; }
        public void setTransactionDate(Long v)     { this.transactionDate  = v; }
        public void setSettleDate(Long v)          { this.settleDate       = v; }
        public void setAmount(Double v)            { this.amount           = v; }
        public void setDescription(String v)       { this.description      = v; }
        public void setBrokerage(BrokerageInfo v)  { this.brokerage        = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BrokerageInfo {

        @JsonProperty("product")
        private EtradeInstrument product;

        @JsonProperty("quantity")
        private Double quantity;

        @JsonProperty("price")
        private Double price;

        @JsonProperty("commission")
        private Double commission;

        /** 交易方向: BOUGHT / SOLD */
        @JsonProperty("transactionType")
        private String transactionType;

        public EtradeInstrument getProduct()     { return product; }
        public Double getQuantity()              { return quantity; }
        public Double getPrice()                 { return price; }
        public Double getCommission()            { return commission; }
        public String getTransactionType()       { return transactionType; }

        public void setProduct(EtradeInstrument v) { this.product          = v; }
        public void setQuantity(Double v)          { this.quantity         = v; }
        public void setPrice(Double v)             { this.price            = v; }
        public void setCommission(Double v)        { this.commission       = v; }
        public void setTransactionType(String v)   { this.transactionType  = v; }
    }
}
