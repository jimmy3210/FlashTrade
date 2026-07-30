import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../domain/stock_suggestion.dart';

class StockRepository {
  const StockRepository(this._dio);
  final Dio _dio;

  Future<List<StockSuggestion>> searchStocks(String query) async {
    final response = await _dio.get(
      ApiConstants.stockSearch,
      queryParameters: {'query': query},
    );
    final data = response.data['data'] as List<dynamic>;
    return data
        .map((e) => StockSuggestion.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
