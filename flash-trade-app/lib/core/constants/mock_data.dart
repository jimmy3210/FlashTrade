import '../../features/home/domain/models.dart';
import '../../features/orders/domain/models.dart';
import '../../features/portfolio/domain/models.dart';

class MockData {
  MockData._();

  // ── ポジション一覧 ──────────────────────────────────────
  static final List<PositionResponse> positions = [
    const PositionResponse(
      symbol: 'AAPL',
      quantity: 100,
      averageCost: 145.50,
      currentPrice: 178.20,
      marketValue: 17820.00,
      unrealizedPnl: 3270.00,
      unrealizedPnlRate: 0.2247,
      brokerName: 'Schwab',
    ),
    const PositionResponse(
      symbol: 'TSLA',
      quantity: 50,
      averageCost: 230.00,
      currentPrice: 215.40,
      marketValue: 10770.00,
      unrealizedPnl: -730.00,
      unrealizedPnlRate: -0.0635,
      brokerName: 'E*TRADE',
    ),
    const PositionResponse(
      symbol: 'NVDA',
      quantity: 30,
      averageCost: 480.00,
      currentPrice: 876.50,
      marketValue: 26295.00,
      unrealizedPnl: 11895.00,
      unrealizedPnlRate: 0.8260,
      brokerName: 'Tastyworks',
    ),
    const PositionResponse(
      symbol: 'MSFT',
      quantity: 80,
      averageCost: 310.00,
      currentPrice: 425.80,
      marketValue: 34064.00,
      unrealizedPnl: 9264.00,
      unrealizedPnlRate: 0.3731,
      brokerName: 'Schwab',
    ),
    const PositionResponse(
      symbol: 'AMZN',
      quantity: 25,
      averageCost: 130.00,
      currentPrice: 188.50,
      marketValue: 4712.50,
      unrealizedPnl: 1462.50,
      unrealizedPnlRate: 0.4500,
      brokerName: 'E*TRADE',
    ),
    const PositionResponse(
      symbol: 'GOOGL',
      quantity: 40,
      averageCost: 140.00,
      currentPrice: 172.30,
      marketValue: 6892.00,
      unrealizedPnl: 1292.00,
      unrealizedPnlRate: 0.2307,
      brokerName: 'Tastyworks',
    ),
  ];

  // ── 資産ビュー ──────────────────────────────────────────
  static List<AssetViewResponse> get assetView => positions
      .map(
        (p) => AssetViewResponse(
          symbol: p.symbol,
          marketValue: p.marketValue,
          unrealizedPnl: p.unrealizedPnl,
          unrealizedPnlRate: p.unrealizedPnlRate,
          currentPrice: p.currentPrice,
          quantity: p.quantity,
        ),
      )
      .toList();

  // ── 日次損益（過去7日） ─────────────────────────────────
  static final List<DailyPnlResponse> dailyPnl = [
    const DailyPnlResponse(
      date: '2026-03-25',
      realizedPnl: 1200.00,
      unrealizedPnl: 8500.00,
      totalPnl: 9700.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-26',
      realizedPnl: -400.00,
      unrealizedPnl: -3200.00,
      totalPnl: -3600.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-27',
      realizedPnl: 850.00,
      unrealizedPnl: 5100.00,
      totalPnl: 5950.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-28',
      realizedPnl: -200.00,
      unrealizedPnl: -1800.00,
      totalPnl: -2000.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-29',
      realizedPnl: 1500.00,
      unrealizedPnl: 12000.00,
      totalPnl: 13500.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-30',
      realizedPnl: -600.00,
      unrealizedPnl: -4500.00,
      totalPnl: -5100.00,
    ),
    const DailyPnlResponse(
      date: '2026-03-31',
      realizedPnl: 2100.00,
      unrealizedPnl: 24253.50,
      totalPnl: 26353.50,
    ),
  ];

  // ── 注文一覧 ────────────────────────────────────────────
  static final List<OrderResponse> orders = [
    const OrderResponse(
      id: 'ord-001',
      symbol: 'AAPL',
      side: 'BUY',
      orderType: 'LIMIT',
      quantity: 10,
      price: 175.00,
      status: 'PENDING',
      createdAt: '2026-04-01T09:30:00',
    ),
    const OrderResponse(
      id: 'ord-002',
      symbol: 'TSLA',
      side: 'SELL',
      orderType: 'MARKET',
      quantity: 5,
      price: null,
      status: 'FILLED',
      createdAt: '2026-03-31T14:22:10',
    ),
    const OrderResponse(
      id: 'ord-003',
      symbol: 'NVDA',
      side: 'BUY',
      orderType: 'LIMIT',
      quantity: 3,
      price: 850.00,
      status: 'CANCELLED',
      createdAt: '2026-03-30T10:05:33',
    ),
    const OrderResponse(
      id: 'ord-004',
      symbol: 'MSFT',
      side: 'BUY',
      orderType: 'MARKET',
      quantity: 20,
      price: null,
      status: 'FILLED',
      createdAt: '2026-03-29T11:00:00',
    ),
    const OrderResponse(
      id: 'ord-005',
      symbol: 'AMZN',
      side: 'BUY',
      orderType: 'LIMIT',
      quantity: 5,
      price: 185.00,
      status: 'PENDING',
      createdAt: '2026-04-01T08:45:00',
    ),
  ];

  // ── 成約履歴 ────────────────────────────────────────────
  static final List<TradeResponse> trades = [
    const TradeResponse(
      id: 'trd-001',
      orderId: 'ord-002',
      symbol: 'TSLA',
      side: 'SELL',
      quantity: 5,
      executedPrice: 216.80,
      executedAt: '2026-03-31T14:22:10',
      commission: 1.50,
      brokerName: 'E*TRADE',
    ),
    const TradeResponse(
      id: 'trd-002',
      orderId: 'ord-004',
      symbol: 'MSFT',
      side: 'BUY',
      quantity: 20,
      executedPrice: 424.50,
      executedAt: '2026-03-29T11:00:22',
      commission: 0.00,
      brokerName: 'Schwab',
    ),
    const TradeResponse(
      id: 'trd-003',
      orderId: 'ord-006',
      symbol: 'NVDA',
      side: 'BUY',
      quantity: 10,
      executedPrice: 872.00,
      executedAt: '2026-03-27T15:30:05',
      commission: 0.65,
      brokerName: 'Tastyworks',
    ),
    const TradeResponse(
      id: 'trd-004',
      orderId: 'ord-007',
      symbol: 'AAPL',
      side: 'BUY',
      quantity: 50,
      executedPrice: 146.20,
      executedAt: '2026-03-25T10:10:00',
      commission: 0.00,
      brokerName: 'Schwab',
    ),
  ];
}
