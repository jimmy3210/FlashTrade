import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../domain/models.dart';

class OrdersRepository {
  const OrdersRepository(this._dio);
  final Dio _dio;

  Future<List<OrderResponse>> getOrders() async {
    final response = await _dio.get(ApiConstants.orders);
    final list = response.data as List<dynamic>;
    return list
        .map((e) => OrderResponse.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<TradeResponse>> getTrades() async {
    final response = await _dio.get(ApiConstants.trades);
    final list = response.data as List<dynamic>;
    return list
        .map((e) => TradeResponse.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> cancelOrder(String orderId) async {
    await _dio.delete('${ApiConstants.placeOrder}/$orderId');
  }
}
