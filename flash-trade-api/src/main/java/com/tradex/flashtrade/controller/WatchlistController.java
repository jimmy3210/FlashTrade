package com.tradex.flashtrade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradex.flashtrade.dto.common.ApiResponse;
import com.tradex.flashtrade.dto.watchlist.AddWatchlistRequest;
import com.tradex.flashtrade.dto.watchlist.WatchlistItemResponse;
import com.tradex.flashtrade.service.WatchlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Watchlist", description = "用户自选股管理接口")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/watchlist")
    @Operation(summary = "获取自选股列表", description = "返回当前登录用户的自选股列表。")
    public ResponseEntity<ApiResponse<List<WatchlistItemResponse>>> getWatchlist(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        List<WatchlistItemResponse> watchlist = watchlistService.getWatchlist(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(watchlist));
    }

    @PostMapping("/watchlist")
    @Operation(summary = "添加自选股", description = "将 symbol 添加到当前登录用户的自选股列表。")
    public ResponseEntity<ApiResponse<WatchlistItemResponse>> addWatchlistItem(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestBody AddWatchlistRequest request) {
        WatchlistItemResponse added = watchlistService.addToWatchlist(userDetails.getUsername(), request.symbol());
        return ResponseEntity.ok(ApiResponse.success(added));
    }

    @DeleteMapping("/watchlist/{symbol}")
    @Operation(summary = "删除自选股", description = "从当前登录用户的自选股列表中删除指定 symbol。")
    public ResponseEntity<ApiResponse<Void>> removeWatchlistItem(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable String symbol) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), symbol);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
