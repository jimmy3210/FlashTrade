package com.tradex.flashtrade.controller;

import com.tradex.flashtrade.config.SecurityConfig;
import com.tradex.flashtrade.service.AdminUserService;
import com.tradex.flashtrade.service.BrokerAccountService;
import com.tradex.flashtrade.service.ScopeMappingService;
import com.tradex.flashtrade.security.JwtAuthenticationFilter;
import com.tradex.flashtrade.security.JwtTokenProvider;
import com.tradex.flashtrade.security.UserDetailsServiceImpl;
import com.tradex.flashtrade.service.CompanyService;
import com.tradex.flashtrade.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        CompanyController.class,
        UserAdminController.class,
        BrokerAccountAdminController.class,
        ScopeMappingController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminScopeSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private BrokerAccountService brokerAccountService;

    @MockitoBean
    private ScopeMappingService scopeMappingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "COMPANY_ADMIN", "SUPER_ADMIN"})
    void companyAdminsCanReachCompanyReadRoute(String role) throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies")
                        .with(user("company-admin").roles(role)))
                .andExpect(status().isOk());

        verify(companyService).listCompanies(0, 20, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "COMPANY_ADMIN", "SUPER_ADMIN"})
    void companyAdminsCanReachScopedUserReadRoute(String role) throws Exception {
        mockMvc.perform(get("/users")
                        .with(user("company-admin").roles(role)))
                .andExpect(status().isOk());

        verify(adminUserService).listUsers();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "COMPANY_ADMIN", "SUPER_ADMIN"})
    void companyAdminsCanReachScopedBrokerAccountReadRoute(String role) throws Exception {
        mockMvc.perform(get("/admin/broker-accounts")
                        .with(user("company-admin").roles(role)))
                .andExpect(status().isOk());

        verify(brokerAccountService).listAccounts();
    }

    @Test
    void companyAdminCannotMutateCompany() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/companies/42")
                        .with(user("company-admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(companyService);
    }

    @Test
    void companyAdminCannotReadAnotherCompany() throws Exception {
        doThrow(new AccessDeniedException("outside company scope"))
                .when(companyService)
                .getCompany(43L);

        mockMvc.perform(get("/api/v1/admin/companies/43")
                        .with(user("company-admin").roles("COMPANY_ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyAdminCannotMutateUsersOrBrokerAccounts() throws Exception {
        mockMvc.perform(post("/users")
                        .with(user("company-admin").roles("COMPANY_ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/broker-accounts")
                        .with(user("company-admin").roles("COMPANY_ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminUserService, brokerAccountService);
    }

    @Test
    void unauthenticatedCannotAccessScopeMappings() throws Exception {
        mockMvc.perform(get("/admin/company-mappings"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(scopeMappingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "COMPANY_ADMIN"})
    void companyAdminCannotAccessScopeMappings(String role) throws Exception {
        mockMvc.perform(get("/admin/company-mappings")
                        .with(user("company-admin").roles(role)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(scopeMappingService);
    }

    @Test
    void superAdminCanReadAndWriteScopeMappings() throws Exception {
        mockMvc.perform(get("/admin/company-mappings")
                        .with(user("platform-admin").roles("SUPER_ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/admin/company-mappings/users/7")
                        .with(user("platform-admin").roles("SUPER_ADMIN"))
                        .contentType("application/json")
                        .content("{\"companyId\":42}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/admin/company-mappings/accounts/9")
                        .with(user("platform-admin").roles("SUPER_ADMIN"))
                        .contentType("application/json")
                        .content("{\"userId\":7}"))
                .andExpect(status().isOk());

        verify(scopeMappingService).status();
        verify(scopeMappingService).mapUserToCompany(7L, 42L);
        verify(scopeMappingService).mapAccountOwner(9L, 7L);
    }

    @Test
    void scopeMappingRejectsMissingRequiredIds() throws Exception {
        mockMvc.perform(post("/admin/company-mappings/users/7")
                        .with(user("platform-admin").roles("SUPER_ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/admin/company-mappings/accounts/9")
                        .with(user("platform-admin").roles("SUPER_ADMIN"))
                        .contentType("application/json")
                        .content("{\"userId\":-1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scopeMappingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/orders", "/positions"})
    void companyAdminCannotUseUnscopedAdminRoutes(String path) throws Exception {
        mockMvc.perform(get(path)
                        .with(user("company-admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }
}
