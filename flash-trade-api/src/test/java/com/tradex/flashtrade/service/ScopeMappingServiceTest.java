package com.tradex.flashtrade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchRequest;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchResultDto;
import com.tradex.flashtrade.dto.admin.ScopeMappingStatusDto;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.security.TenantScopeService;

@ExtendWith(MockitoExtension.class)
class ScopeMappingServiceTest {

    @Mock
    private JdbcTemplate jdbc;
    @Mock
    private TenantScopeService tenantScopeService;
    @Mock
    private AuditLogger auditLogger;

    private ScopeMappingService service;

    @BeforeEach
    void setUp() {
        service = new ScopeMappingService(jdbc, tenantScopeService, auditLogger);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "platform-admin",
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));
    }

    @Test
    void mapUserRequiresActiveCompany() {
        when(jdbc.queryForObject(
                org.mockito.ArgumentMatchers.contains("FROM t_company"),
                eq(Integer.class),
                eq(42L))).thenReturn(0);

        assertThatThrownBy(() -> service.mapUserToCompany(7L, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未启用");
    }

    @Test
    void mapUserUpdatesCompanyAndWritesAudit() {
        when(jdbc.queryForObject(
                org.mockito.ArgumentMatchers.contains("FROM t_company"),
                eq(Integer.class),
                eq(42L))).thenReturn(1);
        when(jdbc.queryForList("SELECT id, username, company_id FROM users WHERE id = ?", 7L))
                .thenReturn(List.of(Map.of("id", 7L, "username", "trader", "company_id", 0L)));

        service.mapUserToCompany(7L, 42L);

        verify(jdbc).update(
                "UPDATE users SET company_id = ?, updated_at = NOW() WHERE id = ?",
                42L,
                7L);
        verify(auditLogger).logOperation(
                eq(null),
                eq("platform-admin"),
                eq("ASSIGN_LEGACY_USER_COMPANY"),
                eq("USER"),
                eq(7L),
                eq("trader"),
                eq(0L),
                eq(42L),
                eq("人工映射旧用户公司 Scope"),
                eq(null),
                eq("/admin/company-mappings/users/7"),
                eq("POST"),
                eq(200),
                eq(null));
    }

    @Test
    void mapAccountRejectsUserWithoutActiveCompany() {
        when(jdbc.queryForList(
                "SELECT id, user_id, account_no FROM t_broker_account WHERE id = ?", 9L))
                .thenReturn(List.of(Map.of("id", 9L, "account_no", "12345678")));
        when(jdbc.queryForList(
                org.mockito.ArgumentMatchers.contains("FROM users u JOIN t_company c"),
                eq(12L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.mapAccountOwner(9L, 12L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("有效启用公司");
    }

    @Test
    void mapAccountUpdatesOwnerAndWritesAuditWhenUserHasActiveCompany() {
        when(jdbc.queryForList(
                "SELECT id, user_id, account_no FROM t_broker_account WHERE id = ?", 9L))
                .thenReturn(List.of(Map.of(
                        "id", 9L,
                        "user_id", 3L,
                        "account_no", "12345678")));
        when(jdbc.queryForList(
                org.mockito.ArgumentMatchers.contains("FROM users u JOIN t_company c"),
                eq(12L)))
                .thenReturn(List.of(Map.of(
                        "id", 12L,
                        "username", "trader",
                        "company_id", 42L,
                        "is_active", 1)));

        service.mapAccountOwner(9L, 12L);

        verify(jdbc).update(
                "UPDATE t_broker_account SET user_id = ? WHERE id = ?",
                12L,
                9L);
        verify(auditLogger).logOperation(
                eq(null),
                eq("platform-admin"),
                eq("ASSIGN_LEGACY_ACCOUNT_OWNER"),
                eq("BROKER_ACCOUNT"),
                eq(9L),
                eq("••••5678"),
                eq(3L),
                eq(12L),
                eq("人工映射旧券商账户所有者"),
                eq(null),
                eq("/admin/company-mappings/accounts/9"),
                eq("POST"),
                eq(200),
                eq(null));
    }

    @Test
    void batchReturnsIndividualSuccessAndFailure() {
        ScopeMappingBatchRequest request = new ScopeMappingBatchRequest();
        request.setUsers(List.of(
                new ScopeMappingBatchRequest.UserCompanyMapping(7L, 42L),
                new ScopeMappingBatchRequest.UserCompanyMapping(-1L, 42L)));

        when(jdbc.queryForObject(
                org.mockito.ArgumentMatchers.contains("FROM t_company"),
                eq(Integer.class),
                eq(42L))).thenReturn(1);
        when(jdbc.queryForList("SELECT id, username, company_id FROM users WHERE id = ?", 7L))
                .thenReturn(List.of(Map.of("id", 7L, "username", "trader")));

        ScopeMappingBatchResultDto result = service.batch(request);

        assertThat(result.users()).hasSize(2);
        assertThat(result.users().get(0).success()).isTrue();
        assertThat(result.users().get(1).success()).isFalse();
    }

    @Test
    void statusReturnsUnmappedUsersAndMaskedAccounts() {
        when(jdbc.query(
                org.mockito.ArgumentMatchers.contains("WHERE company_id IS NULL"),
                any(RowMapper.class))).thenReturn(List.of());
        when(jdbc.query(
                org.mockito.ArgumentMatchers.contains("FROM t_broker_account a"),
                any(RowMapper.class))).thenReturn(List.of());

        ScopeMappingStatusDto result = service.status();

        assertThat(result.unmappedUsers()).isEmpty();
        assertThat(result.unmappedAccounts()).isEmpty();
    }
}
