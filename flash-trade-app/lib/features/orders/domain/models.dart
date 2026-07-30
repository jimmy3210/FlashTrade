class OrderResponse {
  const OrderResponse({
    required this.id,
    required this.symbol,
    required this.side,
    required this.orderType,
    required this.quantity,
    this.price,
    required this.status,
    required this.createdAt,
  });

  final String id;
  final String symbol;
  final String side;
  final String orderType;
  final int quantity;
  final double? price;
  final String status;
  final String createdAt;

  factory OrderResponse.fromJson(Map<String, dynamic> json) => OrderResponse(
    id: json['id'] as String,
    symbol: json['symbol'] as String,
    side: json['side'] as String,
    orderType: json['orderType'] as String,
    quantity: (json['quantity'] as num).toInt(),
    price: json['price'] != null ? (json['price'] as num).toDouble() : null,
    status: json['status'] as String,
    createdAt: json['createdAt'] as String? ?? '',
  );
}

class TradeResponse {
  const TradeResponse({
    required this.id,
    required this.orderId,
    required this.symbol,
    required this.side,
    required this.quantity,
    required this.executedPrice,
    required this.executedAt,
    this.commission = 0.0,
    this.brokerName = '',
  });

  final String id;
  final String orderId;
  final String symbol;
  final String side;
  final int quantity;
  final double executedPrice;
  final String executedAt;
  final double commission;
  final String brokerName;

  factory TradeResponse.fromJson(Map<String, dynamic> json) => TradeResponse(
    id: json['id'] as String,
    orderId: json['orderId'] as String? ?? '',
    symbol: json['symbol'] as String,
    side: json['side'] as String,
    quantity: (json['quantity'] as num).toInt(),
    executedPrice: (json['executedPrice'] as num).toDouble(),
    executedAt: json['executedAt'] as String? ?? '',
    commission: json['commission'] != null
        ? (json['commission'] as num).toDouble()
        : 0.0,
    brokerName: json['brokerName'] as String? ?? '',
  );
}
