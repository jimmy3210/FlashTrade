import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flash_trade_app/core/constants/api_constants.dart';
import 'package:flash_trade_app/core/network/api_interceptor.dart';
import 'package:flash_trade_app/features/auth/data/auth_repository.dart';
import 'package:flash_trade_app/features/auth/domain/auth_state.dart';
import 'package:flash_trade_app/features/auth/providers/auth_provider.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const storageChannel = MethodChannel(
    'plugins.it_nomads.com/flutter_secure_storage',
  );
  late Map<String, String> storedTokens;

  setUp(() {
    storedTokens = {
      'flash_trade_access_token': 'access-token',
      'flash_trade_refresh_token': 'refresh-token',
    };
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
              throw UnsupportedError('Unexpected storage call: ${call.method}');
          }
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(storageChannel, null);
  });

  group('AuthNotifier session lifecycle', () {
    test('login, refresh, and logout use the rotated token pair', () async {
      storedTokens.clear();
      final adapter = _SessionFlowAdapter();
      final refreshDio = Dio()..httpClientAdapter = adapter;
      final dio = Dio(BaseOptions(baseUrl: 'https://test.example'))
        ..httpClientAdapter = adapter;
      dio.interceptors.add(ApiInterceptor(dio, refreshDio: refreshDio));
      addTearDown(() => dio.close(force: true));
      addTearDown(() => refreshDio.close(force: true));

      final container = ProviderContainer(
        overrides: [
          authRepositoryProvider.overrideWithValue(AuthRepository(dio)),
        ],
      );
      addTearDown(container.dispose);

      await container.read(authProvider.future);
      await container.read(authProvider.notifier).login('trader', 'password');

      expect(storedTokens['flash_trade_access_token'], 'initial-access');
      expect(storedTokens['flash_trade_refresh_token'], 'initial-refresh');

      final positions = await dio.get(ApiConstants.positions);

      expect(positions.data, {'positions': []});
      expect(adapter.positionsAuthorizationHeaders, [
        'Bearer initial-access',
        'Bearer rotated-access',
      ]);
      expect(adapter.refreshRequestBody, {'refreshToken': 'initial-refresh'});
      expect(storedTokens['flash_trade_access_token'], 'rotated-access');
      expect(storedTokens['flash_trade_refresh_token'], 'rotated-refresh');

      final logoutResult = await container.read(authProvider.notifier).logout();

      expect(logoutResult, LogoutResult.completed);
      expect(adapter.logoutAuthorization, 'Bearer rotated-access');
      expect(adapter.logoutRequestBody, {'refreshToken': 'rotated-refresh'});
      expect(adapter.loginRequestCount, 1);
      expect(adapter.refreshRequestCount, 1);
      expect(adapter.positionsRequestCount, 2);
      expect(adapter.logoutRequestCount, 1);
      expect(storedTokens, isEmpty);
      expect(container.read(authProvider).value, isA<AuthUnauthenticated>());
    });

    test('login saves both tokens returned by the repository', () async {
      final repository = _FakeAuthRepository(
        loginResult: const LoginResponse(
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token',
        ),
      );
      final container = ProviderContainer(
        overrides: [authRepositoryProvider.overrideWithValue(repository)],
      );
      addTearDown(container.dispose);

      await container.read(authProvider.future);
      await container.read(authProvider.notifier).login('trader', 'password');

      expect(storedTokens['flash_trade_access_token'], 'new-access-token');
      expect(storedTokens['flash_trade_refresh_token'], 'new-refresh-token');
      expect(container.read(authProvider).value, isA<AuthAuthenticated>());
    });

    test('clears local tokens after confirmed server revocation', () async {
      final repository = _FakeAuthRepository();
      final container = ProviderContainer(
        overrides: [authRepositoryProvider.overrideWithValue(repository)],
      );
      addTearDown(container.dispose);

      await container.read(authProvider.future);
      final result = await container.read(authProvider.notifier).logout();

      expect(result, LogoutResult.completed);
      expect(repository.logoutCalls, 1);
      expect(repository.lastRefreshToken, 'refresh-token');
      expect(storedTokens, isEmpty);
      expect(container.read(authProvider).value, isA<AuthUnauthenticated>());
    });

    test('clears local tokens when server revocation is unconfirmed', () async {
      final repository = _FakeAuthRepository(
        failure: DioException(
          requestOptions: RequestOptions(path: '/api/v1/auth/logout'),
          type: DioExceptionType.connectionError,
        ),
      );
      final container = ProviderContainer(
        overrides: [authRepositoryProvider.overrideWithValue(repository)],
      );
      addTearDown(container.dispose);

      await container.read(authProvider.future);
      final result = await container.read(authProvider.notifier).logout();

      expect(result, LogoutResult.serverRevocationUnconfirmed);
      expect(repository.logoutCalls, 1);
      expect(repository.lastRefreshToken, 'refresh-token');
      expect(storedTokens, isEmpty);
      expect(container.read(authProvider).value, isA<AuthUnauthenticated>());
    });
  });
}

