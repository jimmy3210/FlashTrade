package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class CompanyUpdateRequest {
    @NotBlank(message = "公司名称不能为空")
    private String name;

    private Boolean isActive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
