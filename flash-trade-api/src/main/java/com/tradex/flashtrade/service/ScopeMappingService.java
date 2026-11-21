package com.tradex.flashtrade.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchRequest;
import com.tradex.flashtrade.dto.admin.ScopeMappingBatchResultDto;
import com.tradex.flashtrade.dto.admin.ScopeMappingStatusDto;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.security.TenantScopeService;

@Service
public class ScopeMappingService {

    private final JdbcTemplate jdbc;
    private final TenantScopeService tenantScopeService;
    private final AuditLogger auditLogger;

    public ScopeMappingService(
            JdbcTemplate jdbc,
            TenantScopeService tenantScopeService,
            AuditLogger auditLogger) {
        this.jdbc = jdbc;
        this.tenantScopeService = tenantScopeService;
        this.auditLogger = auditLogger;
    }

    public ScopeMappingStatusDto status() {
        tenantScopeService.requirePlatformAdmin();

        List<ScopeMappingStatusDto.UnmappedUserDto> users = jdbc.query(
                "SELECT id, username, role FROM users " +
                "WHERE company_id IS NULL OR NOT EXISTS (" +
                "  SELECT 1 FROM t_company c WHERE c.id = users.company_id AND c.is_active = 1" +
                ") ORDER BY id",
                (rs, rowNum) -> new ScopeMappingStatusDto.UnmappedUserDto(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("role")));

        List<ScopeMappingStatusDto.UnmappedAccountDto> accounts = jdbc.query(
                "SELECT a.id, a.user_id, u.username, b.broker_code, a.account_no " +
                "FROM t_broker_account a " +
                "LEFT JOIN users u ON u.id = a.user_id " +
                "LEFT JOIN t_broker b ON b.id = a.broker_id " +
                "WHERE a.user_id IS NULL OR u.company_id IS NULL OR NOT EXISTS (" +
                "  SELECT 1 FROM t_company c WHERE c.id = u.company_id AND c.is_active = 1" +
                ") ORDER BY a.id",
                (rs, rowNum) -> new ScopeMappingStatusDto.UnmappedAccountDto(
                        rs.getLong("id"),
                        rs.getObject("user_id") == null ? null : rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("broker_code"),
                        maskAccountNo(rs.getString("account_no"))));

        return new ScopeMappingStatusDto(users, accounts);
    }

    @Transactional
    public void mapUserToCompany(Long userId, Long companyId) {
        tenantScopeService.requirePlatformAdmin();
        validatePositiveId(userId, "用户");
        validateActiveCompany(companyId);

        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id, username, company_id FROM users WHERE id = ?", userId);
        if (existing.isEmpty()) {
            throw new com.tradex.flashtrade.exception.BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.RESOURCE_NOT_FOUND,
                    "用户不存在: " + userId);
        }
        Object oldCompanyId = existing.get(0).get("company_id");
        jdbc.update("UPDATE users SET company_id = ?, updated_at = NOW() WHERE id = ?",
                companyId, userId);

