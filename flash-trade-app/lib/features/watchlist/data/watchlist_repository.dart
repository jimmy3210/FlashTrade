import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/demo_mode.dart';
import '../domain/watchlist_item.dart';

class WatchlistRepository {
  const WatchlistRepository(this._dio);

  final Dio _dio;

  Future<List<WatchlistItem>> getWatchlist() async {
    if (kDemoMode) {
      return const [
        WatchlistItem(
          symbol: 'AAPL',
          companyName: 'Apple Inc.',
          exchange: 'NASDAQ',
          active: true,
        ),
      ];
    }

    final response = await _dio.get(ApiConstants.watchlist);
    final data = response.data['data'] as List<dynamic>;
    return data
        .map((e) => WatchlistItem.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> addToWatchlist(String symbol) async {
    if (kDemoMode) return;
    await _dio.post(ApiConstants.watchlist, data: {'symbol': symbol});
  }

  Future<void> removeFromWatchlist(String symbol) async {
    if (kDemoMode) return;
    await _dio.delete('${ApiConstants.watchlist}/$symbol');
  }
}
