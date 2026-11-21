package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
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
@Tag(name = "风控管理", description = "查询账户风控状态；变更和清仓操作暂缓")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
    @Operation(summary = "关停账户", description = "账户范围授权能力完成前拒绝变更")
    public ResponseEntity<ApiResponse<String>> disable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.disableAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已关停"));
    }

    @PostMapping("/enable")
    @Operation(summary = "解除账户关停", description = "账户范围授权能力完成前拒绝变更")
    public ResponseEntity<ApiResponse<String>> enable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.enableAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已恢复正常"));
    }

    @PostMapping("/limit")
    @Operation(summary = "限制账户交易", description = "账户范围授权能力完成前拒绝变更")
    public ResponseEntity<ApiResponse<String>> limit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        riskService.limitAccount(accountId, operatorId);
        return ResponseEntity.ok(ApiResponse.success("账户 [" + accountId + "] 已限制交易"));
    }

    @PostMapping("/liquidate")
    @Operation(summary = "强制清仓", description = "执行状态核验能力完成前拒绝请求")
    public ResponseEntity<ApiResponse<String>> liquidate(
            @RequestParam String accountId) {
        throw new BusinessException(ErrorCode.INVALID_STATE,
                "强制清仓已暂停，待补齐执行状态核验、环境校验和审计能力");
    }
}
