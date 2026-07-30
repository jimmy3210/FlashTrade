package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Schwab 账户证券持仓包装器
 * <pre>
 * GET /accounts/{accountNumber}/positions
 * 响应: [{ "securitiesAccount": { "accountNumber": "...", "positions": [...] } }]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabAccountPositions {

    @JsonProperty("securitiesAccount")
    private SecuritiesAccount securitiesAccount;

    public SecuritiesAccount getSecuritiesAccount() { return securitiesAccount; }
    public void setSecuritiesAccount(SecuritiesAccount v) { this.securitiesAccount = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SecuritiesAccount {

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("type")
        private String type;

        @JsonProperty("positions")
        private List<SchwabPositionItem> positions;

        public String getAccountNumber()               { return accountNumber; }
        public String getType()                        { return type; }
        public List<SchwabPositionItem> getPositions() { return positions; }

        public void setAccountNumber(String v)               { this.accountNumber = v; }
        public void setType(String v)                        { this.type          = v; }
        public void setPositions(List<SchwabPositionItem> v) { this.positions     = v; }
    }
}
