import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../../../core/constants/api_constants.dart';

class LoginResponse {
  const LoginResponse({required this.accessToken, required this.refreshToken});

  final String accessToken;
  final String refreshToken;

  factory LoginResponse.fromJson(Map<String, dynamic> json) => LoginResponse(
    accessToken: json['accessToken'] as String,
    refreshToken: json['refreshToken'] as String,
  );
}

class AuthRepository {
  const AuthRepository(this._dio);
  final Dio _dio;

  Future<LoginResponse> login(String username, String password) async {
    debugPrint(
      'AuthRepository.login: url=${ApiConstants.baseUrl}${ApiConstants.login} username=$username passwordLength=${password.length}',
    );
    try {
      final response = await _dio.post(
        ApiConstants.login,
        data: {'username': username, 'password': password},
      );
      debugPrint(
        'AuthRepository.login success: status=${response.statusCode} data=${response.data}',
      );
      final data =
          (response.data as Map<String, dynamic>)['data']
              as Map<String, dynamic>?;
      if (data == null) {
        throw StateError('Unexpected login response format: ${response.data}');
      }
      return LoginResponse.fromJson(data);
    } on DioException catch (e) {
      debugPrint(
        'AuthRepository.login DioException: message=${e.message} status=${e.response?.statusCode} response=${e.response?.data}',
      );
      rethrow;
    } catch (e, st) {
      debugPrint('AuthRepository.login error: $e\n$st');
      rethrow;
    }
  }
}
