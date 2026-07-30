import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../constants/api_constants.dart';
import '../storage/secure_storage.dart';

class ApiInterceptor extends Interceptor {
  bool _isRefreshing = false;

  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await SecureStorageService.getAccessToken();
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    debugPrint(
      'ApiInterceptor.onRequest: url=${options.baseUrl}${options.path} method=${options.method} headers=${options.headers} data=${options.data}',
    );
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    debugPrint(
      'ApiInterceptor.onError: url=${err.requestOptions.baseUrl}${err.requestOptions.path} status=${err.response?.statusCode} error=${err.message} response=${err.response?.data}',
    );
    if (err.response?.statusCode == 401 && !_isRefreshing) {
      _isRefreshing = true;
      try {
        final refreshToken = await SecureStorageService.getRefreshToken();
        if (refreshToken == null) {
          await SecureStorageService.clearTokens();
          handler.next(err);
          return;
        }

        final refreshDio = Dio();
        final response = await refreshDio.post(
          '${ApiConstants.baseUrl}${ApiConstants.refresh}',
          data: {'refreshToken': refreshToken},
        );

        final newAccessToken = response.data['accessToken'] as String;
        final newRefreshToken = response.data['refreshToken'] as String;
        await SecureStorageService.saveTokens(
          accessToken: newAccessToken,
          refreshToken: newRefreshToken,
        );

        // Retry the original request with the new token
        err.requestOptions.headers['Authorization'] = 'Bearer $newAccessToken';
        final retryDio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
        final retryResponse = await retryDio.fetch(err.requestOptions);
        handler.resolve(retryResponse);
      } catch (_) {
        await SecureStorageService.clearTokens();
        handler.next(err);
      } finally {
        _isRefreshing = false;
      }
    } else {
      handler.next(err);
    }
  }
}
