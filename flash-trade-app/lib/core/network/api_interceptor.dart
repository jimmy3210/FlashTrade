import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../constants/api_constants.dart';
import '../storage/secure_storage.dart';

class ApiFieldError {
  const ApiFieldError({
    required this.field,
    required this.code,
    required this.message,
  });

  final String field;
  final String code;
  final String message;
}

class ApiError {
  const ApiError({
    required this.code,
    required this.message,
    required this.fieldErrors,
    this.traceId,
  });

  final String code;
  final String message;
  final List<ApiFieldError> fieldErrors;
  final String? traceId;

  static ApiError? tryParse(dynamic body) {
    if (body is! Map<String, dynamic>) return null;

    final code = body['code'];
    final message = body['message'];
    final data = body['data'];
    if (code is String && message is String && data is Map<String, dynamic>) {
      final rawFieldErrors = data['fieldErrors'];
      final traceId = data['traceId'];
      if (rawFieldErrors is! List || traceId is! String) return null;

      final fieldErrors = <ApiFieldError>[];
      for (final rawError in rawFieldErrors) {
        if (rawError is! Map<String, dynamic>) return null;
        final field = rawError['field'];
        final fieldCode = rawError['code'];
        final fieldMessage = rawError['message'];
        if (field is! String ||
            fieldCode is! String ||
            fieldMessage is! String) {
          return null;
        }
        fieldErrors.add(
          ApiFieldError(field: field, code: fieldCode, message: fieldMessage),
        );
      }

      return ApiError(
        code: code,
        message: message,
        fieldErrors: List.unmodifiable(fieldErrors),
        traceId: traceId,
      );
    }

    final legacyError = body['error'];
    if (body['success'] == false && legacyError is Map<String, dynamic>) {
      final legacyCode = legacyError['code'];
      final legacyMessage = legacyError['message'];
      final traceId = legacyError['traceId'];
      if (legacyCode is! String ||
          legacyMessage is! String ||
          (traceId != null && traceId is! String)) {
        return null;
      }
      return ApiError(
        code: legacyCode,
        message: legacyMessage,
        fieldErrors: const [],
        traceId: traceId is String ? traceId : null,
      );
    }

    return null;
  }
}

class ApiInterceptor extends Interceptor {
  static const _refreshRetryKey = 'apiInterceptorRefreshRetryAttempted';

  ApiInterceptor(this._dio, {Dio? refreshDio})
    : _refreshDio = refreshDio ?? Dio();

  final Dio _dio;
  final Dio _refreshDio;
  Future<String?>? _refreshFuture;

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
      'ApiInterceptor.onRequest: method=${options.method} path=${options.uri.path}',
    );
    handler.next(options);
  }

  @override
  void onResponse(
    Response<dynamic> response,
    ResponseInterceptorHandler handler,
  ) {
    response.data = ApiInterceptor.unwrapApiResponse(response.data);
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    final apiError = ApiError.tryParse(err.response?.data);
    final responseError = apiError == null
        ? err
        : err.copyWith(message: apiError.message, error: apiError);
    debugPrint(
      'ApiInterceptor.onError: method=${err.requestOptions.method} path=${err.requestOptions.uri.path} status=${err.response?.statusCode} type=${err.type.name}',
    );
    if (err.response?.statusCode == 401 &&
        err.requestOptions.extra[_refreshRetryKey] != true) {
      try {
        final currentAccessToken = await SecureStorageService.getAccessToken();
        final requestAuthorization =
            err.requestOptions.headers['Authorization'];
        final accessToken =
            currentAccessToken != null &&
                requestAuthorization != 'Bearer $currentAccessToken'
            ? currentAccessToken
            : await _refreshAccessToken();
        if (accessToken == null) {
          handler.next(responseError);
          return;
        }

        const safeMethods = {'GET', 'HEAD', 'OPTIONS'};
        if (!safeMethods.contains(err.requestOptions.method.toUpperCase())) {
          handler.next(responseError);
          return;
        }

        err.requestOptions.extra[_refreshRetryKey] = true;
        err.requestOptions.headers['Authorization'] = 'Bearer $accessToken';
        final retryResponse = await _dio.fetch(err.requestOptions);
        handler.resolve(retryResponse);
      } catch (_) {
        await SecureStorageService.clearTokens();
        handler.next(responseError);
      }
    } else {
      handler.next(responseError);
    }
  }

  Future<String?> _refreshAccessToken() {
    final activeRefresh = _refreshFuture;
    if (activeRefresh != null) return activeRefresh;

    late final Future<String?> refreshFuture;
    refreshFuture = _performTokenRefresh().whenComplete(() {
      if (identical(_refreshFuture, refreshFuture)) {
        _refreshFuture = null;
      }
    });
    _refreshFuture = refreshFuture;
    return refreshFuture;
  }

  Future<String?> _performTokenRefresh() async {
    final refreshToken = await SecureStorageService.getRefreshToken();
    if (refreshToken == null) {
      await SecureStorageService.clearTokens();
      return null;
    }

    final response = await _refreshDio.post(
      '${ApiConstants.baseUrl}${ApiConstants.refresh}',
      data: {'refreshToken': refreshToken},
    );

    final tokenData = ApiInterceptor.unwrapApiResponse(response.data);
    if (tokenData is! Map<String, dynamic>) {
      throw const FormatException('Unexpected token refresh response');
    }
    final accessToken = tokenData['accessToken'];
    final refreshTokenResult = tokenData['refreshToken'];
    if (accessToken is! String || refreshTokenResult is! String) {
      throw const FormatException('Token refresh response is missing tokens');
    }
    await SecureStorageService.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshTokenResult,
    );
    return accessToken;
  }

  static dynamic unwrapApiResponse(dynamic body) {
    if (body is Map<String, dynamic> && body['code'] == 'SUCCESS') {
      return body['data'];
    }
    if (body is Map<String, dynamic> && body['success'] == true) {
      return body['data'];
    }
    return body;
  }
}
