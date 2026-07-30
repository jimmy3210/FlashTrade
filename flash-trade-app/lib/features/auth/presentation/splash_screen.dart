import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/storage/secure_storage.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _navigate();
  }

  Future<void> _navigate() async {
    await Future.delayed(const Duration(milliseconds: 1500));
    if (!mounted) return;
    final token = await SecureStorageService.getAccessToken();
    if (!mounted) return;
    if (token != null) {
      context.go('/home/dashboard');
    } else {
      context.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 96,
              height: 96,
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.cardBg,
                borderRadius: BorderRadius.all(Radius.circular(28)),
                border: Border.fromBorderSide(
                  BorderSide(color: AppColors.border),
                ),
                boxShadow: [
                  BoxShadow(
                    color: Color(0x33000000),
                    blurRadius: 20,
                    offset: Offset(0, 12),
                  ),
                ],
              ),
              child: Image(
                image: AssetImage('assets/icons/ft_icon.png'),
                fit: BoxFit.contain,
              ),
            ),
            SizedBox(height: 20),
            Text(
              'FlashTrade',
              style: TextStyle(
                color: AppColors.textPrimary,
                fontSize: 36,
                fontWeight: FontWeight.bold,
                letterSpacing: 2,
              ),
            ),
            SizedBox(height: 8),
            Text(
              'Stock Trading Platform',
              style: TextStyle(color: AppColors.textSecondary, fontSize: 14),
            ),
            SizedBox(height: 36),
            CircularProgressIndicator(color: AppColors.accent, strokeWidth: 2),
          ],
        ),
      ),
    );
  }
}
