import 'package:dio/dio.dart';
import '../constants/api_constants.dart';
import 'api_interceptor.dart';

class DioClient {
  DioClient._internal() {
    _dio = Dio(
      BaseOptions(
        baseUrl: ApiConstants.baseUrl,
        connectTimeout: const Duration(seconds: 5),
        receiveTimeout: const Duration(seconds: 10),
        headers: {'Content-Type': 'application/json'},
      ),
    );
    _dio.interceptors.add(ApiInterceptor());
  }

  static final DioClient _instance = DioClient._internal();
  static DioClient get instance => _instance;

  late final Dio _dio;
  Dio get dio => _dio;
}
