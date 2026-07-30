class WatchlistItem {
  const WatchlistItem({
    required this.symbol,
    required this.companyName,
    required this.exchange,
    required this.active,
  });

  final String symbol;
  final String companyName;
  final String exchange;
  final bool active;

  factory WatchlistItem.fromJson(Map<String, dynamic> json) {
    return WatchlistItem(
      symbol: json['symbol'] as String,
      companyName: json['companyName'] as String? ?? '',
      exchange: json['exchange'] as String? ?? '',
      active: json['active'] as bool? ?? true,
    );
  }
}
