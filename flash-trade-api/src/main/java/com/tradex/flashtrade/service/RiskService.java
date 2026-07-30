package com.tradex.flashtrade.service;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.entity.OrderSide;
import com.tradex.flashtrade.entity.Position;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.PositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 风控服务（TODO-08）
 * <p>
 * 提供账户级风控操作：
 * <ul>
 *   <li>关停账户（DISABLED）：阻止该账户后续下单</li>
 *   <li>解除关停（NORMAL）</li>
 *   <li>限制交易（LIMITED）：只读，不能下单</li>
 *   <li>强制清仓（LIQUIDATED）：异步市价卖出所有持仓</li>
 * </ul>
 *
 * <p>风控状态持久化在 {@code t_account_risk} 表，
 * 操作记录写入 {@code t_operation_log} 表（审计）。
 */
@Service
public class RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskService.class);

    private static final String NORMAL     = "NORMAL";
    private static final String DISABLED   = "DISABLED";
    private static final String LIMITED    = "LIMITED";
    private static final String LIQUIDATED = "LIQUIDATED";

    private final JdbcTemplate               jdbc;
    private final PositionRepository         positionRepo;
    private final BrokerSyncProperties       syncProperties;
    private final Map<String, BrokerClient>  brokerMap;

    public RiskService(JdbcTemplate jdbc,
                       PositionRepository positionRepo,
                       BrokerSyncProperties syncProperties,
                       List<BrokerClient> brokerClients) {
        this.jdbc           = jdbc;
        this.positionRepo   = positionRepo;
        this.syncProperties = syncProperties;
        this.brokerMap      = brokerClients.stream()
                .collect(Collectors.toMap(BrokerClient::getBrokerName, Function.identity()));
    }

    // -------------------------------------------------------------------------
    // 状态变更
    // -------------------------------------------------------------------------

    /** 关停账户（下单被阻止） */
    @Transactional
    public void disableAccount(String accountId, Long operatorId) {
        upsertRisk(accountId, DISABLED, operatorId, "管理员关停账户");
        writeOperationLog(operatorId, "DISABLE_ACCOUNT", "ACCOUNT", accountId, "账户已关停");
        log.info("账户 [{}] 已关停，操作人 ID={}", accountId, operatorId);
    }

    /** 解除关停，恢复正常状态 */
    @Transactional
    public void enableAccount(String accountId, Long operatorId) {
        upsertRisk(accountId, NORMAL, operatorId, "管理员解除关停");
        writeOperationLog(operatorId, "ENABLE_ACCOUNT", "ACCOUNT", accountId, "账户已恢复正常");
        log.info("账户 [{}] 已恢复正常，操作人 ID={}", accountId, operatorId);
    }

    /** 限制交易（LIMITED：只读，不能下单） */
    @Transactional
    public void limitAccount(String accountId, Long operatorId) {
        upsertRisk(accountId, LIMITED, operatorId, "管理员限制交易");
        writeOperationLog(operatorId, "LIMIT_ACCOUNT", "ACCOUNT", accountId, "账户已限制交易");
        log.info("账户 [{}] 已限制交易，操作人 ID={}", accountId, operatorId);
    }

    /**
     * 强制清仓（异步）。
     * <p>
     * 获取账户当前所有 {@code quantity > 0} 的持仓，
     * 对每个 symbol 生成市价卖出委托，通过 BrokerClient 执行。
     */
    @Async
    public void forceLiquidation(String accountId, Long operatorId) {
        log.info("开始强制清仓，账户={}, 操作人 ID={}", accountId, operatorId);

        // 查找此账户对应的券商配置
        BrokerSyncProperties.AccountMapping mapping = syncProperties.getAccounts().stream()
                .filter(a -> a.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND,
                        "账户 " + accountId + " 未在配置中找到"));

        BrokerClient client = brokerMap.get(mapping.getBrokerName());
        if (client == null) {
            log.error("强制清仓失败：找不到券商 [{}]", mapping.getBrokerName());
            return;
        }

        // 获取持仓
        List<Position> positions = positionRepo.findByAccountId(accountId);
        int count = 0;
        for (Position pos : positions) {
            if (pos.getQuantity() == null || pos.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            try {
                OrderRequest req = new OrderRequest();
                req.setSymbol(pos.getSymbol());
                req.setSide(OrderSide.SELL);
                req.setQuantity(pos.getQuantity().intValue());
                req.setOrderType(OrderRequest.OrderType.MARKET);
                req.setClientOrderId("LIQUIDATE-" + pos.getSymbol() + "-" + UUID.randomUUID());

                client.placeOrder(accountId, req);
                count++;
                log.info("清仓委托已提交：账户={}, symbol={}, qty={}",
                        accountId, pos.getSymbol(), pos.getQuantity().intValue());
            } catch (Exception e) {
                log.error("清仓委托失败：symbol={}, 原因={}", pos.getSymbol(), e.getMessage(), e);
            }
        }

        // 更新风控状态
        upsertRisk(accountId, LIQUIDATED, operatorId, "强制清仓，共 " + count + " 笔委托");
        writeOperationLog(operatorId, "FORCE_LIQUIDATE", "ACCOUNT", accountId,
                "强制清仓完成，提交 " + count + " 笔市价卖出");

        log.info("强制清仓完成，账户={}，共提交 {} 笔委托", accountId, count);
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

    // -------------------------------------------------------------------------
    // 内部方法
    // -------------------------------------------------------------------------

    private void upsertRisk(String accountId, String status, Long operatorId, String note) {
        String sql = """
                INSERT INTO t_account_risk (account_id, risk_status, triggered_at, operator_id, note)
                VALUES (?, ?, NOW(), ?, ?)
                ON DUPLICATE KEY UPDATE
                    risk_status  = VALUES(risk_status),
                    triggered_at = VALUES(triggered_at),
                    operator_id  = VALUES(operator_id),
                    note         = VALUES(note)
                """;
        jdbc.update(sql, accountId, status, operatorId, note);
    }

    private void writeOperationLog(Long operatorId, String action,
                                   String targetType, String targetId, String detail) {
        String sql = """
                INSERT INTO t_operation_log (operator_id, action, target_type, target_id, detail)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbc.update(sql, operatorId, action, targetType, targetId, detail);
        } catch (Exception e) {
            log.warn("操作日志写入失败（非关键路径）: {}", e.getMessage());
        }
    }
}
