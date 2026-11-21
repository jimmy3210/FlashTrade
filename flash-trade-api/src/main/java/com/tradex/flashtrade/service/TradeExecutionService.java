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
import java.util.Objects;
import java.util.Optional;
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
        AccountMapping mapping = resolveAuthorizedAccount(username, accountId);
        String clientOrderId = request.getClientOrderId();
        if (clientOrderId != null && (clientOrderId.isBlank() || clientOrderId.length() > 100)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "幂等 ID 必须为 1 到 100 个字符");
        }

        if (clientOrderId != null) {
            Optional<Order> existingOrder = findByClientOrderId(mapping, accountId, clientOrderId);
            if (existingOrder.isPresent()) {
                return resultForExistingOrder(existingOrder.get());
            }
        }

        // リスクコントロールチェック
        if (riskService.isAccountBlocked(accountId)) {
            throw new BusinessException(ErrorCode.ACCOUNT_RISK_BLOCKED);
        }

        // 解析券商信息
        BrokerClient broker = brokerMap.get(mapping.getBrokerName());
        if (broker == null) {
            throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                    "未找到券商实现: " + mapping.getBrokerName());
        }

        // 幂等检查
        if (clientOrderId != null) {
            String idempotencyKey = idempotencyKey(mapping, accountId, clientOrderId);
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(idempotencyKey, "SUBMITTING", IDEMPOTENCY_TTL);
            if (isNew == null) {
                throw new BusinessException(ErrorCode.BROKER_UNAVAILABLE,
                        "无法确认下单幂等状态；请查询订单状态后再操作");
            }
            if (!isNew) {
                return findByClientOrderId(mapping, accountId, clientOrderId)
                        .map(this::resultForExistingOrder)
                        .orElseGet(() -> PlaceOrderResult.builder()
                                .success(false)
                                .message("相同下单请求正在处理或结果尚未确认；请先查询订单状态，不要重复提交")
                                .build());
            }
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
            order.setClientOrderId(clientOrderId);
            orderRepository.save(order);
            log.info("下单成功 broker={} account={} symbol={} externalOrderId={}",
                    mapping.getBrokerName(), accountId,
                    request.getSymbol(), result.getExternalOrderId());
        }

        return result;
    }

    private Optional<Order> findByClientOrderId(
            AccountMapping mapping, String accountId, String clientOrderId) {
        return orderRepository.findByUserIdAndAccountIdAndBrokerNameAndClientOrderId(
                mapping.getUserId(), accountId, mapping.getBrokerName(), clientOrderId);
    }

    private String idempotencyKey(
            AccountMapping mapping, String accountId, String clientOrderId) {
        return IDEMPOTENCY_KEY_PREFIX + mapping.getUserId()
                + ":" + accountId.length() + ":" + accountId
                + ":" + mapping.getBrokerName().length() + ":" + mapping.getBrokerName()
                + ":" + clientOrderId.length() + ":" + clientOrderId;
    }

    private PlaceOrderResult resultForExistingOrder(Order order) {
        if (order.getExternalOrderId() == null || order.getExternalOrderId().isBlank()) {
            return PlaceOrderResult.builder()
                    .success(false)
                    .message("已有下单记录尚未确认券商订单号；请查询订单状态，不要重复提交")
                    .build();
        }
        return PlaceOrderResult.builder()
                .success(true)
                .externalOrderId(order.getExternalOrderId())
                .message("重复请求，返回已有委托")
                .build();
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
        AccountMapping mapping = resolveAuthorizedAccount(username, accountId);
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
    public BrokerOrder getOrderStatus(String username, String accountId, String externalOrderId) {
        AccountMapping mapping = resolveAuthorizedAccount(username, accountId);
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

    private AccountMapping resolveAuthorizedAccount(String username, String accountId) {
        AccountMapping mapping = resolveAccount(accountId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
        if (!Objects.equals(mapping.getUserId(), user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return mapping;
    }
}
