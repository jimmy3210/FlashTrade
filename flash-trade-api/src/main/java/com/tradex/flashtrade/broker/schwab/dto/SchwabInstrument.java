package com.tradex.flashtrade.broker.schwab.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schwab 持仓中的金融工具信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchwabInstrument {

    @JsonProperty("assetType")
    private String assetType;

    @JsonProperty("cusip")
    private String cusip;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("description")
    private String description;

    @JsonProperty("netChange")
    private Double netChange;

    public String getAssetType()  { return assetType; }
    public String getCusip()      { return cusip; }
    public String getSymbol()     { return symbol; }
    public String getDescription(){ return description; }
    public Double getNetChange()  { return netChange; }

    public void setAssetType(String v)   { this.assetType   = v; }
    public void setCusip(String v)       { this.cusip       = v; }
    public void setSymbol(String v)      { this.symbol      = v; }
    public void setDescription(String v) { this.description = v; }
    public void setNetChange(Double v)   { this.netChange   = v; }
}
