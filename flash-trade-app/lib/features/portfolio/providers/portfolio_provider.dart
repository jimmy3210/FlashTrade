import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/demo_mode.dart';
import '../../../core/constants/mock_data.dart';
import '../../../core/network/dio_client.dart';
import '../data/portfolio_repository.dart';
import '../domain/models.dart';

final portfolioRepositoryProvider = Provider<PortfolioRepository>((ref) {
  return PortfolioRepository(DioClient.instance.dio);
});

final positionsProvider = FutureProvider<List<PositionResponse>>((ref) {
  if (kDemoMode) return Future.value(MockData.positions);
  return ref.read(portfolioRepositoryProvider).getPositions();
});

final assetViewProvider = FutureProvider<List<AssetViewResponse>>((ref) {
  if (kDemoMode) return Future.value(MockData.assetView);
  return ref.read(portfolioRepositoryProvider).getAssetView();
});
