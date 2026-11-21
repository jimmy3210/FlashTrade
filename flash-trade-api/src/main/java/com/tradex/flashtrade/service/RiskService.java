package com.tradex.flashtrade.service;

import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 风控服务（TODO-08）
 * <p>
 * 提供账户级风控状态查询。状态变更和强制清仓在授权及结果核验能力完成前暂停。
 *
 * <p>风控状态持久化在 {@code t_account_risk} 表，
 * 操作记录写入 {@code t_operation_log} 表（审计）。
 */
@Service
public class RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskService.class);

    private static final String NORMAL = "NORMAL";
    private static final String DISABLED = "DISABLED";
    private static final String LIMITED = "LIMITED";
    private static final String SCOPE_REQUIRED_MESSAGE =
            "风控状态变更已暂停，待补齐服务端账户范围授权能力";
    private final JdbcTemplate jdbc;

    public RiskService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -------------------------------------------------------------------------
    // 状态变更
    // -------------------------------------------------------------------------

    /** 账户范围授权能力完成前拒绝变更。 */
    public void disableAccount(String accountId, Long operatorId) {
        rejectMutationUntilScopeAuthorization();
    }

    public void enableAccount(String accountId, Long operatorId) {
        rejectMutationUntilScopeAuthorization();
    }

    public void limitAccount(String accountId, Long operatorId) {
        rejectMutationUntilScopeAuthorization();
    }

    // -------------------------------------------------------------------------
    // 状态查询
    // -------------------------------------------------------------------------

    /**
     * 检查账户是否被风控阻止（DISABLED 或 LIMITED）。
     * 供 TradeExecutionService 下单前调用。
     */
    public boolean isAccountBlocked(String accountId) {
        String sql = "SELECT risk_status FROM t_account_risk WHERE account_id = ?";
        try {
            String status = jdbc.queryForObject(sql, String.class, accountId);
            return DISABLED.equals(status) || LIMITED.equals(status);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false; // 不在风控表中 → 正常
        }
    }

    /**
     * 获取账户风控状态（字符串）。
     */
    public String getRiskStatus(String accountId) {
        String sql = "SELECT risk_status FROM t_account_risk WHERE account_id = ?";
        try {
            return jdbc.queryForObject(sql, String.class, accountId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return NORMAL;
        }
    }

    private void rejectMutationUntilScopeAuthorization() {
        log.warn("Risk status mutation rejected because account-scope authorization is unavailable");
        throw new BusinessException(ErrorCode.ACCESS_DENIED, SCOPE_REQUIRED_MESSAGE);
    }
}
