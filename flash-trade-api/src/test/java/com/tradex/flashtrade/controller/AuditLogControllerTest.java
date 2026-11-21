package com.tradex.flashtrade.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tradex.flashtrade.repository.ApiCallLogRepository;
import com.tradex.flashtrade.repository.LoginLogRepository;
import com.tradex.flashtrade.repository.OperationLogRepository;
import com.tradex.flashtrade.repository.SystemEventLogRepository;
import com.tradex.flashtrade.config.SecurityConfig;
import com.tradex.flashtrade.security.JwtAuthenticationFilter;

@WebMvcTest(
    value = AuditLogController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
    )
)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperationLogRepository operationLogRepository;

    @MockitoBean
    private LoginLogRepository loginLogRepository;

    @MockitoBean
    private ApiCallLogRepository apiCallLogRepository;

    @MockitoBean
    private SystemEventLogRepository systemEventLogRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void operationLogsApplyCombinedFiltersAndTimeRange() throws Exception {
        when(operationLogRepository.search(
                eq(7L), eq("UPDATE"), eq("USER"), eq(42L), eq(200),
                eq(LocalDateTime.parse("2026-11-01T10:00:00")),
                eq(LocalDateTime.parse("2026-11-08T10:00:00")), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/admin/audit/operations")
                        .param("operatorId", "7")
                        .param("operation", " UPDATE ")
                        .param("targetType", "USER")
                        .param("targetId", "42")
                        .param("httpStatus", "200")
                        .param("startTime", "2026-11-01T10:00:00")
                        .param("endTime", "2026-11-08T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        verify(operationLogRepository).search(
                eq(7L), eq("UPDATE"), eq("USER"), eq(42L), eq(200),
                eq(LocalDateTime.parse("2026-11-01T10:00:00")),
                eq(LocalDateTime.parse("2026-11-08T10:00:00")), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void loginLogsApplyStatusUsernameAndIpTogether() throws Exception {
        when(loginLogRepository.search(
                eq("operator"), eq("FAILED"), eq("192.0.2.10"),
                isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/admin/audit/logins")
                        .param("username", "operator")
                        .param("loginStatus", "FAILED")
                        .param("requestIp", "192.0.2.10"))
                .andExpect(status().isOk());

        verify(loginLogRepository).search(
                eq("operator"), eq("FAILED"), eq("192.0.2.10"),
                isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void apiCallLogsAllowCriticalAndErrorFiltersTogether() throws Exception {
        when(apiCallLogRepository.search(
                eq(12L), eq("/api/v1/orders"), eq(500), eq(true), eq(true),
                isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/admin/audit/api-calls")
                        .param("userId", "12")
                        .param("endpoint", "/api/v1/orders")
                        .param("responseCode", "500")
                        .param("isCritical", "true")
                        .param("errorFlag", "true"))
                .andExpect(status().isOk());

        verify(apiCallLogRepository).search(
                eq(12L), eq("/api/v1/orders"), eq(500), eq(true), eq(true),
                isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void systemEventLogsCombineResolvedStateAndSeverity() throws Exception {
        when(systemEventLogRepository.search(
                eq("AUTH"), eq("HIGH"), eq("gateway"), eq(true),
                isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/admin/audit/events")
                        .param("eventType", "AUTH")
                        .param("severity", "HIGH")
                        .param("component", "gateway")
                        .param("unresolved", "true"))
                .andExpect(status().isOk());

        verify(systemEventLogRepository).search(
                eq("AUTH"), eq("HIGH"), eq("gateway"), eq(true),
                isNull(), isNull(), any(Pageable.class));
    }
}
