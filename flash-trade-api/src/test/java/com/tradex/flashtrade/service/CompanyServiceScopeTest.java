package com.tradex.flashtrade.service;

import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.dto.admin.CompanyDto;
import com.tradex.flashtrade.repository.UserRepository;
import com.tradex.flashtrade.security.TenantScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyServiceScopeTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final TenantScopeService tenantScopeService = mock(TenantScopeService.class);
    private final CompanyService companyService = new CompanyService(
            jdbc,
            mock(AuditLogger.class),
            mock(UserRepository.class),
            tenantScopeService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void companyAdminListingIsFilteredForCountAndRows() {
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(1L);
        when(jdbc.query(anyString(), anyRowMapper(), any(Object[].class))).thenReturn(List.of());

        companyService.listCompanies(0, 20, null);

        verify(jdbc).queryForObject(
                org.mockito.ArgumentMatchers.contains("WHERE c.id = ?"),
                eq(Long.class),
                eq(new Object[]{42L}));
        verify(jdbc).query(
                org.mockito.ArgumentMatchers.contains("WHERE c.id = ?"),
                anyRowMapper(),
                eq(new Object[]{42L, 20, 0}));
    }

    @Test
    void companyAdminCannotReadAnotherCompany() {
        doThrow(new AccessDeniedException("outside company scope"))
                .when(tenantScopeService)
                .assertCompanyAccess(43L);

        assertThatThrownBy(() -> companyService.getCompany(43L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @SuppressWarnings("unchecked")
    private RowMapper<CompanyDto> anyRowMapper() {
        return any(RowMapper.class);
    }
}
