package com.tradex.flashtrade.security;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.OptionalLong;

@Service
public class TenantScopeService {

    private final JdbcTemplate jdbc;

    public TenantScopeService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public OptionalLong currentCompanyScope() {
        Authentication authentication = currentAuthentication();
        if (hasAuthority(authentication, "ROLE_SUPER_ADMIN")) {
            return OptionalLong.empty();
        }
        if (!hasAuthority(authentication, "ROLE_ADMIN")
                && !hasAuthority(authentication, "ROLE_COMPANY_ADMIN")) {
            throw new AccessDeniedException("Company administration role is required");
        }

        try {
            Long companyId = jdbc.queryForObject(
                    """
                    SELECT u.company_id
                    FROM users u
                    JOIN t_company c ON c.id = u.company_id AND c.is_active = 1
                    WHERE u.username = ?
                      AND u.enabled = 1
                      AND u.role IN ('ROLE_ADMIN', 'ROLE_COMPANY_ADMIN')
                    """,
                    Long.class,
                    authentication.getName());
            if (companyId == null) {
                throw new AccessDeniedException("Company scope is not assigned");
            }
            return OptionalLong.of(companyId);
        } catch (EmptyResultDataAccessException exception) {
            throw new AccessDeniedException("Company scope is not assigned", exception);
        }
    }

    public void assertCompanyAccess(Long companyId) {
        OptionalLong scope = currentCompanyScope();
        if (scope.isPresent() && scope.getAsLong() != companyId) {
            throw new AccessDeniedException("Company is outside the current user's scope");
        }
    }

    public void requirePlatformAdmin() {
        if (!hasAuthority(currentAuthentication(), "ROLE_SUPER_ADMIN")) {
            throw new AccessDeniedException("Platform administrator role is required");
        }
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authentication;
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
