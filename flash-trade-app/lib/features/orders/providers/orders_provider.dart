import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../data/orders_repository.dart';
import '../domain/models.dart';

final ordersRepositoryProvider = Provider<OrdersRepository>((ref) {
  return OrdersRepository(DioClient.instance.dio);
});

final ordersProvider = FutureProvider<List<OrderResponse>>((ref) {
  return ref.read(ordersRepositoryProvider).getOrders();
});

final tradesProvider = FutureProvider<List<TradeResponse>>((ref) {
  return ref.read(ordersRepositoryProvider).getTrades();
});
