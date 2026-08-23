import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../data/stock_repository.dart';
import '../domain/stock_suggestion.dart';

final stockRepositoryProvider = Provider<StockRepository>((ref) {
  return StockRepository(DioClient.instance.dio);
});

final stockSearchProvider =
    FutureProvider.family<List<StockSuggestion>, String>((ref, query) {
      return ref.read(stockRepositoryProvider).searchStocks(query);
    });
