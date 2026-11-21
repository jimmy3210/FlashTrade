package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AccountOwnerMappingRequest {
    @NotNull
    @Positive
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
