package com.tradex.flashtrade.broker.etrade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EtradeApiError {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("description")
    private String description;

    public Integer getCode() { return code; }
    public String getDescription() { return description; }

    public void setCode(Integer code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
}
