import 'package:flutter/foundation.dart';
import 'dart:io';

class ApiConstants {
  ApiConstants._();

  static String get baseUrl {
    if (kIsWeb) return 'http://localhost:8080';
    if (Platform.isAndroid) return 'http://10.0.2.2:8080';
    return 'http://localhost:8080';
  }

  static const String login = '/api/v1/auth/login';
  static const String refresh = '/api/v1/auth/refresh';
  static const String positions = '/api/v1/portfolio/positions';
  static const String assetView = '/api/v1/portfolio/asset-view';
  static const String dailyPnl = '/api/v1/analytics/pnl/daily';
  static const String placeOrder = '/api/v1/trade/order';
  static const String orders = '/api/v1/orders';
  static const String trades = '/api/v1/trades';
  static const String stockSearch = '/api/v1/stocks/search';
  static const String watchlist = '/api/v1/watchlist';
}
