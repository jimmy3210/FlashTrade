package com.tradex.flashtrade.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantScopeServiceTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TenantScopeService tenantScopeService = new TenantScopeService(jdbc);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void platformAdminHasGlobalCompanyScopeWithoutDatabaseLookup() {
        authenticate("platform-admin", "ROLE_SUPER_ADMIN");

        assertThat(tenantScopeService.currentCompanyScope()).isEmpty();
        verify(jdbc, never()).queryForObject(anyString(), eq(Long.class), any(Object[].class));
    }

    @Test
    void legacyAdminGetsOnlyExplicitCompanyScope() {
        authenticate("company-admin", "ROLE_ADMIN");
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(42L);

        assertThat(tenantScopeService.currentCompanyScope()).isEqualTo(OptionalLong.of(42L));
        verify(jdbc).queryForObject(
                org.mockito.ArgumentMatchers.contains("c.is_active = 1"),
                eq(Long.class),
                eq("company-admin"));
    }

    @Test
    void companyAdminWithoutCompanyMappingIsDenied() {
        authenticate("unscoped-admin", "ROLE_COMPANY_ADMIN");
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(tenantScopeService::currentCompanyScope)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void companyAdminOfInactiveCompanyIsDenied() {
        authenticate("inactive-company-admin", "ROLE_COMPANY_ADMIN");
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(tenantScopeService::currentCompanyScope)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void nonAdminCannotResolveCompanyScope() {
        authenticate("trader", "ROLE_TRADER");

        assertThatThrownBy(tenantScopeService::currentCompanyScope)
                .isInstanceOf(AccessDeniedException.class);
        verify(jdbc, never()).queryForObject(anyString(), eq(Long.class), any(Object[].class));
    }

    private void authenticate(String username, String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "credentials-not-used",
                        List.of(new SimpleGrantedAuthority(authority))));
    }
}
