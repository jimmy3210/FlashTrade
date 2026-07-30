class PositionResponse {
  const PositionResponse({
    required this.symbol,
    required this.quantity,
    required this.averageCost,
    required this.currentPrice,
    required this.marketValue,
    required this.unrealizedPnl,
    required this.unrealizedPnlRate,
    this.brokerName = '',
  });

  final String symbol;
  final int quantity;
  final double averageCost;
  final double currentPrice;
  final double marketValue;
  final double unrealizedPnl;
  final double unrealizedPnlRate;
  final String brokerName;

  factory PositionResponse.fromJson(Map<String, dynamic> json) =>
      PositionResponse(
        symbol: json['symbol'] as String,
        quantity: (json['quantity'] as num).toInt(),
        averageCost: (json['averageCost'] as num).toDouble(),
        currentPrice: (json['currentPrice'] as num).toDouble(),
        marketValue: (json['marketValue'] as num).toDouble(),
        unrealizedPnl: (json['unrealizedPnl'] as num).toDouble(),
        unrealizedPnlRate: (json['unrealizedPnlRate'] as num).toDouble(),
        brokerName: json['brokerName'] as String? ?? '',
      );
}

class AssetViewResponse {
  const AssetViewResponse({
    required this.symbol,
    required this.marketValue,
    required this.unrealizedPnl,
    required this.unrealizedPnlRate,
    required this.currentPrice,
    required this.quantity,
  });

  final String symbol;
  final double marketValue;
  final double unrealizedPnl;
  final double unrealizedPnlRate;
  final double currentPrice;
  final int quantity;

  factory AssetViewResponse.fromJson(Map<String, dynamic> json) =>
      AssetViewResponse(
        symbol: json['symbol'] as String,
        marketValue: (json['marketValue'] as num).toDouble(),
        unrealizedPnl: (json['unrealizedPnl'] as num).toDouble(),
        unrealizedPnlRate: (json['unrealizedPnlRate'] as num).toDouble(),
        currentPrice: (json['currentPrice'] as num).toDouble(),
        quantity: (json['quantity'] as num).toInt(),
      );
}
