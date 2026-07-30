package com.tradex.flashtrade.service;

import com.tradex.flashtrade.dto.portfolio.OrderResponse;
import com.tradex.flashtrade.dto.portfolio.PositionResponse;
import com.tradex.flashtrade.dto.portfolio.TradeResponse;
import com.tradex.flashtrade.entity.Order;
import com.tradex.flashtrade.entity.OrderStatus;
import com.tradex.flashtrade.entity.Position;
import com.tradex.flashtrade.entity.Trade;
import com.tradex.flashtrade.repository.OrderRepository;
import com.tradex.flashtrade.repository.PositionRepository;
import com.tradex.flashtrade.repository.TradeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private final PositionRepository positionRepository;
    private final OrderRepository    orderRepository;
    private final TradeRepository    tradeRepository;
    private final UserLookupService  userLookupService;

    public PortfolioService(PositionRepository positionRepository,
                            OrderRepository orderRepository,
                            TradeRepository tradeRepository,
                            UserLookupService userLookupService) {
        this.positionRepository = positionRepository;
        this.orderRepository    = orderRepository;
        this.tradeRepository    = tradeRepository;
        this.userLookupService  = userLookupService;
    }

    public List<PositionResponse> getPositions(String username) {
        return getPositions(username, null, null);
    }

    public List<PositionResponse> getPositions(String username, String accountId, String symbol) {
        Long userId = userLookupService.getUserIdByUsername(username);
        List<Position> positions;
        if (accountId != null && !accountId.isBlank() && symbol != null && !symbol.isBlank()) {
            positions = positionRepository
                    .findByUserIdAndAccountIdAndSymbolOrderBySymbolAsc(userId, accountId, symbol.toUpperCase());
        } else if (accountId != null && !accountId.isBlank()) {
            positions = positionRepository.findByUserIdAndAccountId(userId, accountId);
        } else if (symbol != null && !symbol.isBlank()) {
            positions = positionRepository
                    .findByUserIdAndSymbolOrderBySymbolAsc(userId, symbol.toUpperCase());
        } else {
            positions = positionRepository.findByUserIdOrderBySymbolAsc(userId);
        }
        return positions.stream().map(this::toPositionResponse).toList();
    }

    /** 带分页的委托列表 */
    public Page<OrderResponse> getOrders(String username, OrderStatus status, Pageable pageable) {
        Long userId = userLookupService.getUserIdByUsername(username);
        Page<Order> orders = (status == null)
                ? orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        return orders.map(this::toOrderResponse);
    }

    /** 不分页的委托列表（全量） */
    public List<OrderResponse> getAllOrders(String username, OrderStatus status) {
        Long userId = userLookupService.getUserIdByUsername(username);
        List<Order> orders = (status == null)
                ? orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return orders.stream().map(this::toOrderResponse).toList();
    }

    /** 成約一覧（全件） */
    public List<TradeResponse> getTrades(String username) {
        Long userId = userLookupService.getUserIdByUsername(username);
        return tradeRepository.findByUserIdOrderByTradedAtDesc(userId).stream()
                .map(this::toTradeResponse)
                .toList();
    }

    /** 成交列表（含券商筛选） */
    public List<TradeResponse> getTradesByBroker(String username, String brokerName) {
        Long userId = userLookupService.getUserIdByUsername(username);
        return tradeRepository.findByUserIdAndBrokerNameOrderByTradedAtDesc(userId, brokerName).stream()
                .map(this::toTradeResponse)
                .toList();
    }

    // ---- mapping helpers ----

    public PositionResponse toPositionResponse(Position p) {
        return PositionResponse.builder()
                .id(p.getId())
                .accountId(p.getAccountId())
                .symbol(p.getSymbol())
                .quantity(p.getQuantity())
                .averageCost(p.getAverageCost())
                .currentPrice(p.getCurrentPrice())
                .marketValue(p.getMarketValue())
                .unrealizedPnl(p.getUnrealizedPnl())
                .brokerName(p.getBrokerName())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private OrderResponse toOrderResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .accountId(o.getAccountId())
                .symbol(o.getSymbol())
                .side(o.getSide())
                .quantity(o.getQuantity())
                .price(o.getPrice())
                .status(o.getStatus())
                .brokerName(o.getBrokerName())
                .externalOrderId(o.getExternalOrderId())
                .createdAt(o.getCreatedAt())
                .filledAt(o.getFilledAt())
                .build();
    }

    private TradeResponse toTradeResponse(Trade t) {
        return TradeResponse.builder()
                .id(t.getId())
                .accountId(t.getAccountId())
                .symbol(t.getSymbol())
                .side(t.getSide())
                .quantity(t.getQuantity())
                .price(t.getPrice())
                .commission(t.getCommission())
                .brokerName(t.getBrokerName())
                .externalTradeId(t.getExternalTradeId())
                .orderId(t.getOrder() != null ? t.getOrder().getId() : null)
                .tradedAt(t.getTradedAt())
                .build();
    }
}
