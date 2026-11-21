import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flash_trade_app/core/constants/api_constants.dart';
import 'package:flash_trade_app/features/auth/data/auth_repository.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('logout sends the optional refresh token to the session endpoint', () async {
    final adapter = _RecordingAdapter();
    final dio = Dio(BaseOptions(baseUrl: 'https://test.example'));
    dio.httpClientAdapter = adapter;

    await AuthRepository(dio).logout(refreshToken: 'refresh-token');

    expect(adapter.requestOptions?.method, 'POST');
    expect(adapter.requestOptions?.path, ApiConstants.logout);
    expect(adapter.requestOptions?.data, {'refreshToken': 'refresh-token'});
  });
}

class _RecordingAdapter implements HttpClientAdapter {
  RequestOptions? requestOptions;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requestOptions = options;
    return ResponseBody.fromString('', 200);
  }

  @override
  void close({bool force = false}) {}
}
