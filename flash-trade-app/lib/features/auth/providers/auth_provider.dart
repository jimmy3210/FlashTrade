import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/constants/demo_mode.dart';
import '../../../core/network/dio_client.dart';
import '../../../core/storage/secure_storage.dart';
import '../data/auth_repository.dart';
import '../domain/auth_state.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(DioClient.instance.dio);
});

class AuthNotifier extends AsyncNotifier<AuthState> {
  @override
  Future<AuthState> build() async {
    if (kDemoMode) return const AuthUnauthenticated();
    final token = await SecureStorageService.getAccessToken();
    if (token != null) return AuthAuthenticated(token);
    return const AuthUnauthenticated();
  }

  Future<void> login(String username, String password) async {
    state = const AsyncLoading();

    // デモモード: demo/demo でローカル認証
    if (kDemoMode) {
      await Future.delayed(const Duration(milliseconds: 600));
      if (username == kDemoUsername && password == kDemoPassword) {
        state = const AsyncData(AuthAuthenticated('demo_token'));
      } else {
        state = const AsyncData(AuthError('demo/demo でログインしてください'));
      }
      return;
    }

    state = await AsyncValue.guard(() async {
      final repo = ref.read(authRepositoryProvider);
      final response = await repo.login(username, password);
      await SecureStorageService.saveTokens(
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      );
      return AuthAuthenticated(response.accessToken);
    });
  }

  Future<void> logout() async {
    if (!kDemoMode) await SecureStorageService.clearTokens();
    state = const AsyncData(AuthUnauthenticated());
  }
}

final authProvider = AsyncNotifierProvider<AuthNotifier, AuthState>(
  AuthNotifier.new,
);
