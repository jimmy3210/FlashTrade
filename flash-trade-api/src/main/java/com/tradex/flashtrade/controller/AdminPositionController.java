package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.admin.PositionCloseRequest;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.service.RiskService;
import com.tradex.flashtrade.service.UserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "仓位管理", description = "管理员仓位监控与平仓接口")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPositionController {

    private final JdbcTemplate jdbc;
    private final RiskService riskService;
    private final UserLookupService userLookupService;

    public AdminPositionController(JdbcTemplate jdbc,
                                   RiskService riskService,
                                   UserLookupService userLookupService) {
        this.jdbc = jdbc;
        this.riskService = riskService;
        this.userLookupService = userLookupService;
    }

    @GetMapping("/positions")
    @Operation(summary = "查询全部用户仓位", description = "返回按用户分组的仓位列表，用于管理员监控")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> listPositions() {
        String sql = "SELECT user_id, account_id, broker_name, symbol, quantity, average_cost, current_price, market_value, unrealized_pnl, updated_at " +
                     "FROM positions ORDER BY user_id, account_id, broker_name, symbol";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String userId = String.valueOf(row.get("user_id"));
            grouped.computeIfAbsent(userId, k -> new ArrayList<>()).add(row);
        }
        return ResponseEntity.ok(ApiResponse.success(grouped));
    }

    @PostMapping("/positions/close_all")
    @Operation(summary = "一键平仓", description = "对指定用户的所有可用券商账户发起异步强制清仓")
    public ResponseEntity<ApiResponse<String>> closeAllPositions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PositionCloseRequest request) {
        Long operatorId = userLookupService.getUserIdByUsername(userDetails.getUsername());
        List<String> accountIds = jdbc.queryForList(
                "SELECT account_no FROM t_broker_account WHERE user_id = ? AND status = 'ACTIVE'",
                String.class,
                request.getUserId());
        if (accountIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND,
                    "用户未绑定有效券商账户：" + request.getUserId());
        }

        for (String accountId : accountIds) {
            riskService.disableAccount(accountId, operatorId);
            riskService.forceLiquidation(accountId, operatorId);
        }

        return ResponseEntity.ok(ApiResponse.success("用户 " + request.getUserId() + " 的平仓请求已提交"));
    }
}
