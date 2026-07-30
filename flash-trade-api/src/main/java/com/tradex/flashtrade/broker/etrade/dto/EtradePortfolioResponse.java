package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * E*TRADE 账户持仓响应
 * <pre>
 * GET /v1/accounts/{accountIdKey}/portfolio
 * 响应: { "PortfolioResponse": { "AccountPortfolio": [{ ... }] } }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradePortfolioResponse {

    @JsonProperty("PortfolioResponse")
    private PortfolioResponse portfolioResponse;

    public PortfolioResponse getPortfolioResponse() { return portfolioResponse; }
    public void setPortfolioResponse(PortfolioResponse v) { this.portfolioResponse = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortfolioResponse {

        @JsonProperty("AccountPortfolio")
        private List<AccountPortfolio> accountPortfolio;

        @JsonProperty("totalNoOfPages")
        private Integer totalNoOfPages;

        @JsonProperty("nextPageNo")
        private String nextPageNo;

        public List<AccountPortfolio> getAccountPortfolio() { return accountPortfolio; }
        public Integer getTotalNoOfPages()                   { return totalNoOfPages; }
        public String getNextPageNo()                        { return nextPageNo; }

        public void setAccountPortfolio(List<AccountPortfolio> v) { this.accountPortfolio = v; }
        public void setTotalNoOfPages(Integer v)                   { this.totalNoOfPages   = v; }
        public void setNextPageNo(String v)                        { this.nextPageNo       = v; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountPortfolio {

        @JsonProperty("accountId")
        private String accountId;

        @JsonProperty("Position")
        private List<EtradePosition> positions;

        /** 账户总市场价值 */
        @JsonProperty("totalMarketValue")
        private Double totalMarketValue;

        /** 账户总浮动盈亏 */
        @JsonProperty("totalGain")
        private Double totalGain;

        public String getAccountId()                  { return accountId; }
        public List<EtradePosition> getPositions()    { return positions; }
        public Double getTotalMarketValue()            { return totalMarketValue; }
        public Double getTotalGain()                  { return totalGain; }

        public void setAccountId(String v)                  { this.accountId       = v; }
        public void setPositions(List<EtradePosition> v)    { this.positions       = v; }
        public void setTotalMarketValue(Double v)           { this.totalMarketValue = v; }
        public void setTotalGain(Double v)                  { this.totalGain       = v; }
    }
}