class _FakeAuthRepository extends AuthRepository {
  _FakeAuthRepository({
    this.failure,
    this.loginResult = const LoginResponse(
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
    ),
  }) : super(Dio());

  final DioException? failure;
  final LoginResponse loginResult;
  int logoutCalls = 0;
  String? lastRefreshToken;

  @override
  Future<LoginResponse> login(String username, String password) async {
    return loginResult;
  }

  @override
  Future<void> logout({String? refreshToken}) async {
    logoutCalls++;
    lastRefreshToken = refreshToken;
    final error = failure;
    if (error != null) throw error;
  }
}

class _SessionFlowAdapter implements HttpClientAdapter {
  int loginRequestCount = 0;
  int refreshRequestCount = 0;
  int positionsRequestCount = 0;
  int logoutRequestCount = 0;
  final List<String?> positionsAuthorizationHeaders = [];
  String? logoutAuthorization;
  Map<String, dynamic>? refreshRequestBody;
  Map<String, dynamic>? logoutRequestBody;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    final requestBody = await _decodeRequestBody(options, requestStream);
    switch (options.uri.path) {
      case ApiConstants.login:
        loginRequestCount++;
        return _successResponse({
          'accessToken': 'initial-access',
          'refreshToken': 'initial-refresh',
        });
      case ApiConstants.refresh:
        refreshRequestCount++;
        refreshRequestBody = requestBody;
        return _successResponse({
          'accessToken': 'rotated-access',
          'refreshToken': 'rotated-refresh',
        });
      case ApiConstants.positions:
        positionsRequestCount++;
        positionsAuthorizationHeaders.add(
          options.headers['Authorization'] as String?,
        );
        if (positionsRequestCount == 1) {
          return _jsonResponse({
            'code': 'UNAUTHORIZED',
            'message': 'token expired',
            'data': {'fieldErrors': [], 'traceId': 'trace-session-refresh'},
          }, statusCode: 401);
        }
        return _successResponse({'positions': []});
      case ApiConstants.logout:
        logoutRequestCount++;
        logoutAuthorization = options.headers['Authorization'] as String?;
        logoutRequestBody = requestBody;
        return _successResponse(null);
      default:
        throw StateError(
          'Unexpected mocked HTTP request ${options.method} ${options.uri.path}',
        );
    }
  }

  @override
  void close({bool force = false}) {}

  Future<Map<String, dynamic>?> _decodeRequestBody(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
  ) async {
    if (requestStream == null) {
      final data = options.data;
      return data is Map ? Map<String, dynamic>.from(data) : null;
    }

    final bytes = <int>[];
    await for (final chunk in requestStream) {
      bytes.addAll(chunk);
    }
    if (bytes.isEmpty) return null;
    return Map<String, dynamic>.from(jsonDecode(utf8.decode(bytes)) as Map);
  }

  ResponseBody _successResponse(dynamic data) =>
      _jsonResponse({'code': 'SUCCESS', 'message': 'ok', 'data': data});

  ResponseBody _jsonResponse(
    Map<String, dynamic> body, {
    int statusCode = 200,
  }) => ResponseBody.fromString(
    jsonEncode(body),
    statusCode,
    headers: {
      Headers.contentTypeHeader: ['application/json; charset=utf-8'],
    },
  );
}
