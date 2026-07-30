const _sentinel = Object();

class TradeFormState {
  const TradeFormState({
    this.symbol = '',
    this.side = 'BUY',
    this.orderType = 'MARKET',
    this.timeInForce = 'DAY',
    this.quantity = '',
    this.price = '',
    this.isLoading = false,
    this.errorMessage,
  });

  final String symbol;
  final String side;
  final String orderType;
  final String timeInForce;
  final String quantity;
  final String price;
  final bool isLoading;
  final String? errorMessage;

  TradeFormState copyWith({
    String? symbol,
    String? side,
    String? orderType,
    String? timeInForce,
    String? quantity,
    String? price,
    bool? isLoading,
    Object? errorMessage = _sentinel,
  }) {
    return TradeFormState(
      symbol: symbol ?? this.symbol,
      side: side ?? this.side,
      orderType: orderType ?? this.orderType,
      timeInForce: timeInForce ?? this.timeInForce,
      quantity: quantity ?? this.quantity,
      price: price ?? this.price,
      isLoading: isLoading ?? this.isLoading,
      errorMessage: errorMessage == _sentinel
          ? this.errorMessage
          : errorMessage as String?,
    );
  }
}
