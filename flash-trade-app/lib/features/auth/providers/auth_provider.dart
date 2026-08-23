import 'package:flutter_riverpod/flutter_riverpod.dart';
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
    final token = await SecureStorageService.getAccessToken();
    if (token != null) return AuthAuthenticated(token);
    return const AuthUnauthenticated();
  }

  Future<void> login(String username, String password) async {
    state = const AsyncLoading();

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
    await SecureStorageService.clearTokens();
    state = const AsyncData(AuthUnauthenticated());
  }
}

final authProvider = AsyncNotifierProvider<AuthNotifier, AuthState>(
  AuthNotifier.new,
);
