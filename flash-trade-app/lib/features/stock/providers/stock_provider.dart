import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/demo_mode.dart';
import '../../../core/network/dio_client.dart';
import '../data/stock_repository.dart';
import '../domain/stock_suggestion.dart';

final stockRepositoryProvider = Provider<StockRepository>((ref) {
  return StockRepository(DioClient.instance.dio);
});

final stockSearchProvider =
    FutureProvider.family<List<StockSuggestion>, String>((ref, query) {
      if (kDemoMode) {
        return Future.value(
          query.isEmpty
              ? []
              : [
                  const StockSuggestion(
                    symbol: 'AAPL',
                    companyName: 'Apple Inc.',
                    exchange: 'NASDAQ',
                    active: true,
                  ),
                  const StockSuggestion(
                    symbol: 'AMZN',
                    companyName: 'Amazon.com, Inc.',
                    exchange: 'NASDAQ',
                    active: true,
                  ),
                ],
        );
      }
      return ref.read(stockRepositoryProvider).searchStocks(query);
    });
