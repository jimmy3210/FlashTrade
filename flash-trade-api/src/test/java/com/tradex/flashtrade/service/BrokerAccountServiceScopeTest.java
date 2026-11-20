package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.admin.BrokerAccountDto;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.security.TenantScopeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerAccountServiceScopeTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private TenantScopeService tenantScopeService;

    @Test
    void accountListIsFilteredByOwnerCompanyAndBindingIdentity() {
        BrokerAccountService service = new BrokerAccountService(jdbc, tenantScopeService);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(), eq(42L)))
                .thenReturn(List.of());

        service.listAccounts();

        verify(jdbc).query(
                contains("WHERE u.company_id = ?"),
                ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(),
                eq(42L));
        verify(jdbc).query(
                contains("u_bind.company_id = u.company_id"),
                ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(),
                eq(42L));
    }

    @Test
    void accountDetailsOutsideCompanyScopeAreNotFound() {
        BrokerAccountService service = new BrokerAccountService(jdbc, tenantScopeService);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(), eq(91L), eq(42L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getAccount(91L))
                .isInstanceOf(BusinessException.class);

        verify(jdbc).query(
                contains("WHERE a.id = ? AND u.company_id = ?"),
                ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(),
                eq(91L),
                eq(42L));
    }

    @Test
    void currentAccountLookupIsFilteredByOwnerCompany() {
        BrokerAccountService service = new BrokerAccountService(jdbc, tenantScopeService);
        when(tenantScopeService.currentCompanyScope()).thenReturn(OptionalLong.of(42L));
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(), eq(12L), eq(42L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getCurrentAccountForUser(12L))
                .isInstanceOf(BusinessException.class);

        verify(jdbc).query(
                contains("WHERE u.id = ? AND u.current_broker_account_id = a.id AND u.company_id = ?"),
                ArgumentMatchers.<RowMapper<BrokerAccountDto>>any(),
                eq(12L),
                eq(42L));
    }

    @Test
    void companyAdminCannotMutateBrokerAccountsThroughService() {
        BrokerAccountService service = new BrokerAccountService(jdbc, tenantScopeService);
        doThrow(new AccessDeniedException("platform administrator required"))
                .when(tenantScopeService)
                .requirePlatformAdmin();

        assertThatThrownBy(() -> service.deleteAccount(91L))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(jdbc);
    }
}
