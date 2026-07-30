import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/demo_mode.dart';
import '../../../core/constants/mock_data.dart';
import '../../../core/network/dio_client.dart';
import '../data/home_repository.dart';
import '../domain/models.dart';

final homeRepositoryProvider = Provider<HomeRepository>((ref) {
  return HomeRepository(DioClient.instance.dio);
});

final dailyPnlProvider = FutureProvider<List<DailyPnlResponse>>((ref) {
  if (kDemoMode) return Future.value(MockData.dailyPnl);
  return ref.read(homeRepositoryProvider).getDailyPnl();
});
