class DailyPnlResponse {
  const DailyPnlResponse({
    required this.date,
    required this.realizedPnl,
    required this.unrealizedPnl,
    required this.totalPnl,
  });

  final String date;
  final double realizedPnl;
  final double unrealizedPnl;
  final double totalPnl;

  factory DailyPnlResponse.fromJson(Map<String, dynamic> json) =>
      DailyPnlResponse(
        date: json['date'] as String? ?? '',
        realizedPnl: (json['realizedPnl'] as num?)?.toDouble() ?? 0.0,
        unrealizedPnl: (json['unrealizedPnl'] as num?)?.toDouble() ?? 0.0,
        totalPnl: (json['totalPnl'] as num?)?.toDouble() ?? 0.0,
      );
}
