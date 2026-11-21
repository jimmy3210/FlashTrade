package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.admin.CompanyCreateRequest;
import com.tradex.flashtrade.dto.admin.CompanyDto;
import com.tradex.flashtrade.dto.admin.CompanyStatusRequest;
import com.tradex.flashtrade.dto.admin.CompanyUpdateRequest;
import com.tradex.flashtrade.audit.AuditLogger;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.repository.UserRepository;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.security.TenantScopeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@Service
public class CompanyService {

    private final JdbcTemplate jdbc;
    private final AuditLogger auditLogger;
    private final UserRepository userRepository;
    private final TenantScopeService tenantScopeService;

    public CompanyService(
            JdbcTemplate jdbc,
            AuditLogger auditLogger,
            UserRepository userRepository,
            TenantScopeService tenantScopeService) {
        this.jdbc = jdbc;
        this.auditLogger = auditLogger;
        this.userRepository = userRepository;
        this.tenantScopeService = tenantScopeService;
    }

    public List<CompanyDto> listCompanies() {
        OptionalLong scope = tenantScopeService.currentCompanyScope();
        String sql = """
                SELECT c.id,
                       c.name,
                       c.is_active,
                       c.created_at,
                       (SELECT COUNT(*) FROM t_group g WHERE g.company_id = c.id) AS group_count
                FROM t_company c
                """ + (scope.isPresent() ? "WHERE c.id = ?\n" : "") + "ORDER BY c.id";
        if (scope.isPresent()) {
            return jdbc.query(sql, this::mapCompanyRow, scope.getAsLong());
        }
        return jdbc.query(sql, this::mapCompanyRow);
    }

    /**
     * 分页查询公司，支持按名称模糊搜索（q）。page 从 0 开始。
     */
    public com.tradex.flashtrade.dto.admin.CompanyListResponse listCompanies(int page, int size, String q) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        int offset = page * size;

        OptionalLong scope = tenantScopeService.currentCompanyScope();
        List<String> filters = new ArrayList<>();
        List<Object> countParameters = new ArrayList<>();
        if (scope.isPresent()) {
            filters.add("c.id = ?");
            countParameters.add(scope.getAsLong());
        }
        if (q != null && !q.isBlank()) {
            filters.add("c.name LIKE ?");
            String like = "%" + q.trim() + "%";
            countParameters.add(like);
        }

        String baseWhere = filters.isEmpty() ? "" : "WHERE " + String.join(" AND ", filters);
        String countSql = String.format("SELECT COUNT(1) FROM t_company c %s", baseWhere);
        Long count = jdbc.queryForObject(countSql, Long.class, countParameters.toArray());
        if (count == null) {
            throw new IllegalStateException("Company count query returned no result");
        }
        long total = count;

        String listSql = String.format("""
                SELECT c.id,
                       c.name,
                       c.is_active,
                       c.created_at,
                       (SELECT COUNT(*) FROM t_group g WHERE g.company_id = c.id) AS group_count
                FROM t_company c
                %s
                ORDER BY c.id
                LIMIT ? OFFSET ?
                """, baseWhere);

        List<Object> listParameters = new ArrayList<>(countParameters);
        listParameters.add(size);
        listParameters.add(offset);
        List<CompanyDto> items = jdbc.query(listSql, this::mapCompanyRow, listParameters.toArray());

