package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotNull;

public class CompanyStatusRequest {
    @NotNull(message = "状态不能为空")
    private Boolean isActive;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
