import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/demo_mode.dart';
import '../../../core/constants/mock_data.dart';
import '../../../core/network/dio_client.dart';
import '../data/orders_repository.dart';
import '../domain/models.dart';

final ordersRepositoryProvider = Provider<OrdersRepository>((ref) {
  return OrdersRepository(DioClient.instance.dio);
});

final ordersProvider = FutureProvider<List<OrderResponse>>((ref) {
  if (kDemoMode) return Future.value(MockData.orders);
  return ref.read(ordersRepositoryProvider).getOrders();
});

final tradesProvider = FutureProvider<List<TradeResponse>>((ref) {
  if (kDemoMode) return Future.value(MockData.trades);
  return ref.read(ordersRepositoryProvider).getTrades();
});