        return new com.tradex.flashtrade.dto.admin.CompanyListResponse(items, total, page, size);
    }

    public CompanyDto getCompany(Long companyId) {
        tenantScopeService.assertCompanyAccess(companyId);
        String sql = """
                SELECT c.id,
                       c.name,
                       c.is_active,
                       c.created_at,
                       (SELECT COUNT(*) FROM t_group g WHERE g.company_id = c.id) AS group_count
                FROM t_company c
                WHERE c.id = ?
                """;
        List<CompanyDto> list = jdbc.query(sql, this::mapCompanyRow, companyId);
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公司不存在: " + companyId);
        }
        return list.get(0);
    }

    @Transactional
    public CompanyDto createCompany(CompanyCreateRequest request) {
        tenantScopeService.requirePlatformAdmin();
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "公司名称不能为空");
        }
        String name = request.getName().trim();
        if (existsByName(name)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "公司名称已存在: " + name);
        }

        Boolean active = request.getIsActive() == null ? true : request.getIsActive();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO t_company (name, is_active, created_at) VALUES (?, ?, NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setBoolean(2, active);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建公司失败");
        }
        CompanyDto created = getCompany(key.longValue());

        // Audit log
        Long operatorId = getCurrentUserId().orElse(null);
        String operatorUsername = getCurrentUsername();
        auditLogger.logOperation(operatorId, operatorUsername,
                "CREATE_COMPANY", "COMPANY", created.getId(), created.getName(),
                null, created, "创建公司", null, "/api/v1/admin/companies", "POST", 201, null);

        return created;
    }

    @Transactional
    public CompanyDto updateCompany(Long companyId, CompanyUpdateRequest request) {
        tenantScopeService.requirePlatformAdmin();
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "公司名称不能为空");
        }
        if (!existsById(companyId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公司不存在: " + companyId);
        }

        CompanyDto before = getCompany(companyId);

        String name = request.getName().trim();
        if (existsByNameExceptId(name, companyId)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "公司名称已存在: " + name);
        }

        String sql = "UPDATE t_company SET name = ?, is_active = COALESCE(?, is_active) WHERE id = ?";
        jdbc.update(sql, name, request.getIsActive(), companyId);
        CompanyDto after = getCompany(companyId);

        // Audit log
        Long operatorId = getCurrentUserId().orElse(null);
        String operatorUsername = getCurrentUsername();
        auditLogger.logOperation(operatorId, operatorUsername,
                "UPDATE_COMPANY", "COMPANY", companyId, after.getName(),
                before, after, "更新公司信息", null, "/api/v1/admin/companies/" + companyId, "PUT", 200, null);

        return after;
    }

    @Transactional
    public void setCompanyStatus(Long companyId, CompanyStatusRequest request) {
        tenantScopeService.requirePlatformAdmin();
        if (request == null || request.getIsActive() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "公司状态不能为空");
        }
        CompanyDto before = getCompany(companyId);
        int updated = jdbc.update("UPDATE t_company SET is_active = ? WHERE id = ?", request.getIsActive(), companyId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公司不存在: " + companyId);
        }
        CompanyDto after = getCompany(companyId);

        // Audit log: ENABLE / DISABLE
        Long operatorId = getCurrentUserId().orElse(null);
        String operatorUsername = getCurrentUsername();
        String op = after.getIsActive() ? "ENABLE_COMPANY" : "DISABLE_COMPANY";
        auditLogger.logOperation(operatorId, operatorUsername,
                op, "COMPANY", companyId, after.getName(),
                before, after, op + " by admin", null, "/api/v1/admin/companies/" + companyId + "/status", "POST", 200, null);
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        tenantScopeService.requirePlatformAdmin();
        if (!existsById(companyId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "公司不存在: " + companyId);
        }
        CompanyDto before = getCompany(companyId);
        jdbc.update("DELETE FROM t_group WHERE company_id = ?", companyId);
        int deleted = jdbc.update("DELETE FROM t_company WHERE id = ?", companyId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除公司失败");
        }

        // Audit log
        Long operatorId = getCurrentUserId().orElse(null);
        String operatorUsername = getCurrentUsername();
        auditLogger.logOperation(operatorId, operatorUsername,
                "DELETE_COMPANY", "COMPANY", companyId, before.getName(),
                before, null, "删除公司", null, "/api/v1/admin/companies/" + companyId, "DELETE", 200, null);
    }

    private boolean existsById(Long companyId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM t_company WHERE id = ?", Integer.class, companyId);
        return count != null && count > 0;
    }

    private CompanyDto mapCompanyRow(ResultSet rs, int rowNum) throws SQLException {
        return new CompanyDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getBoolean("is_active"),
                rs.getInt("group_count"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private boolean existsByName(String name) {
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM t_company WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    private boolean existsByNameExceptId(String name, Long companyId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM t_company WHERE name = ? AND id <> ?",
                Integer.class,
                name,
                companyId
        );
        return count != null && count > 0;
    }

    private Optional<Long> getCurrentUserId() {
        try {
            String username = getCurrentUsername();
            if (username == null) return Optional.empty();
            Optional<User> u = userRepository.findByUsername(username);
            return u.map(User::getId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getCurrentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;
            return auth.getName();
        } catch (Exception e) {
            return null;
        }
    }
}
