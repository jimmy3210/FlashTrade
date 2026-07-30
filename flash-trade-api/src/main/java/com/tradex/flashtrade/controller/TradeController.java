package com.tradex.flashtrade.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.dto.admin.BrokerAccountDto;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.BrokerAccountService;
import com.tradex.flashtrade.service.TradeExecutionService;
import com.tradex.flashtrade.service.UserLookupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 交易执行接口
 * <p>
 * 提供下单、撤单、查询委托状态三个端点。
 */
@RestController
@RequestMapping("/api/v1/trade")
@Tag(name = "交易操作", description = "下单 / 撤单 / 委托状态查询 / 当前账户查询")
@SecurityRequirement(name = "bearerAuth")
public class TradeController {

    private final TradeExecutionService tradeExecutionService;
    private final BrokerAccountService brokerAccountService;
    private final UserLookupService userLookupService;

    public TradeController(TradeExecutionService tradeExecutionService,
                         BrokerAccountService brokerAccountService,
                         UserLookupService userLookupService) {
        this.tradeExecutionService = tradeExecutionService;
        this.brokerAccountService = brokerAccountService;
        this.userLookupService = userLookupService;
    }

    /**
     * 下单
     *
     * @param accountId 目标账户 ID（Query 参数）
     * @param request   下单请求体
     */
    @PostMapping("/order")
    @Operation(summary = "下单", description = "向指定账户发送买入或卖出委托。支持 clientOrderId 幂等控制，重复提交返回原委托结果。")
    public ResponseEntity<ApiResponse<PlaceOrderResult>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId,
            @Valid @RequestBody OrderRequest request) {
        PlaceOrderResult result = tradeExecutionService.placeOrder(
                userDetails.getUsername(), accountId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 撤单
     *
     * @param accountId       账户 ID
     * @param externalOrderId 券商侧委托 ID
     */
    @DeleteMapping("/order/{externalOrderId}")
    @Operation(summary = "撤单", description = "撤销指定账户的未成交委托。")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId,
            @PathVariable String externalOrderId) {
        tradeExecutionService.cancelOrder(userDetails.getUsername(), accountId, externalOrderId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 查询单笔委托状态（实时，直接调用券商 API）
     */
    @GetMapping("/order/{externalOrderId}/status")
    @Operation(summary = "查询委托状态", description = "实时查询指定委托的最新状态（直接调用券商 API，不使用缓存）。")
    public ResponseEntity<ApiResponse<BrokerOrder>> getOrderStatus(
            @RequestParam String accountId,
            @PathVariable String externalOrderId) {
        BrokerOrder order = tradeExecutionService.getOrderStatus(accountId, externalOrderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * 获取当前用户绑定的券商账户及 API 环境
     * 客户端用于确定使用 LIVE 或 SANDBOX 端点
     */
    @GetMapping("/current-account")
    @Operation(summary = "获取用户当前账户", description = "查询当前登录用户绑定的券商账户及其 API 环境（LIVE/SANDBOX）")
    public ResponseEntity<ApiResponse<BrokerAccountDto>> getCurrentAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        BrokerAccountDto account = brokerAccountService.getCurrentAccountForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(account));
    }
}
