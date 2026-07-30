import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../../../core/constants/api_constants.dart';
import '../domain/models.dart';

class PortfolioRepository {
  const PortfolioRepository(this._dio);
  final Dio _dio;

  Future<List<PositionResponse>> getPositions() async {
    debugPrint(
      'PortfolioRepository.getPositions: url=${ApiConstants.baseUrl}${ApiConstants.positions}',
    );
    try {
      final response = await _dio.get(ApiConstants.positions);
      debugPrint(
        'PortfolioRepository.getPositions success: status=${response.statusCode} data=${response.data}',
      );
      final list = response.data as List<dynamic>;
      return list
          .map((e) => PositionResponse.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      debugPrint(
        'PortfolioRepository.getPositions DioException: message=${e.message} status=${e.response?.statusCode} response=${e.response?.data}',
      );
      rethrow;
    } catch (e, st) {
      debugPrint('PortfolioRepository.getPositions error: $e\n$st');
      rethrow;
    }
  }

  Future<List<AssetViewResponse>> getAssetView() async {
    final response = await _dio.get(ApiConstants.assetView);
    final list = response.data as List<dynamic>;
    return list
        .map((e) => AssetViewResponse.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
