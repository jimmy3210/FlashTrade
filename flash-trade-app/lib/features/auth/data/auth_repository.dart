import 'package:dio/dio.dart';
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
    final response = await _dio.post(
      ApiConstants.login,
      data: {'username': username, 'password': password},
    );
    final data = response.data;
    if (data is! Map<String, dynamic>) {
      throw StateError('Unexpected login response format');
    }
    return LoginResponse.fromJson(data);
  }

  Future<void> logout({String? refreshToken}) async {
    await _dio.post(
      ApiConstants.logout,
      data: refreshToken == null ? null : {'refreshToken': refreshToken},
    );
  }
}
