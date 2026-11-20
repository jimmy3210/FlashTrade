package com.tradex.flashtrade.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.tradex.flashtrade.dto.admin.BrokerAccountDto;
import com.tradex.flashtrade.dto.admin.BrokerAccountRequest;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.security.TenantScopeService;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class BrokerAccountService {

    private static final List<String> VALID_ENVIRONMENTS = List.of("LIVE", "SANDBOX");
    private static final List<String> VALID_ACCOUNT_TYPES = List.of("CASH", "MARGIN");
    private static final List<String> VALID_STATUSES = List.of("ACTIVE", "SUSPENDED", "CLOSED");

    private final JdbcTemplate jdbc;
    private final TenantScopeService tenantScopeService;

    public BrokerAccountService(JdbcTemplate jdbc, TenantScopeService tenantScopeService) {
        this.jdbc = jdbc;
        this.tenantScopeService = tenantScopeService;
    }

    public List<BrokerAccountDto> listAccounts() {
        OptionalLong companyScope = tenantScopeService.currentCompanyScope();
        String sql = accountSelect(companyScope.isPresent())
                + (companyScope.isPresent() ? "WHERE u.company_id = ? " : "")
                + "ORDER BY a.id";
        if (companyScope.isPresent()) {
            return jdbc.query(sql, this::mapRow, companyScope.getAsLong());
        }
        return jdbc.query(sql, this::mapRow);
    }

    public BrokerAccountDto getAccount(@NotNull Long accountId) {
        OptionalLong companyScope = tenantScopeService.currentCompanyScope();
        String sql = accountSelect(companyScope.isPresent())
                + "WHERE a.id = ? "
                + (companyScope.isPresent() ? "AND u.company_id = ?" : "");
        List<BrokerAccountDto> list = companyScope.isPresent()
                ? jdbc.query(sql, this::mapRow, accountId, companyScope.getAsLong())
                : jdbc.query(sql, this::mapRow, accountId);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "券商账户不存在: " + accountId);
        }
        return list.get(0);
    }

    @Transactional
    public BrokerAccountDto createAccount(BrokerAccountRequest request) {
        tenantScopeService.requirePlatformAdmin();
        validateBrokerAccountRequest(request);
        Long brokerId = resolveBrokerId(request.getBrokerName());
        validateUserId(request.getUserId());
        validateDevAccountId(request.getDevAccountId(), brokerId);

        if (existsAccountForBrokerAndNumber(brokerId, request.getAccountNo())) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,
                    "券商账户号已存在: " + request.getAccountNo());
        }

        jdbc.update("INSERT INTO t_broker_account " +
                        "(user_id, broker_id, account_no, account_type, status, dev_account_id, api_environment, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())",
                request.getUserId(), brokerId, request.getAccountNo(), request.getAccountType(),
                request.getStatus(), request.getDevAccountId(), request.getApiEnvironment());
        return getLatestAccountByBrokerAndNumber(brokerId, request.getAccountNo());
    }

    @Transactional
    public BrokerAccountDto updateAccount(Long accountId, BrokerAccountRequest request) {
        tenantScopeService.requirePlatformAdmin();
        validateBrokerAccountRequest(request);
        BrokerAccountDto existing = getAccount(accountId);
        Long brokerId = resolveBrokerId(request.getBrokerName());
        validateUserId(request.getUserId());
        validateDevAccountId(request.getDevAccountId(), brokerId);

        if (!existing.getAccountNo().equals(request.getAccountNo()) && existsAccountForBrokerAndNumber(brokerId, request.getAccountNo())) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,
                    "券商账户号已存在: " + request.getAccountNo());
        }

        jdbc.update("UPDATE t_broker_account SET user_id = ?, broker_id = ?, account_no = ?, account_type = ?, " +
                        "status = ?, dev_account_id = ?, api_environment = ? WHERE id = ?",
                request.getUserId(), brokerId, request.getAccountNo(), request.getAccountType(),
                request.getStatus(), request.getDevAccountId(), request.getApiEnvironment(), accountId);
        return getAccount(accountId);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        tenantScopeService.requirePlatformAdmin();
        BrokerAccountDto existing = getAccount(accountId);
        if (existing.getStatus() != null && existing.getStatus().equals("CLOSED")) {
            return;
        }
        jdbc.update("UPDATE t_broker_account SET status = 'CLOSED' WHERE id = ?", accountId);
    }

    /**
     * 获取用户当前绑定的券商账户
     */
    public BrokerAccountDto getCurrentAccountForUser(@NotNull Long userId) {
        OptionalLong companyScope = tenantScopeService.currentCompanyScope();
        String sql = "SELECT a.id, a.user_id, u.username, a.broker_id, b.broker_code, b.broker_name, " +
                     "a.account_no, a.account_type, a.status, a.dev_account_id, a.api_environment " +
                     "FROM t_broker_account a " +
                     "LEFT JOIN users u ON a.user_id = u.id " +
                     "LEFT JOIN t_broker b ON a.broker_id = b.id " +
                     "WHERE u.id = ? AND u.current_broker_account_id = a.id " +
                     (companyScope.isPresent() ? "AND u.company_id = ?" : "");
        List<BrokerAccountDto> list = companyScope.isPresent()
                ? jdbc.query(sql, this::mapRow, userId, companyScope.getAsLong())
                : jdbc.query(sql, this::mapRow, userId);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户未绑定券商账户: " + userId);
        }
        return list.get(0);
    }

    /**
     * 为用户切换或设置当前绑定的券商账户
     * 同一个账户只能被一个用户绑定，自动解绑其他用户的绑定
     */
    @Transactional
    public BrokerAccountDto setCurrentAccountForUser(@NotNull Long userId, @NotNull Long accountId) {
        tenantScopeService.requirePlatformAdmin();
        validateUserId(userId);
        BrokerAccountDto account = getAccount(accountId);

        // 验证账户必须为 ACTIVE 状态
        if (account.getStatus() != null && !account.getStatus().equals("ACTIVE")) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "券商账户非活跃状态，无法绑定: " + account.getStatus());
        }

        // 检查该账户是否已被其他用户绑定
        List<Map<String, Object>> boundUsers = jdbc.queryForList(
                "SELECT id FROM users WHERE current_broker_account_id = ? AND id != ?",
                accountId, userId);
        if (!boundUsers.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,
                    "该券商账户已被其他用户绑定");
        }

        // 解绑当前用户的旧账户
        jdbc.update("UPDATE users SET current_broker_account_id = NULL WHERE id = ?", userId);

        // 绑定新账户
        jdbc.update("UPDATE users SET current_broker_account_id = ? WHERE id = ?", accountId, userId);

        return account;
    }

    private void validateBrokerAccountRequest(BrokerAccountRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "请求不能为空");
        }
        String env = normalizeEnvironment(request.getApiEnvironment());
        if (!VALID_ENVIRONMENTS.contains(env)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "无效的 API 环境：" + request.getApiEnvironment());
        }
        request.setApiEnvironment(env);

        String type = normalizeAccountType(request.getAccountType());
        if (!VALID_ACCOUNT_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "无效的账户类型：" + request.getAccountType());
        }
        request.setAccountType(type);

        String status = normalizeStatus(request.getStatus());
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "无效的账户状态：" + request.getStatus());
        }
        request.setStatus(status);
    }

    private String normalizeEnvironment(String environment) {
        if (environment == null || environment.isBlank()) {
            return "LIVE";
        }
        return environment.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return "CASH";
        }
        return accountType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private Long resolveBrokerId(String brokerName) {
        if (brokerName == null || brokerName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "券商名称不能为空");
        }
        String normalized = brokerName.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> brokers = jdbc.queryForList(
                "SELECT id FROM t_broker WHERE LOWER(broker_code) = ? OR LOWER(broker_name) = ? LIMIT 1",
                normalized, normalized);
        if (brokers.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "未知的券商: " + brokerName);
        }
        return ((Number) brokers.get(0).get("id")).longValue();
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "用户 ID 不能为空");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在: " + userId);
        }
    }

    private void validateDevAccountId(Long devAccountId, Long brokerId) {
        if (devAccountId == null) {
            return;
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM t_broker_dev_account WHERE id = ? AND broker_id = ?", Integer.class, devAccountId, brokerId);
        if (count == null || count == 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT,
                    "开发者账号不存在或不属于指定券商: " + devAccountId);
        }
    }

    private boolean existsAccountForBrokerAndNumber(Long brokerId, String accountNo) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM t_broker_account WHERE broker_id = ? AND account_no = ?", Integer.class,
                brokerId, accountNo);
        return count != null && count > 0;
    }

    private BrokerAccountDto getLatestAccountByBrokerAndNumber(Long brokerId, String accountNo) {
        String sql = "SELECT a.id, a.user_id, u.username, a.broker_id, b.broker_code, b.broker_name, " +
                     "a.account_no, a.account_type, a.status, a.dev_account_id, a.api_environment, " +
                     "u_bind.username AS current_binding_user " +
                     "FROM t_broker_account a " +
                     "LEFT JOIN users u ON a.user_id = u.id " +
                     "LEFT JOIN t_broker b ON a.broker_id = b.id " +
                     "LEFT JOIN users u_bind ON a.id = u_bind.current_broker_account_id " +
                     "WHERE a.broker_id = ? AND a.account_no = ? ORDER BY a.id DESC LIMIT 1";
        List<BrokerAccountDto> list = jdbc.query(sql, this::mapRow, brokerId, accountNo);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建券商账户后未找到记录");
        }
        return list.get(0);
    }

    private String accountSelect(boolean restrictBindingToOwnerCompany) {
        return "SELECT a.id, a.user_id, u.username, a.broker_id, b.broker_code, b.broker_name, " +
               "a.account_no, a.account_type, a.status, a.dev_account_id, a.api_environment, " +
               "u_bind.username AS current_binding_user " +
               "FROM t_broker_account a " +
               "LEFT JOIN users u ON a.user_id = u.id " +
               "LEFT JOIN t_broker b ON a.broker_id = b.id " +
               "LEFT JOIN users u_bind ON a.id = u_bind.current_broker_account_id " +
               (restrictBindingToOwnerCompany ? "AND u_bind.company_id = u.company_id " : "");
    }

    @SuppressWarnings("unused")
    private BrokerAccountDto mapRow(ResultSet rs, int ignoredRowNum) throws SQLException {
        BrokerAccountDto dto = new BrokerAccountDto();
        dto.setId(rs.getLong("id"));
        dto.setUserId(rs.getLong("user_id"));
        dto.setUsername(rs.getString("username"));
        dto.setBrokerId(rs.getLong("broker_id"));
        dto.setBrokerCode(rs.getString("broker_code"));
        dto.setBrokerName(rs.getString("broker_name"));
        dto.setAccountNo(rs.getString("account_no"));
        dto.setAccountType(rs.getString("account_type"));
        dto.setStatus(rs.getString("status"));
        dto.setDevAccountId(rs.getObject("dev_account_id") != null ? rs.getLong("dev_account_id") : null);
        dto.setApiEnvironment(rs.getString("api_environment"));
        dto.setCurrentBindingUsername(rs.getString("current_binding_user"));
        return dto;
    }
}