        auditLogger.logOperation(
                null,
                currentUsername(),
                "ASSIGN_LEGACY_USER_COMPANY",
                "USER",
                userId,
                (String) existing.get(0).get("username"),
                oldCompanyId,
                companyId,
                "人工映射旧用户公司 Scope",
                null,
                "/admin/company-mappings/users/" + userId,
                "POST",
                200,
                null);
    }

    @Transactional
    public void mapAccountOwner(Long accountId, Long userId) {
        tenantScopeService.requirePlatformAdmin();
        validatePositiveId(accountId, "券商账户");
        validatePositiveId(userId, "用户");

        List<Map<String, Object>> account = jdbc.queryForList(
                "SELECT id, user_id, account_no FROM t_broker_account WHERE id = ?", accountId);
        if (account.isEmpty()) {
            throw new com.tradex.flashtrade.exception.BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.RESOURCE_NOT_FOUND,
                    "券商账户不存在: " + accountId);
        }
        List<Map<String, Object>> user = jdbc.queryForList(
                "SELECT u.id, u.username, u.company_id, c.is_active " +
                "FROM users u JOIN t_company c ON c.id = u.company_id " +
                "WHERE u.id = ? AND c.is_active = 1",
                userId);
        if (user.isEmpty()) {
            throw new com.tradex.flashtrade.exception.BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.INVALID_ARGUMENT,
                    "目标用户不存在或尚未归属有效启用公司: " + userId);
        }

        Object oldOwnerId = account.get(0).get("user_id");
        jdbc.update("UPDATE t_broker_account SET user_id = ? WHERE id = ?", userId, accountId);

        auditLogger.logOperation(
                null,
                currentUsername(),
                "ASSIGN_LEGACY_ACCOUNT_OWNER",
                "BROKER_ACCOUNT",
                accountId,
                maskAccountNo((String) account.get(0).get("account_no")),
                oldOwnerId,
                userId,
                "人工映射旧券商账户所有者",
                null,
                "/admin/company-mappings/accounts/" + accountId,
                "POST",
                200,
                null);
    }

    @Transactional
    public ScopeMappingBatchResultDto batch(ScopeMappingBatchRequest request) {
        tenantScopeService.requirePlatformAdmin();
        if (request == null) {
            throw new com.tradex.flashtrade.exception.BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.INVALID_ARGUMENT,
                    "请求不能为空");
        }
        List<ScopeMappingBatchResultDto.MappingItemResult> users = new ArrayList<>();
        for (ScopeMappingBatchRequest.UserCompanyMapping item : request.getUsers()) {
            users.add(runUserMapping(item == null ? null : item.userId(), item == null ? null : item.companyId()));
        }
        List<ScopeMappingBatchResultDto.MappingItemResult> accounts = new ArrayList<>();
        for (ScopeMappingBatchRequest.AccountOwnerMapping item : request.getAccounts()) {
            accounts.add(runAccountMapping(item == null ? null : item.accountId(), item == null ? null : item.userId()));
        }
        return new ScopeMappingBatchResultDto(users, accounts);
    }

    private ScopeMappingBatchResultDto.MappingItemResult runUserMapping(Long userId, Long companyId) {
        try {
            mapUserToCompany(userId, companyId);
            return new ScopeMappingBatchResultDto.MappingItemResult(userId, true, "SUCCESS", "映射成功");
        } catch (RuntimeException exception) {
            return failedItem(userId, exception);
        }
    }

    private ScopeMappingBatchResultDto.MappingItemResult runAccountMapping(Long accountId, Long userId) {
        try {
            mapAccountOwner(accountId, userId);
            return new ScopeMappingBatchResultDto.MappingItemResult(accountId, true, "SUCCESS", "映射成功");
        } catch (RuntimeException exception) {
            return failedItem(accountId, exception);
        }
    }

    private ScopeMappingBatchResultDto.MappingItemResult failedItem(Long id, RuntimeException exception) {
        String code = exception instanceof BusinessException businessException
                ? businessException.getErrorCode().getCode()
                : "INVALID_ARGUMENT";
        return new ScopeMappingBatchResultDto.MappingItemResult(id, false, code, exception.getMessage());
    }

    private void validateActiveCompany(Long companyId) {
        validatePositiveId(companyId, "公司");
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM t_company WHERE id = ? AND is_active = 1",
                Integer.class,
                companyId);
        if (count == null || count == 0) {
            throw new BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.INVALID_ARGUMENT,
                    "公司不存在或未启用: " + companyId);
        }
    }

    private void validatePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    com.tradex.flashtrade.exception.ErrorCode.INVALID_ARGUMENT,
                    name + " ID 无效");
        }
    }

    private String maskAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            return null;
        }
        if (accountNo.length() <= 4) {
            return "••••";
        }
        return "••••" + accountNo.substring(accountNo.length() - 4);
    }

    private String currentUsername() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
