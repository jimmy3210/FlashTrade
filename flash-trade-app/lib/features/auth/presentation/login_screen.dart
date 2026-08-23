import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:window_manager/window_manager.dart';
import '../../../core/constants/app_colors.dart';
import '../../../l10n/app_localizations.dart';
import '../../../shared/widgets/tech_background.dart';
import '../domain/auth_state.dart';
import '../providers/auth_provider.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) return;
    await ref
        .read(authProvider.notifier)
        .login(_usernameController.text.trim(), _passwordController.text);
  }

  Future<void> _exitApp() async {
    if (!kIsWeb &&
        (Platform.isWindows || Platform.isMacOS || Platform.isLinux)) {
      await windowManager.close();
      return;
    }

    await SystemNavigator.pop();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final isLoading = ref.watch(authProvider).isLoading;

    ref.listen<AsyncValue<AuthState>>(authProvider, (_, next) {
      next.whenOrNull(
        data: (state) {
          if (state is AuthAuthenticated) {
            context.go('/home/dashboard');
          }
        },
        error: (e, _) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(_errorMessage(e, l10n)),
              backgroundColor: AppColors.loss,
            ),
          );
        },
      );
    });

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          const Positioned.fill(child: TechBackground()),
          Positioned(
            top: 16,
            right: 16,
            child: IconButton(
              onPressed: _exitApp,
              tooltip: '退出',
              icon: const Icon(Icons.close, color: AppColors.textPrimary),
              style: IconButton.styleFrom(
                backgroundColor: AppColors.cardBg.withValues(alpha: 0.7),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                  side: const BorderSide(color: AppColors.border),
                ),
              ),
            ),
          ),
          SafeArea(
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 430),
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 24,
                  ),
                  child: SingleChildScrollView(
                    child: Container(
                      padding: const EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: AppColors.cardBg.withValues(alpha: 0.92),
                        borderRadius: BorderRadius.circular(5),
                        border: Border.all(color: AppColors.border),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withAlpha(80),
                            blurRadius: 24,
                            offset: const Offset(0, 16),
                          ),
                        ],
                      ),
                      child: Form(
                        key: _formKey,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            Center(
                              child: Container(
                                width: 76,
                                height: 76,
                                padding: const EdgeInsets.all(14),
                                decoration: BoxDecoration(
                                  color: AppColors.secondaryBg,
                                  borderRadius: BorderRadius.circular(5),
                                  border: Border.all(color: AppColors.border),
                                ),
                                child: Image.asset(
                                  'assets/icons/ft_icon.png',
                                  fit: BoxFit.contain,
                                ),
                              ),
                            ),
                            const SizedBox(height: 18),
                            Text(
                              l10n.appName,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                                fontSize: 34,
                                fontWeight: FontWeight.bold,
                                letterSpacing: 1.4,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              l10n.loginSubtitle,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                color: AppColors.textSecondary,
                                fontSize: 15,
                              ),
                            ),
                            const SizedBox(height: 28),
                            TextFormField(
                              controller: _usernameController,
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                              ),
                              decoration: _inputDecoration(
                                l10n.loginUsername,
                                Icons.person_outline,
                              ),
                              validator: (v) => (v == null || v.isEmpty)
                                  ? l10n.loginErrorEmpty
                                  : null,
                            ),
                            const SizedBox(height: 16),
                            TextFormField(
                              controller: _passwordController,
                              style: const TextStyle(
                                color: AppColors.textPrimary,
                              ),
                              obscureText: _obscurePassword,
                              decoration:
                                  _inputDecoration(
                                    l10n.loginPassword,
                                    Icons.lock_outline,
                                  ).copyWith(
                                    suffixIcon: IconButton(
                                      icon: Icon(
                                        _obscurePassword
                                            ? Icons.visibility_off
                                            : Icons.visibility,
                                        color: AppColors.textSecondary,
                                      ),
                                      onPressed: () => setState(
                                        () => _obscurePassword =
                                            !_obscurePassword,
                                      ),
                                    ),
                                  ),
                              validator: (v) => (v == null || v.isEmpty)
                                  ? l10n.loginErrorEmpty
                                  : null,
                            ),
                            const SizedBox(height: 28),
                            SizedBox(
                              height: 52,
                              child: ElevatedButton(
                                onPressed: isLoading ? null : _login,
                                style: ElevatedButton.styleFrom(
                                  minimumSize: const Size.fromHeight(52),
                                  disabledBackgroundColor: AppColors.accent
                                      .withAlpha(100),
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(5),
                                  ),
                                ),
                                child: isLoading
                                    ? const SizedBox(
                                        width: 22,
                                        height: 22,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                          color: AppColors.textPrimary,
                                        ),
                                      )
                                    : Text(
                                        l10n.loginButton,
                                        style: const TextStyle(fontSize: 16),
                                      ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration _inputDecoration(String label, IconData icon) =>
      InputDecoration(
        labelText: label,
        prefixIcon: Icon(icon, size: 18, color: AppColors.textSecondary),
        labelStyle: const TextStyle(color: AppColors.textSecondary),
        filled: true,
        fillColor: AppColors.cardBg,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(5),
          borderSide: const BorderSide(color: AppColors.border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(5),
          borderSide: const BorderSide(color: AppColors.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(5),
          borderSide: const BorderSide(color: AppColors.accent),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(5),
          borderSide: const BorderSide(color: AppColors.loss),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(5),
          borderSide: const BorderSide(color: AppColors.loss),
        ),
      );

  String _errorMessage(Object e, AppLocalizations l10n) {
    final msg = e.toString();
    if (msg.contains('401')) return l10n.loginErrorInvalid;
    if (msg.contains('SocketException') || msg.contains('Network')) {
      return l10n.loginErrorNetwork;
    }
    return l10n.loginErrorFailed;
  }
}
