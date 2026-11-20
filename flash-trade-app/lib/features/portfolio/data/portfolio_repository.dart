import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../domain/models.dart';

class PortfolioRepository {
  const PortfolioRepository(this._dio);
  final Dio _dio;

  Future<List<PositionResponse>> getPositions() async {
    final response = await _dio.get(ApiConstants.positions);
    final list = response.data as List<dynamic>;
    return list
        .map((e) => PositionResponse.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<AssetViewResponse>> getAssetView() async {
    final response = await _dio.get(ApiConstants.assetView);
    final list = response.data as List<dynamic>;
    return list
        .map((e) => AssetViewResponse.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
