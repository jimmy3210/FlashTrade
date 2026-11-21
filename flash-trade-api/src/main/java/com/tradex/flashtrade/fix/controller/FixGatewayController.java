package com.tradex.flashtrade.fix.controller;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.fix.core.FixGatewayService;
import com.tradex.flashtrade.fix.core.FixSessionManager;
import com.tradex.flashtrade.logging.TraceIdSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * FIX Gateway 管理 REST API
 *
 * FIX セッションの起動・停止・状態確認を REST で提供する。
 * 管理者権限（ROLE_ADMIN）が必要。
 */
@RestController
@RequestMapping("/api/fix")
@Tag(name = "FIX Gateway", description = "FIX プロトコルゲートウェイ管理 API")
@ConditionalOnBean(FixGatewayService.class)
public class FixGatewayController {

    private static final Logger log = LoggerFactory.getLogger(FixGatewayController.class);

    private final FixGatewayService gatewayService;
    private final FixSessionManager sessionManager;

    public FixGatewayController(FixGatewayService gatewayService, FixSessionManager sessionManager) {
        this.gatewayService = gatewayService;
        this.sessionManager = sessionManager;
    }

    // -------------------------------------------------------------------------
    // セッション起動 / 停止
    // -------------------------------------------------------------------------

    @PostMapping("/sessions/start-all")
    @Operation(summary = "全セッション起動", description = "enabled=true の全 FIX セッションを起動する")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> startAll() {
        log.info("REST: FIX 全セッション起動要求");
        gatewayService.startAll();
        return ResponseEntity.ok(ApiResponse.success("全 FIX セッションを起動しました", null));
    }

    @PostMapping("/sessions/stop-all")
    @Operation(summary = "全セッション停止", description = "全 FIX セッションを安全に停止する")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> stopAll() {
        log.info("REST: FIX 全セッション停止要求");
        gatewayService.stopAll();
        return ResponseEntity.ok(ApiResponse.success("全 FIX セッションを停止しました", null));
    }

    @PostMapping("/sessions/{key}/start")
    @Operation(summary = "指定セッション起動", description = "セッションキーで指定した FIX セッションを起動する")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> startSession(
            @Parameter(description = "セッションキー（例: test-local）")
            @PathVariable String key
    ) {
        log.info("REST: FIX セッション起動要求: key={}", key);
        gatewayService.start(key);
        return ResponseEntity.ok(ApiResponse.success("FIX セッション [" + key + "] を起動しました", null));
    }

    @PostMapping("/sessions/{key}/stop")
    @Operation(summary = "指定セッション停止", description = "セッションキーで指定した FIX セッションを停止する")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> stopSession(
            @Parameter(description = "セッションキー（例: test-local）")
            @PathVariable String key
    ) {
        log.info("REST: FIX セッション停止要求: key={}", key);
        gatewayService.stop(key);
        return ResponseEntity.ok(ApiResponse.success("FIX セッション [" + key + "] を停止しました", null));
    }

    // -------------------------------------------------------------------------
    // 状態確認
    // -------------------------------------------------------------------------

    @GetMapping("/sessions")
    @Operation(summary = "全セッション状態一覧", description = "全 FIX セッションの状態と稼働中フラグを返す")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GatewayStatusResponse>> getAllSessions() {
        Map<String, Boolean> runningMap = gatewayService.getAllRunningStatus();
        List<SessionInfo> sessionInfos = runningMap.entrySet().stream()
                .map(e -> new SessionInfo(e.getKey(), e.getValue()))
                .toList();

        long loggedOnCount = sessionManager.getLoggedOnCount();

        return ResponseEntity.ok(ApiResponse.success(new GatewayStatusResponse(
                sessionInfos,
                loggedOnCount,
                sessionInfos.size(),
                Instant.now()
        )));
    }

    @GetMapping("/sessions/{key}/status")
    @Operation(summary = "指定セッション状態", description = "指定セッションキーの状態を返す")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SessionInfo>> getSessionStatus(
            @Parameter(description = "セッションキー（例: test-local）")
            @PathVariable String key
    ) {
        boolean running = gatewayService.isRunning(key);
        return ResponseEntity.ok(ApiResponse.success(new SessionInfo(key, running)));
    }

    // -------------------------------------------------------------------------
    // レスポンスモデル
    // -------------------------------------------------------------------------

    public record SessionInfo(String key, boolean running) {}

    public record GatewayStatusResponse(
            List<SessionInfo> sessions,
            long loggedOnCount,
            int totalCount,
            Instant timestamp
    ) {}

    // -------------------------------------------------------------------------
    // 例外ハンドラー
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleIllegalArgument(
            IllegalArgumentException e, HttpServletResponse response) {
        log.warn("FIX Gateway API 不正引数: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(
                ErrorCode.INVALID_ARGUMENT.getCode(),
                ErrorCode.INVALID_ARGUMENT.getMessage(),
                TraceIdSupport.ensureTraceId(response)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ApiResponse.ErrorData>> handleRuntimeException(
            RuntimeException e, HttpServletResponse response) {
        log.error("FIX Gateway API エラー: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                TraceIdSupport.ensureTraceId(response)));
    }
}
