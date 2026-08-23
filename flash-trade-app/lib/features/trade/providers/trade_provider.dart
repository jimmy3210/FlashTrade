import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/dio_client.dart';
import '../data/trade_repository.dart';
import '../domain/trade_form_state.dart';

final tradeRepositoryProvider = Provider<TradeRepository>((ref) {
  return TradeRepository(DioClient.instance.dio);
});

/// 銘柄プリセット用プロバイダー（ポートフォリオ画面からタップ時に設定）
final selectedSymbolProvider = StateProvider<String>((ref) => '');

class TradeFormNotifier extends StateNotifier<TradeFormState> {
  TradeFormNotifier(this._ref) : super(const TradeFormState());
  final Ref _ref;

  void setSymbol(String v) => state = state.copyWith(symbol: v.toUpperCase());
  void setSide(String v) => state = state.copyWith(side: v);
  void setOrderType(String v) => state = state.copyWith(orderType: v);
  void setQuantity(String v) => state = state.copyWith(quantity: v);
  void setPrice(String v) => state = state.copyWith(price: v);

  Future<bool> submitOrder() async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final repo = _ref.read(tradeRepositoryProvider);
      await repo.placeOrder(
        symbol: state.symbol,
        side: state.side,
        orderType: state.orderType,
        quantity: int.parse(state.quantity),
        price: state.orderType == 'LIMIT' ? double.parse(state.price) : null,
      );
      state = const TradeFormState();
      return true;
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: '発注に失敗しました');
      return false;
    }
  }

  void reset() => state = const TradeFormState();
}

final tradeFormProvider =
    StateNotifierProvider<TradeFormNotifier, TradeFormState>(
      (ref) => TradeFormNotifier(ref),
    );
