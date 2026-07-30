package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.service.UserLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史盈亏分析接口（TODO-05 残り）
 * <p>
 * 提供对 {@code t_daily_pnl_stock} 和 {@code t_daily_pnl_account} 的查询。
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "历史分析", description = "每日盈亏快照查询")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final JdbcTemplate      jdbc;
    private final UserLookupService userLookupService;

    public AnalyticsController(JdbcTemplate jdbc, UserLookupService userLookupService) {
        this.jdbc             = jdbc;
        this.userLookupService = userLookupService;
    }

    /**
     * 查询指定日期的账户每日汇总盈亏。
     *
     * @param date 日期（格式 yyyy-MM-dd，默认今日）
     */
    @GetMapping("/pnl/daily")
    @Operation(summary = "账户每日汇总盈亏", description = "返回当前用户所有账户在指定日期的总盈亏快照")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyAccountPnl(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) date = LocalDate.now();

        Long userId = userLookupService.getUserIdByUsername(userDetails.getUsername());

        // 通过 positions 表关联账户 ID 所属用户
        String sql = """
                SELECT a.*
                  FROM t_daily_pnl_account a
                 WHERE a.snapshot_date = ?
                   AND a.broker_account_id IN (
                       SELECT DISTINCT p.account_id FROM positions p WHERE p.user_id = ?
                   )
                 ORDER BY a.broker_account_id
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, date, userId);
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    /**
     * 查询指定日期的个股盈亏明细。
     *
     * @param date     日期（格式 yyyy-MM-dd，默认今日）
     * @param symbol   可选，过滤特定股票
     * @param accountId 可选，过滤特定账户
     */
    @GetMapping("/pnl/daily/stocks")
    @Operation(summary = "个股每日盈亏明细", description = "返回当前用户所有持仓股票在指定日期的盈亏快照")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyStockPnl(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String accountId) {

        if (date == null) date = LocalDate.now();

        Long userId = userLookupService.getUserIdByUsername(userDetails.getUsername());

        StringBuilder sql = new StringBuilder("""
                SELECT s.*
                  FROM t_daily_pnl_stock s
                 WHERE s.snapshot_date = ?
                   AND s.broker_account_id IN (
                       SELECT DISTINCT p.account_id FROM positions p WHERE p.user_id = ?
                   )
                """);

        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(date);
        params.add(userId);

        if (symbol != null && !symbol.isBlank()) {
            sql.append(" AND s.symbol = ?");
            params.add(symbol.toUpperCase());
        }
        if (accountId != null && !accountId.isBlank()) {
            sql.append(" AND s.broker_account_id = ?");
            params.add(accountId);
        }
        sql.append(" ORDER BY s.broker_account_id, s.symbol");

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    /**
     * 查询账户在一段时间内的每日汇总盈亏（折线图数据源）。
     *
     * @param from 开始日期（包含）
     * @param to   结束日期（包含）
     */
    @GetMapping("/pnl/range")
    @Operation(summary = "日期范围盈亏趋势", description = "返回当前用户在指定日期范围内每日账户总盈亏，适合前端折线图展示")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPnlRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Long userId = userLookupService.getUserIdByUsername(userDetails.getUsername());

        String sql = """
                SELECT a.snapshot_date, SUM(a.total_pnl) AS total_pnl,
                       SUM(a.total_realized_pnl) AS total_realized_pnl,
                       SUM(a.total_market_value)  AS total_market_value
                  FROM t_daily_pnl_account a
                 WHERE a.snapshot_date BETWEEN ? AND ?
                   AND a.broker_account_id IN (
                       SELECT DISTINCT p.account_id FROM positions p WHERE p.user_id = ?
                   )
                 GROUP BY a.snapshot_date
                 ORDER BY a.snapshot_date
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, from, to, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("data", rows);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
