package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.RiskService;
import com.tradex.flashtrade.service.UserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 风控管理接口（TODO-08）
 * <p>
 * 所有端点均需要 ROLE_ADMIN 权限。
 */
@RestController
@RequestMapping("/admin/risk")
@Tag(name = "风控管理", description = "账户关停 / 解除 / 限制 / 强制清仓")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class RiskController {

    private final RiskService       riskService;
    private final UserLookupService userLookupService;

    public RiskController(RiskService riskService, UserLookupService userLookupService) {
        this.riskService       = riskService;
        this.userLookupService = userLookupService;
    }

    @GetMapping("/status")
    @Operation(summary = "查询账户风控状态")
    public ResponseEntity<ApiResponse<Map<String, String>>> getStatus(
            @RequestParam String accountId) {
        String status = riskService.getRiskStatus(accountId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("accountId", accountId, "riskStatus", status)));
    }

    @PostMapping("/disable")
    @Operation(summary = "关停账户", description = "将账户状态设为 DISABLED，下单请求将被拒绝")
    public ResponseEntity<ApiResponse<String>> disable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.disableAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已关停"));
    }

    @PostMapping("/enable")
    @Operation(summary = "解除账户关停", description = "将账户状态恢复为 NORMAL")
    public ResponseEntity<ApiResponse<String>> enable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.enableAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已恢复正常"));
    }

    @PostMapping("/limit")
    @Operation(summary = "限制账户交易", description = "将账户状态设为 LIMITED（只读，不能下单）")
    public ResponseEntity<ApiResponse<String>> limit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.limitAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已限制交易"));
    }

    @PostMapping("/liquidate")
    @Operation(summary = "强制清仓", description = "异步执行：对账户所有持仓发出市价卖出委托，状态设为 LIQUIDATED。不可逆操作。")
    public ResponseEntity<ApiResponse<String>> liquidate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        // 先禁止后续下单
        riskService.disableAccount(accountId, operatorId);
        // 异步清仓
        riskService.forceLiquidation(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success(
                "账户 [" + accountId + "] 强制清仓已提交（异步执行）"));
    }
}
