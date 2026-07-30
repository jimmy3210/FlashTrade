import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../domain/models.dart';

class HomeRepository {
  const HomeRepository(this._dio);
  final Dio _dio;

  Future<List<DailyPnlResponse>> getDailyPnl() async {
    final response = await _dio.get(ApiConstants.dailyPnl);
    // Handle both single object and array
    if (response.data is List) {
      final list = response.data as List<dynamic>;
      return list
          .map((e) => DailyPnlResponse.fromJson(e as Map<String, dynamic>))
          .toList();
    } else if (response.data is Map) {
      return [DailyPnlResponse.fromJson(response.data as Map<String, dynamic>)];
    }
    return [];
  }
}
