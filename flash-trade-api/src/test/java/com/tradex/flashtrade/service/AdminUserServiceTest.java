package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.admin.UserAdminDto;
import com.tradex.flashtrade.dto.admin.UserCreateRequest;
import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.security.TenantScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TenantScopeService tenantScopeService;

    @Mock
    private AuditLogger auditLogger;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateUser_preservesExistingRole() throws Exception {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.empty());
        when(jdbc.queryForObject(contains("FROM t_company"), eq(Integer.class), eq(7L))).thenReturn(1);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("user_id")).thenReturn(12L);
        when(resultSet.getString("username")).thenReturn("operator");
        when(resultSet.getString("role")).thenReturn("ROLE_ADMIN");
        when(resultSet.getBoolean("enabled")).thenReturn(true);
        when(resultSet.getString("account_user_id")).thenReturn("account-1");
        when(resultSet.getString("broker_name")).thenReturn("SCHWAB");
        when(resultSet.getObject("company_id")).thenReturn(5L, 7L);
        when(resultSet.getString("company_name")).thenReturn("Old Company", "New Company");
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<UserAdminDto>>any(), eq(12L)))
                .thenAnswer(invocation -> {
                    RowMapper<UserAdminDto> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });
        when(jdbc.queryForList(contains("FROM t_broker"), eq("schwab"), eq("schwab")))
                .thenReturn(List.of(Map.of("id", 4L, "broker_code", "SCHWAB", "broker_name", "Schwab")));
        when(jdbc.queryForObject(contains("FROM t_broker_account"), eq(Integer.class), eq(12L)))
                .thenReturn(1);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("operator-updated");
        request.setBrokerName("Schwab");
        request.setAccountUserId("account-1");
        request.setCompanyId(7L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "platform-admin",
                        "credentials-not-used",
                        List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));

        service.updateUser(12L, request);

        ArgumentCaptor<Object[]> arguments = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).update(contains("UPDATE users"), arguments.capture());
        assertThat(arguments.getValue()).containsExactly("operator-updated", 7L, "ROLE_ADMIN", true, 12L);
        verify(jdbc).update(
                contains("api_environment = COALESCE(api_environment, 'SANDBOX')"),
                eq(4L), eq("account-1"), eq(12L));
        verify(passwordEncoder, never()).encode(anyString());
        verify(auditLogger).logOperation(
                isNull(),
                eq("platform-admin"),
                eq("ASSIGN_USER_COMPANY"),
                eq("USER"),
                eq(12L),
                eq("operator"),
                eq(5L),
                eq(7L),
                eq("更新用户公司 Scope"),
                isNull(),
                eq("/users/12"),
                eq("PUT"),
                eq(200),
                isNull());
    }

    @Test
    void listUsersFiltersRowsToCurrentCompany() {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<UserAdminDto>>any(), any(Object[].class)))
                .thenReturn(List.of());

        service.listUsers();

        verify(jdbc).query(
                contains("WHERE u.company_id = ?"),
                ArgumentMatchers.<RowMapper<UserAdminDto>>any(),
                eq(new Object[]{42L}));
    }

    @Test
    void getUserHidesUsersOutsideCurrentCompany() {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<UserAdminDto>>any(), eq(12L), eq(42L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getUser(12L))
                .isInstanceOf(BusinessException.class);

        verify(jdbc).query(
                contains("WHERE u.id = ? AND u.company_id = ?"),
                ArgumentMatchers.<RowMapper<UserAdminDto>>any(),
                eq(12L),
                eq(42L));
    }

    @Test
    void companyAdminCannotUseUserMutationService() {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);
        doThrow(new AccessDeniedException("platform administrator required"))
                .when(tenantScopeService)
                .requirePlatformAdmin();

        assertThatThrownBy(() -> service.changePassword(12L, "new-password"))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(jdbc, passwordEncoder);
    }

    @Test
    void createUserRejectsInactiveCompanyBeforeWriting() {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);
        when(jdbc.queryForObject(contains("FROM t_company"), eq(Integer.class), eq(9L))).thenReturn(0);
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("new-user");
        request.setAccountUserId("account-9");
        request.setBrokerName("SCHWAB");
        request.setPassword("valid-password");
        request.setCompanyId(9L);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(passwordEncoder, auditLogger);
        verify(jdbc, never()).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void createUserRejectsMissingCompany() {
        AdminUserService service = new AdminUserService(jdbc, passwordEncoder, tenantScopeService, auditLogger);

        assertThatThrownBy(() -> service.createUser(new UserCreateRequest()))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(jdbc, passwordEncoder, auditLogger);
    }
}
