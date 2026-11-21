package com.tradex.flashtrade.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.broker.BrokerConfigRequest;
import com.tradex.flashtrade.dto.broker.BrokerConfigResponse;
import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.BrokerConfigService;
import com.tradex.flashtrade.service.PositionAggregatorService;
import com.tradex.flashtrade.service.RiskService;
import com.tradex.flashtrade.service.UserLookupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 后台管理接口（TODO-07 MVP）
 * <p>
 * 最低 MVP 实现：
 * <ul>
 *   <li>GET /admin/positions — 返回全部用户仓位（按角色 Scope 过滤，当前 MVP 不过滤）</li>
 *   <li>GET /admin/users — 基础用户列表</li>
 *   <li>GET /admin/logs/api — API 调用日志</li>
 *   <li>GET /admin/logs/operations — 操作日志</li>
 *   <li>GET /admin/logs/login — 登录日志</li>
 * </ul>
 *
 * <p>所有接口均需 ROLE_ADMIN 权限。
 * 完整 RBAC + Scope 过滤由 TODO-07 后续实装。
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "后台管理", description = "管理员专用接口（需 ADMIN 角色）")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final PositionAggregatorService aggregatorService;
    private final UserLookupService         userLookupService;
    private final RiskService               riskService;
    private final JdbcTemplate              jdbc;
    private final BrokerConfigService       brokerConfigService;

    public AdminController(PositionAggregatorService aggregatorService,
                           UserLookupService userLookupService,
                           RiskService riskService,
                           JdbcTemplate jdbc,
                           BrokerConfigService brokerConfigService) {
        this.aggregatorService = aggregatorService;
        this.userLookupService = userLookupService;
        this.riskService       = riskService;
        this.jdbc              = jdbc;
        this.brokerConfigService = brokerConfigService;
    }

    // -------------------------------------------------------------------------
    // 仓位监控
    // -------------------------------------------------------------------------

    /**
     * 获取全部用户的仓位聚合视图（MVP：无 Scope 过滤）。
     * TODO: 后续按角色 Scope 限制可见账户范围。
     */
    @GetMapping("/positions")
    @Operation(summary = "全量仓位监控", description = "返回所有用户的聚合仓位视图（MVP 版，待接入 RBAC Scope 过滤）")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPositions(
            @RequestParam(required = false) String accountId) {

        String sql = accountId == null
                ? "SELECT * FROM positions ORDER BY user_id, broker_name, symbol"
                : "SELECT * FROM positions WHERE account_id = ? ORDER BY symbol";

        List<Map<String, Object>> rows = accountId == null
                ? jdbc.queryForList(sql)
                : jdbc.queryForList(sql, accountId);

        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    // -------------------------------------------------------------------------
    // 用户管理
    // -------------------------------------------------------------------------

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "返回系统内所有用户（脱敏：不含密码）")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsers() {
        String sql = """
                SELECT id, username, role, enabled, created_at, updated_at
                  FROM users
                 ORDER BY id
                """;
        return ResponseEntity.ok(ApiResponse.success(jdbc.queryForList(sql)));
    }

    // -------------------------------------------------------------------------
    // 日志查询
    // -------------------------------------------------------------------------

    @GetMapping("/logs/api")
    @Operation(summary = "API 调用日志", description = "查询 t_api_log，支持按 broker / status 过滤，按时间倒序")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getApiLogs(
            @RequestParam(required = false) String broker,
            @RequestParam(defaultValue = "100") int limit) {

        String sql = broker != null
                ? "SELECT * FROM t_api_log WHERE broker = ? ORDER BY created_at DESC LIMIT ?"
                : "SELECT * FROM t_api_log ORDER BY created_at DESC LIMIT ?";

        List<Map<String, Object>> rows = broker != null
                ? jdbc.queryForList(sql, broker, limit)
                : jdbc.queryForList(sql, limit);

        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @GetMapping("/logs/operations")
    @Operation(summary = "操作日志", description = "查询 t_operation_log，按时间倒序")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOperationLogs(
            @RequestParam(defaultValue = "100") int limit) {
        String sql = "SELECT * FROM t_operation_log ORDER BY created_at DESC LIMIT ?";
        return ResponseEntity.ok(ApiResponse.success(jdbc.queryForList(sql, limit)));
    }

    @GetMapping("/logs/login")
    @Operation(summary = "登录日志", description = "查询 t_login_log，支持按 login_result 过滤")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLoginLogs(
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "100") int limit) {

        String sql = result != null
                ? "SELECT * FROM t_login_log WHERE login_result = ? ORDER BY created_at DESC LIMIT ?"
                : "SELECT * FROM t_login_log ORDER BY created_at DESC LIMIT ?";

        List<Map<String, Object>> rows = result != null
                ? jdbc.queryForList(sql, result, limit)
                : jdbc.queryForList(sql, limit);

        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @GetMapping("/logs/errors")
    @Operation(summary = "错误日志", description = "查询 t_error_log，按时间倒序")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getErrorLogs(
            @RequestParam(defaultValue = "100") int limit) {
        String sql = "SELECT * FROM t_error_log ORDER BY created_at DESC LIMIT ?";
        return ResponseEntity.ok(ApiResponse.success(jdbc.queryForList(sql, limit)));
    }

    @GetMapping("/brokers/configs")
    @Operation(summary = "券商运行时配置列表", description = "返回服务端校验后的有效模式；不回显券商 API 地址")
    public ResponseEntity<ApiResponse<List<BrokerConfigResponse>>> getBrokerConfigs() {
        return ResponseEntity.ok(ApiResponse.success(brokerConfigService.listBrokerConfigs()));
    }

    @GetMapping("/brokers/configs/{brokerName}")
    @Operation(summary = "查询单个券商配置", description = "根据券商标识获取部署允许的有效模式；不回显券商 API 地址")
    public ResponseEntity<ApiResponse<BrokerConfigResponse>> getBrokerConfig(
            @PathVariable String brokerName) {
        return ResponseEntity.ok(ApiResponse.success(brokerConfigService.getBrokerConfig(brokerName)));
    }

    @PutMapping("/brokers/configs/{brokerName}")
    @Operation(summary = "更新券商运行时配置", description = "仅允许部署白名单内的券商模式；API 地址由服务端环境配置管理")
    public ResponseEntity<ApiResponse<BrokerConfigResponse>> updateBrokerConfig(
            @PathVariable String brokerName,
            @RequestBody BrokerConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brokerConfigService.updateBrokerConfig(brokerName, request)));
    }
}
