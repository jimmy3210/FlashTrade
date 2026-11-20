package com.tradex.flashtrade.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CompanyMappingRequest {
    @NotNull
    @Positive
    private Long companyId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
