package com.tradex.flashtrade.service;

import com.tradex.flashtrade.broker.BrokerClient;
import com.tradex.flashtrade.broker.dto.BrokerOrder;
import com.tradex.flashtrade.broker.dto.OrderRequest;
import com.tradex.flashtrade.broker.dto.PlaceOrderResult;
import com.tradex.flashtrade.config.BrokerSyncProperties;
import com.tradex.flashtrade.config.BrokerSyncProperties.AccountMapping;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.exception.BusinessException;
import com.tradex.flashtrade.exception.ErrorCode;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 交易执行服务
 * <p>
 * 职责：
 * <ul>
 *   <li>接收下单 / 撤单请求，路由到对应券商 BrokerClient</li>
 *   <li>幂等控制：通过 clientOrderId 防止重复下单（Redis SETNX）</li>
 *   <li>下单成功后将委托记录持久化到 orders 表</li>
 * </ul>
 */
@Service
public class TradeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TradeExecutionService.class);
    private static final String IDEMPOTENCY_KEY_PREFIX = "order:idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final Map<String, BrokerClient>   brokerMap;
    private final BrokerSyncProperties        syncProperties;
    private final OrderRepository             orderRepository;
    private final UserRepository              userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RiskService                 riskService;

    public TradeExecutionService(List<BrokerClient> brokerClients,
                                 BrokerSyncProperties syncProperties,
                                 OrderRepository orderRepository,
                                 UserRepository userRepository,
                                 RedisTemplate<String, String> redisTemplate,
                                 RiskService riskService) {
        this.brokerMap       = brokerClients.stream()
                .collect(Collectors.toMap(BrokerClient::getBrokerName, Function.identity()));
        this.syncProperties  = syncProperties;
        this.orderRepository = orderRepository;
        this.userRepository  = userRepository;
        this.redisTemplate   = redisTemplate;
        this.riskService     = riskService;
    }

    /**
     * 下单。
     *
     * @param username  当前登录用户名
     * @param accountId 目标账户 ID
     * @param request   下单请求（包含幂等 clientOrderId）
     * @return 下单结果（含券商侧 externalOrderId）
     */
    @Transactional
    public PlaceOrderResult placeOrder(String username, String accountId, OrderRequest request) {
        // リスクコントロールチェック
        if (riskService.isAccountBlocked(accountId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_RISK_BLOCKED);
        }

        // 幂等检查
        if (request.getClientOrderId() != null) {
            String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + request.getClientOrderId();
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(idempotencyKey, "1", IDEMPOTENCY_TTL);
            if (Boolean.FALSE.equals(isNew)) {
                // 重复请求：查询已有 order，返回其 externalOrderId
                return orderRepository.findByExternalOrderId(request.getClientOrderId())
                        .map(o -> PlaceOrderResult.builder()
                                .success(true)
                                .externalOrderId(o.getExternalOrderId())
                                .message("重复请求，返回已有委托")
                                .build())
                        .orElse(PlaceOrderResult.builder()
                                .success(false)
                                .message("重复请求，委托记录未找到")
                                .build());
            }
        }

        // 解析券商信息
        AccountMapping mapping = resolveAccount(accountId);
        BrokerClient broker = brokerMap.get(mapping.getBrokerName());
        if (broker == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "未找到券商实现: " + mapping.getBrokerName());
        }

        // 调用券商 API
        PlaceOrderResult result = broker.placeOrder(accountId, request);

        // 持久化委托记录
        if (result.isSuccess()) {
            User user = userRepository.findById(mapping.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
            Order order = new Order();
            order.setUser(user);
            order.setAccountId(accountId);
            order.setSymbol(request.getSymbol().toUpperCase());
            order.setSide(request.getSide());
            order.setQuantity(java.math.BigDecimal.valueOf(request.getQuantity()));
            order.setPrice(request.getLimitPrice());
            order.setStatus(OrderStatus.PENDING);
            order.setBrokerName(mapping.getBrokerName());
            order.setExternalOrderId(result.getExternalOrderId());
            orderRepository.save(order);
            log.info("下单成功 broker={} account={} symbol={} externalOrderId={}",
                    mapping.getBrokerName(), accountId,
                    request.getSymbol(), result.getExternalOrderId());
        }

        return result;
    }

    /**
     * 撤单。
     *
     * @param username        当前登录用户名
     * @param accountId       账户 ID
     * @param externalOrderId 券商侧订单 ID
     */
    @Transactional
    public void cancelOrder(String username, String accountId, String externalOrderId) {
        AccountMapping mapping = resolveAccount(accountId);
        BrokerClient broker = brokerMap.get(mapping.getBrokerName());
        if (broker == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "未找到券商实现: " + mapping.getBrokerName());
        }

        broker.cancelOrder(accountId, externalOrderId);

        // 更新本地委托状态
        orderRepository.findByExternalOrderIdAndBrokerName(externalOrderId, mapping.getBrokerName())
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                });

        log.info("撤单成功 broker={} account={} externalOrderId={}",
                mapping.getBrokerName(), accountId, externalOrderId);
    }

    /**
     * 查询单笔委托状态（直接调用券商 API，不走缓存）。
     */
    public BrokerOrder getOrderStatus(String accountId, String externalOrderId) {
        AccountMapping mapping = resolveAccount(accountId);
        BrokerClient broker = brokerMap.get(mapping.getBrokerName());
        if (broker == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "未找到券商实现: " + mapping.getBrokerName());
        }
        return broker.getOrderStatus(accountId, externalOrderId);
    }

    // -------------------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------------------

    private AccountMapping resolveAccount(String accountId) {
        return syncProperties.getAccounts().stream()
                .filter(a -> accountId.equals(a.getAccountId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "账户 [" + accountId + "] 不在配置列表中"));
    }
}
