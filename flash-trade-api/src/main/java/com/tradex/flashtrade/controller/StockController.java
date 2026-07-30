package com.tradex.flashtrade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.dto.stock.StockResponse;
import com.tradex.flashtrade.service.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping({"/api/v1/stocks", "/api/v1/quotes", "/api/quotes"})
@Tag(name = "股票/行情查询", description = "股票搜索与白名单接口")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/search")
    @Operation(summary = "搜索股票", description = "根据 symbol 或名称模糊检索股票白名单，支持自动注册合法 symbol。")
    public ResponseEntity<ApiResponse<List<StockResponse>>> searchStocks(
            @RequestParam String query) {
        List<StockResponse> stocks = stockService.searchStocks(query);
        return ResponseEntity.ok(ApiResponse.success(stocks));
    }
}
