package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * E*TRADE 持仓中的金融工具信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeInstrument {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("description")
    private String description;

    /** 资产类型: EQ（股票）/ OPTN（期权）/ MF（共同基金）等 */
    @JsonProperty("securityType")
    private String securityType;

    public String getSymbol()       { return symbol; }
    public String getDescription()  { return description; }
    public String getSecurityType() { return securityType; }

    public void setSymbol(String v)       { this.symbol       = v; }
    public void setDescription(String v)  { this.description  = v; }
    public void setSecurityType(String v) { this.securityType = v; }
}
