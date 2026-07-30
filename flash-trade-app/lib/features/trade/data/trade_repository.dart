import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';

class TradeRepository {
  const TradeRepository(this._dio);
  final Dio _dio;

  Future<void> placeOrder({
    required String symbol,
    required String side,
    required String orderType,
    required int quantity,
    double? price,
  }) async {
    final data = <String, dynamic>{
      'symbol': symbol,
      'side': side,
      'orderType': orderType,
      'quantity': quantity,
      if (price != null) 'price': price,
    };
    await _dio.post(ApiConstants.placeOrder, data: data);
  }
}
