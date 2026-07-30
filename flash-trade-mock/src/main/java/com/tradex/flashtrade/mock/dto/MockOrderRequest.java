package com.tradex.flashtrade.mock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MockOrderRequest(
        @NotBlank String symbol,
        @NotBlank String side,
        @NotBlank String orderType,
        @Positive int quantity,
        Double limitPrice
) {
}
