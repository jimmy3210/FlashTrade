package com.tradex.flashtrade.dto.admin;

import java.util.List;

public record ScopeMappingBatchResultDto(
        List<MappingItemResult> users,
        List<MappingItemResult> accounts) {

    public record MappingItemResult(
            Long id,
            boolean success,
            String code,
            String message) {}
}
