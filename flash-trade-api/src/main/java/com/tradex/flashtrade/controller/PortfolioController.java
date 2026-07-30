package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.dto.portfolio.OrderResponse;
import com.tradex.flashtrade.dto.portfolio.PositionResponse;
import com.tradex.flashtrade.dto.portfolio.PositionView;
import com.tradex.flashtrade.dto.portfolio.TradeResponse;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.service.PortfolioService;
import com.tradex.flashtrade.service.PositionAggregatorService;
import com.tradex.flashtrade.service.UserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "组合管理", description = "持仓、订单、成交查询")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService           portfolioService;
    private final PositionAggregatorService  aggregatorService;
    private final UserLookupService          userLookupService;

    public PortfolioController(PortfolioService portfolioService,
                               PositionAggregatorService aggregatorService,
                               UserLookupService userLookupService) {
        this.portfolioService  = portfolioService;
        this.aggregatorService = aggregatorService;
        this.userLookupService = userLookupService;
    }

    @GetMapping("/portfolio/positions")
    @Operation(summary = "查询持仓", description = "返回当前登录用户的全部持仓列表，可按 accountId / symbol 过滤")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String symbol) {
        logger.debug("PortfolioController.getPositions: username={} accountId={} symbol={}",
                userDetails.getUsername(), accountId, symbol);
        List<PositionResponse> positions = portfolioService.getPositions(
                userDetails.getUsername(), accountId, symbol);
        logger.debug("PortfolioController.getPositions returned {} positions for username={}", positions.size(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    @GetMapping("/orders")
    @Operation(summary = "查询订单",
               description = "返回当前登录用户的订单列表。paginated=true（默认）时返回分页结果；paginated=false 时返回全件列表。")
    public ResponseEntity<ApiResponse<?>> getOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "true")  boolean paginated,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "20")    int size) {

        if (!paginated) {
            List<OrderResponse> all = portfolioService.getAllOrders(userDetails.getUsername(), status);
            return ResponseEntity.ok(ApiResponse.success(all));
        }

        size = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> orders = portfolioService.getOrders(userDetails.getUsername(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/trades")
    @Operation(summary = "查询成交",
               description = "返回当前登录用户的成交记录。可选 brokerName 参数按券商过滤。")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTrades(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String brokerName) {

        List<TradeResponse> trades = (brokerName != null && !brokerName.isBlank())
                ? portfolioService.getTradesByBroker(userDetails.getUsername(), brokerName.toUpperCase())
                : portfolioService.getTrades(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(trades));
    }

    /**
     * 资产视图（聚合计算结果）
     * 根据成交记录 + 未成交委托 + 实时行情，计算每只股票的完整资产视图。
     */
    @GetMapping("/portfolio/asset-view")
    @Operation(summary = "资产视图（聚合）",
               description = "根据用户成交记录 + 未成交委托 + Redis 行情，实时计算 PositionView。可按 accountId 过滤。")
    public ResponseEntity<ApiResponse<List<PositionView>>> getAssetView(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String accountId) {
        Long userId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        List<PositionView> views = (accountId != null && !accountId.isBlank())
                ? aggregatorService.aggregateByAccount(userId, accountId)
                : aggregatorService.aggregate(userId);
        return ResponseEntity.ok(ApiResponse.success(views));
    }
}
