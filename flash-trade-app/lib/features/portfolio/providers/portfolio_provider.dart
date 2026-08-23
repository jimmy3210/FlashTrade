import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../data/portfolio_repository.dart';
import '../domain/models.dart';

final portfolioRepositoryProvider = Provider<PortfolioRepository>((ref) {
  return PortfolioRepository(DioClient.instance.dio);
});

final positionsProvider = FutureProvider<List<PositionResponse>>((ref) {
  return ref.read(portfolioRepositoryProvider).getPositions();
});

final assetViewProvider = FutureProvider<List<AssetViewResponse>>((ref) {
  return ref.read(portfolioRepositoryProvider).getAssetView();
});
