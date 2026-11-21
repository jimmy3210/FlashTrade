import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flash_trade_app/core/constants/api_constants.dart';
import 'package:flash_trade_app/core/network/api_interceptor.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('unwrapApiResponse', () {
    test('unwraps successful API envelopes', () {
      final result = ApiInterceptor.unwrapApiResponse({
        'code': 'SUCCESS',
        'message': 'ok',
        'data': {'id': 42},
      });

      expect(result, {'id': 42});
    });

    test('leaves non-envelope responses unchanged', () {
      final body = {
        'items': [1, 2],
      };

      expect(ApiInterceptor.unwrapApiResponse(body), same(body));
    });

    test('continues to unwrap legacy successful responses during rollout', () {
      expect(
        ApiInterceptor.unwrapApiResponse({
          'success': true,
          'data': [1, 2],
        }),
        [1, 2],
      );
    });

    test('does not unwrap error envelopes', () {
      final body = {
        'code': 'ACCESS_DENIED',
        'message': 'forbidden',
        'data': {'traceId': 'trace-123', 'fieldErrors': []},
      };

      expect(ApiInterceptor.unwrapApiResponse(body), same(body));
    });
  });

  group('ApiError.tryParse', () {
    test('parses stable code, field errors, and trace ID', () {
      final error = ApiError.tryParse({
        'code': 'VALIDATION_ERROR',
        'message': '请求参数校验失败',
        'data': {
          'fieldErrors': [
            {'field': 'quantity', 'code': 'OUT_OF_RANGE', 'message': '数量超出范围'},
          ],
          'traceId': 'trace-123',
        },
      });

      expect(error, isNotNull);
      expect(error!.code, 'VALIDATION_ERROR');
      expect(error.message, '请求参数校验失败');
      expect(error.traceId, 'trace-123');
      expect(error.fieldErrors, hasLength(1));
      expect(error.fieldErrors.single.field, 'quantity');
      expect(error.fieldErrors.single.code, 'OUT_OF_RANGE');
    });

    test('rejects malformed unified error envelopes', () {
      expect(
        ApiError.tryParse({
          'code': 'ACCESS_DENIED',
          'message': 'forbidden',
          'data': {'traceId': 'trace-123', 'fieldErrors': 'invalid'},
        }),
        isNull,
      );
    });

    test('reads legacy error wrappers during rollout', () {
      final error = ApiError.tryParse({
        'success': false,
        'error': {
          'code': 'ACCESS_DENIED',
          'message': 'forbidden',
          'traceId': 'trace-legacy',
        },
      });

      expect(error, isNotNull);
      expect(error!.code, 'ACCESS_DENIED');
      expect(error.traceId, 'trace-legacy');
      expect(error.fieldErrors, isEmpty);
    });
  });

  group('ApiInterceptor token refresh', () {
    const storageChannel = MethodChannel(
      'plugins.it_nomads.com/flutter_secure_storage',
    );
    late Map<String, String> storedTokens;

    setUp(() {
      storedTokens = {};
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(storageChannel, (call) async {
            final arguments = call.arguments as Map<dynamic, dynamic>;
            final key = arguments['key'] as String;
            switch (call.method) {
              case 'read':
                return storedTokens[key];
              case 'write':
                storedTokens[key] = arguments['value'] as String;
                return null;
              case 'delete':
                storedTokens.remove(key);
                return null;
              default:
                throw MissingPluginException(
                  'Unexpected secure storage method: ${call.method}',
                );
            }
          });
    });

    tearDown(() {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(storageChannel, null);
    });

    test('retries safe reads after successfully refreshing tokens', () async {
      storedTokens['flash_trade_access_token'] = 'expired-access-token';
      storedTokens['flash_trade_refresh_token'] = 'valid-refresh-token';
      final requestAuthorizationHeaders = <String?>[];
      var refreshRequestCount = 0;

      final refreshDio = Dio()
        ..httpClientAdapter = _CallbackAdapter((options) async {
          refreshRequestCount++;
          return _jsonResponse({
            'code': 'SUCCESS',
            'message': 'ok',
            'data': {
              'accessToken': 'new-access-token',
              'refreshToken': 'new-refresh-token',
            },
          });
        });
      final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
      dio.httpClientAdapter = _CallbackAdapter((options) async {
        requestAuthorizationHeaders.add(
          options.headers['Authorization'] as String?,
        );
        if (requestAuthorizationHeaders.length == 1) {
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-read'},
          }, statusCode: 401);
        }
        return _jsonResponse({
          'code': 'SUCCESS',
          'message': 'ok',
          'data': {'positions': []},
        });
      });
      dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

      final response = await dio.get(ApiConstants.positions);

      expect(response.data, {'positions': []});
      expect(requestAuthorizationHeaders, [
        'Bearer expired-access-token',
        'Bearer new-access-token',
      ]);
      expect(refreshRequestCount, 1);
      expect(storedTokens['flash_trade_access_token'], 'new-access-token');
      expect(storedTokens['flash_trade_refresh_token'], 'new-refresh-token');
    });

    test(
      'stops after one retry when the refreshed token is also rejected',
      () async {
        storedTokens['flash_trade_access_token'] = 'expired-access-token';
        storedTokens['flash_trade_refresh_token'] = 'valid-refresh-token';
        var apiRequestCount = 0;
        var refreshRequestCount = 0;

        final refreshDio = Dio()
          ..httpClientAdapter = _CallbackAdapter((options) async {
            refreshRequestCount++;
            return _jsonResponse({
              'code': 'SUCCESS',
              'message': 'ok',
              'data': {
                'accessToken': 'new-access-token',
                'refreshToken': 'new-refresh-token',
              },
            });
          });
        final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
        dio.httpClientAdapter = _CallbackAdapter((options) async {
          apiRequestCount++;
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'token rejected',
            'data': {
              'fieldErrors': [],
              'traceId': 'trace-rejected-$apiRequestCount',
            },
          }, statusCode: 401);
        });
        dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

        DioException? requestError;
        try {
          await dio.get(ApiConstants.positions);
        } on DioException catch (error) {
          requestError = error;
        }

        expect(requestError, isNotNull);
        expect(requestError!.response?.statusCode, 401);
        expect((requestError.error as ApiError).traceId, 'trace-rejected-1');
        expect(apiRequestCount, 2);
        expect(refreshRequestCount, 1);
        expect(storedTokens, isEmpty);
      },
    );

    test('shares one token refresh across concurrent safe reads', () async {
      storedTokens['flash_trade_access_token'] = 'expired-access-token';
      storedTokens['flash_trade_refresh_token'] = 'valid-refresh-token';
      final initialRequestsComplete = Completer<void>();
      final refreshStarted = Completer<void>();
      final finishRefresh = Completer<void>();
      final requestAuthorizationHeaders = <String?>[];
      var apiRequestCount = 0;
      var refreshRequestCount = 0;

      final refreshDio = Dio()
        ..httpClientAdapter = _CallbackAdapter((options) async {
          refreshRequestCount++;
          refreshStarted.complete();
          await finishRefresh.future;
          return _jsonResponse({
            'code': 'SUCCESS',
            'message': 'ok',
            'data': {
              'accessToken': 'new-access-token',
              'refreshToken': 'new-refresh-token',
            },
          });
        });
      final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
      dio.httpClientAdapter = _CallbackAdapter((options) async {
        apiRequestCount++;
        requestAuthorizationHeaders.add(
          options.headers['Authorization'] as String?,
        );
        if (apiRequestCount <= 2) {
          if (apiRequestCount == 2) initialRequestsComplete.complete();
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-concurrent'},
          }, statusCode: 401);
        }
        return _jsonResponse({
          'code': 'SUCCESS',
          'message': 'ok',
          'data': {'positions': []},
        });
      });
      dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

      final requests = [
        dio.get(ApiConstants.positions),
        dio.get(ApiConstants.positions),
      ];
      await initialRequestsComplete.future;
      await refreshStarted.future;
      await Future<void>.delayed(Duration.zero);
      finishRefresh.complete();

      final responses = await Future.wait(requests);

      expect(responses, hasLength(2));
      expect(responses.map((response) => response.data), [
        {'positions': []},
        {'positions': []},
      ]);
      expect(apiRequestCount, 4);
      expect(refreshRequestCount, 1);
      expect(
        requestAuthorizationHeaders.take(2),
        everyElement('Bearer expired-access-token'),
      );
      expect(
        requestAuthorizationHeaders.skip(2),
        everyElement('Bearer new-access-token'),
      );
    });

    test(
      'clears tokens and preserves the original error when refresh fails',
      () async {
        storedTokens['flash_trade_access_token'] = 'expired-access-token';
        storedTokens['flash_trade_refresh_token'] = 'expired-refresh-token';
        var apiRequestCount = 0;
        var refreshRequestCount = 0;

        final refreshDio = Dio()
          ..httpClientAdapter = _CallbackAdapter((options) async {
            refreshRequestCount++;
            return _jsonResponse({
              'code': 'REFRESH_TOKEN_EXPIRED',
              'message': 'refresh token expired',
              'data': {'fieldErrors': [], 'traceId': 'trace-refresh'},
            }, statusCode: 401);
          });
        final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
        dio.httpClientAdapter = _CallbackAdapter((options) async {
          apiRequestCount++;
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'access token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-original'},
          }, statusCode: 401);
        });
        dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

        DioException? requestError;
        try {
          await dio.get(ApiConstants.positions);
        } on DioException catch (error) {
          requestError = error;
        }

        expect(requestError, isNotNull);
        expect(requestError!.response?.statusCode, 401);
        expect((requestError.error as ApiError).code, 'UNAUTHORIZED');
        expect((requestError.error as ApiError).traceId, 'trace-original');
        expect(apiRequestCount, 1);
        expect(refreshRequestCount, 1);
        expect(storedTokens, isEmpty);
      },
    );

    test(
      'clears tokens without calling refresh when no refresh token exists',
      () async {
        storedTokens['flash_trade_access_token'] = 'expired-access-token';
        var apiRequestCount = 0;
        var refreshRequestCount = 0;

        final refreshDio = Dio()
          ..httpClientAdapter = _CallbackAdapter((options) async {
            refreshRequestCount++;
            return _jsonResponse({
              'code': 'SUCCESS',
              'message': 'ok',
              'data': {
                'accessToken': 'unexpected-access-token',
                'refreshToken': 'unexpected-refresh-token',
              },
            });
          });
        final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
        dio.httpClientAdapter = _CallbackAdapter((options) async {
          apiRequestCount++;
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'access token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-no-refresh-token'},
          }, statusCode: 401);
        });
        dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

        DioException? requestError;
        try {
          await dio.get(ApiConstants.positions);
        } on DioException catch (error) {
          requestError = error;
        }

        expect(requestError, isNotNull);
        expect(requestError!.response?.statusCode, 401);
        expect(
          (requestError.error as ApiError).traceId,
          'trace-no-refresh-token',
        );
        expect(apiRequestCount, 1);
        expect(refreshRequestCount, 0);
        expect(storedTokens, isEmpty);
      },
    );

    test(
      'does not automatically retry order submissions after refresh',
      () async {
        storedTokens['flash_trade_access_token'] = 'expired-access-token';
        storedTokens['flash_trade_refresh_token'] = 'valid-refresh-token';
        var orderRequestCount = 0;
        var refreshRequestCount = 0;

        final refreshDio = Dio()
          ..httpClientAdapter = _CallbackAdapter((options) async {
            refreshRequestCount++;
            return _jsonResponse({
              'code': 'SUCCESS',
              'message': 'ok',
              'data': {
                'accessToken': 'new-access-token',
                'refreshToken': 'new-refresh-token',
              },
            });
          });
        final dio = Dio(BaseOptions(baseUrl: ApiConstants.baseUrl));
        dio.httpClientAdapter = _CallbackAdapter((options) async {
          orderRequestCount++;
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-order'},
          }, statusCode: 401);
        });
        dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));

        DioException? requestError;
        try {
          await dio.post(
            ApiConstants.placeOrder,
            data: {'symbol': 'AAPL', 'quantity': 1},
          );
        } on DioException catch (error) {
          requestError = error;
        }

        expect(requestError, isNotNull);
        expect(requestError!.response?.statusCode, 401);
        expect((requestError.error as ApiError).message, 'token expired');
        expect(orderRequestCount, 1);
        expect(refreshRequestCount, 1);
        expect(storedTokens['flash_trade_access_token'], 'new-access-token');
      },
    );
  });
}

class _CallbackAdapter implements HttpClientAdapter {
  _CallbackAdapter(this.onFetch);

  final Future<ResponseBody> Function(RequestOptions options) onFetch;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) => onFetch(options);

  @override
  void close({bool force = false}) {}
}

ResponseBody _jsonResponse(Map<String, dynamic> body, {int statusCode = 200}) =>
    ResponseBody.fromString(
      jsonEncode(body),
      statusCode,
      headers: {
        Headers.contentTypeHeader: ['application/json; charset=utf-8'],
      },
    );
