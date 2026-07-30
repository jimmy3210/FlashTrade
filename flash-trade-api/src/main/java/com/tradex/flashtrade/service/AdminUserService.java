package com.tradex.flashtrade.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.tradex.flashtrade.dto.admin.UserAdminDto;
import com.tradex.flashtrade.dto.admin.UserCreateRequest;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class AdminUserService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAdminDto> listUsers() {
        String sql = "SELECT u.id AS user_id, u.username, u.role, u.enabled, " +
                     "a.account_no AS account_user_id, b.broker_code AS broker_name " +
                     "FROM users u " +
                     "LEFT JOIN t_broker_account a ON a.id = (" +
                     "  SELECT id FROM t_broker_account WHERE user_id = u.id AND status = 'ACTIVE' ORDER BY id LIMIT 1" +
                     ") " +
                     "LEFT JOIN t_broker b ON a.broker_id = b.id " +
                     "ORDER BY u.id";
        return jdbc.query(sql, (rs, rowNum) -> mapUserRow(rs));
    }

    public UserAdminDto getUser(@NotNull Long userId) {
        String sql = "SELECT u.id AS user_id, u.username, u.role, u.enabled, " +
                     "a.account_no AS account_user_id, b.broker_code AS broker_name " +
                     "FROM users u " +
                     "LEFT JOIN t_broker_account a ON a.id = (" +
                     "  SELECT id FROM t_broker_account WHERE user_id = u.id AND status = 'ACTIVE' ORDER BY id LIMIT 1" +
                     ") " +
                     "LEFT JOIN t_broker b ON a.broker_id = b.id " +
                     "WHERE u.id = ?";
        List<UserAdminDto> list = jdbc.query(sql, (rs, rowNum) -> mapUserRow(rs), userId);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在: " + userId);
        }
        return list.get(0);
    }

    @Transactional
    public UserAdminDto createUser(UserCreateRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "用户名已存在: " + request.getUsername());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "密码不能为空");
        }

        Long brokerId = resolveBrokerId(request.getBrokerName());
        String hashed = passwordEncoder.encode(request.getPassword());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, password_hash, role, enabled, created_at, updated_at) " +
                    "VALUES (?, ?, ?, 1, NOW(), NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getUsername());
            ps.setString(2, hashed);
            ps.setString(3, "ROLE_TRADER");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建用户失败");
        }
        Long userId = key.longValue();
        createOrUpdateBrokerAccount(userId, brokerId, request.getAccountUserId());
        return getUser(userId);
    }

    @Transactional
    public UserAdminDto updateUser(Long userId, UserCreateRequest request) {
        UserAdminDto existing = getUser(userId);
        Long brokerId = resolveBrokerId(request.getBrokerName());
        boolean enabled = existing.getDeleteFlag() == 0;

        StringBuilder sql = new StringBuilder("UPDATE users SET username = ?, role = ?, enabled = ?, updated_at = NOW()");
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            sql.append(", password_hash = ?");
        }
        sql.append(" WHERE id = ?");

        String username = request.getUsername();
        String role = "ROLE_TRADER";
        String password = request.getPassword();
        if (password != null && !password.isBlank()) {
            Object[] args = new Object[]{username, role, enabled, passwordEncoder.encode(password), userId};
            jdbc.update(sql.toString(), args);
        } else {
            Object[] args = new Object[]{username, role, enabled, userId};
            jdbc.update(sql.toString(), args);
        }

        createOrUpdateBrokerAccount(userId, brokerId, request.getAccountUserId());
        return getUser(userId);
    }

    @Transactional
    public void changePassword(Long userId, String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "密码不能为空");
        }
        if (!existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在: " + userId);
        }
        jdbc.update("UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?", passwordEncoder.encode(password), userId);
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        if (!existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在: " + userId);
        }
        jdbc.update("UPDATE users SET enabled = ?, updated_at = NOW() WHERE id = ?", enabled, userId);
    }

    private boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM users WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    private boolean existsByUserId(Long userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM users WHERE id = ?", Integer.class, userId);
        return count != null && count > 0;
    }

    private Long resolveBrokerId(String brokerName) {
        if (brokerName == null || brokerName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "券商名称不能为空");
        }
        String normalized = brokerName.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> brokers = jdbc.queryForList(
                "SELECT id, broker_code, broker_name FROM t_broker " +
                "WHERE LOWER(broker_code) = ? OR LOWER(broker_name) = ? LIMIT 1",
                normalized, normalized);
        if (brokers.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "未知的券商: " + brokerName);
        }
        return ((Number) brokers.get(0).get("id")).longValue();
    }

    private void createOrUpdateBrokerAccount(Long userId, Long brokerId, String accountUserId) {
        if (accountUserId == null || accountUserId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "账户名不能为空");
        }
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM t_broker_account WHERE user_id = ?", Integer.class, userId);
        if (count != null && count > 0) {
            jdbc.update("UPDATE t_broker_account SET broker_id = ?, account_no = ?, status = 'ACTIVE', api_environment = COALESCE(api_environment, 'LIVE') WHERE user_id = ?",
                    brokerId, accountUserId, userId);
        } else {
            jdbc.update("INSERT INTO t_broker_account (user_id, broker_id, account_no, account_type, status, dev_account_id, api_environment, created_at) " +
                    "VALUES (?, ?, ?, 'CASH', 'ACTIVE', NULL, 'LIVE', NOW())",
                    userId, brokerId, accountUserId);
        }
    }

    private UserAdminDto mapUserRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        UserAdminDto dto = new UserAdminDto();
        dto.setUserId(rs.getLong("user_id"));
        dto.setUsername(rs.getString("username"));
        dto.setRole(rs.getString("role"));
        dto.setDeleteFlag(rs.getBoolean("enabled") ? 0 : 1);
        dto.setAccountUserId(rs.getString("account_user_id"));
        dto.setBrokerName(rs.getString("broker_name"));
        return dto;
    }
}
