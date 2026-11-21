import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'core/constants/app_colors.dart';
import 'features/auth/presentation/login_screen.dart';
import 'features/auth/presentation/logout_action.dart';
import 'features/auth/presentation/splash_screen.dart';
import 'features/desktop/desktop_dashboard.dart';
import 'features/orders/presentation/orders_screen.dart';
import 'features/portfolio/presentation/portfolio_screen.dart';
import 'features/trade/presentation/trade_screen.dart';
import 'l10n/app_localizations.dart';

// ─────────────────────────────────────────
// ルーター定義
// ─────────────────────────────────────────
final _router = GoRouter(
  initialLocation: '/splash',
  routes: [
    GoRoute(path: '/splash', builder: (_, _) => const SplashScreen()),
    GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
    ShellRoute(
      builder: (context, state, child) =>
          MainShell(location: state.uri.path, child: child),
      routes: [
        GoRoute(
          path: '/home/dashboard',
          builder: (_, _) => const DesktopDashboard(),
        ),
        GoRoute(
          path: '/home/portfolio',
          builder: (_, _) => const PortfolioScreen(),
        ),
        GoRoute(path: '/home/trade', builder: (_, _) => const TradeScreen()),
        GoRoute(path: '/home/orders', builder: (_, _) => const OrdersScreen()),
      ],
    ),
  ],
);

// ─────────────────────────────────────────
// アプリルート
// ─────────────────────────────────────────
class FlashTradeApp extends StatelessWidget {
  const FlashTradeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'FlashTrade',
      debugShowCheckedModeBanner: false,
      theme: _buildDarkTheme(),
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
      locale: const Locale('zh'),
      routerConfig: _router,
    );
  }

  ThemeData _buildDarkTheme() => ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: AppColors.primaryBg,
    colorScheme: const ColorScheme.dark(
      primary: AppColors.accent,
      secondary: AppColors.accent,
      surface: AppColors.cardBg,
      error: AppColors.loss,
      onPrimary: AppColors.textPrimary,
      onSurface: AppColors.textPrimary,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: AppColors.primaryBg,
      foregroundColor: AppColors.textPrimary,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      titleTextStyle: TextStyle(
        color: AppColors.textPrimary,
        fontSize: 18,
        fontWeight: FontWeight.w600,
      ),
      iconTheme: IconThemeData(color: AppColors.textPrimary),
      actionsIconTheme: IconThemeData(color: AppColors.textSecondary),
    ),
    cardTheme: const CardThemeData(
      color: AppColors.cardBg,
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(16)),
        side: BorderSide(color: AppColors.border),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: AppColors.cardBg,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.border),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.accent),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.loss),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: AppColors.loss),
      ),
      hintStyle: const TextStyle(color: AppColors.textSecondary),
      labelStyle: const TextStyle(color: AppColors.textSecondary),
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: AppColors.accent,
        foregroundColor: AppColors.textPrimary,
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: AppColors.cardBg,
      selectedItemColor: AppColors.accent,
      unselectedItemColor: AppColors.textSecondary,
      type: BottomNavigationBarType.fixed,
      elevation: 0,
    ),
    navigationRailTheme: const NavigationRailThemeData(
      backgroundColor: AppColors.cardBg,
      selectedIconTheme: IconThemeData(color: AppColors.accent),
      unselectedIconTheme: IconThemeData(color: AppColors.textSecondary),
      selectedLabelTextStyle: TextStyle(color: AppColors.accent),
      unselectedLabelTextStyle: TextStyle(color: AppColors.textSecondary),
    ),
    dividerColor: AppColors.border,
    cardColor: AppColors.cardBg,
    dialogTheme: const DialogThemeData(backgroundColor: AppColors.cardBg),
    tabBarTheme: const TabBarThemeData(
      indicatorColor: AppColors.accent,
      labelColor: AppColors.accent,
      unselectedLabelColor: AppColors.textSecondary,
    ),
    snackBarTheme: const SnackBarThemeData(
      backgroundColor: AppColors.cardBg,
      contentTextStyle: TextStyle(color: AppColors.textPrimary),
    ),
  );
}

// ─────────────────────────────────────────
// メインシェル（ナビゲーション共通）
// レスポンシブ: 幅 >= 800px → NavigationRail
//              幅  < 800px → BottomNavigationBar
// ─────────────────────────────────────────
class MainShell extends StatelessWidget {
  const MainShell({super.key, required this.location, required this.child});

  final String location;
  final Widget child;

  int get _currentIndex {
    if (location.startsWith('/home/portfolio')) return 1;
    if (location.startsWith('/home/trade')) return 2;
    if (location.startsWith('/home/orders')) return 3;
    return 0;
  }

  void _onTap(BuildContext context, int index) {
    const routes = [
      '/home/dashboard',
      '/home/portfolio',
      '/home/trade',
      '/home/orders',
    ];
    context.go(routes[index]);
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final destinations = [
      _NavDestination(
        icon: Icons.home_outlined,
        activeIcon: Icons.home,
        label: l10n.navHome,
      ),
      _NavDestination(
        icon: Icons.pie_chart_outline,
        activeIcon: Icons.pie_chart,
        label: l10n.navPortfolio,
      ),
      _NavDestination(
        icon: Icons.add_circle_outline,
        activeIcon: Icons.add_circle,
        label: l10n.navTrade,
      ),
      _NavDestination(
        icon: Icons.history_outlined,
        activeIcon: Icons.history,
        label: l10n.navOrders,
      ),
    ];

    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth >= 800) {
          // ── デスクトップレイアウト: ダッシュボード専用画面、その他ページ共通侧边栏布局 ──
          if (location == '/home/dashboard') {
            return const DesktopDashboard();
          }
          return _DesktopShell(location: location, child: child);
        } else {
          // ── モバイルレイアウト (iOS / Android) ──
          return Scaffold(
            backgroundColor: AppColors.primaryBg,
            body: child,
            bottomNavigationBar: BottomNavigationBar(
              currentIndex: _currentIndex,
              onTap: (i) => _onTap(context, i),
              items: destinations
                  .map(
                    (d) => BottomNavigationBarItem(
                      icon: Icon(d.icon),
                      activeIcon: Icon(d.activeIcon),
                      label: d.label,
                    ),
                  )
                  .toList(),
            ),
          );
        }
      },
    );
  }
}

class _DesktopShell extends ConsumerWidget {
  const _DesktopShell({required this.location, required this.child});

  final String location;
  final Widget child;

  bool _isSelected(String route) => location == route;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      backgroundColor: AppColors.primaryBg,
      body: Row(
        children: [
          Container(
            width: 72,
            padding: const EdgeInsets.symmetric(vertical: 20),
            decoration: BoxDecoration(
              color: AppColors.cardBg,
              border: Border(right: BorderSide(color: AppColors.border)),
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  children: [
                    _DesktopNavButton(
                      icon: Icons.home,
                      label: l10n.navHome,
                      active: _isSelected('/home/dashboard'),
                      onTap: () => GoRouter.of(context).go('/home/dashboard'),
                    ),
                    const SizedBox(height: 12),
                    _DesktopNavButton(
                      icon: Icons.pie_chart_outline,
                      label: l10n.navPortfolio,
                      active: _isSelected('/home/portfolio'),
                      onTap: () => GoRouter.of(context).go('/home/portfolio'),
                    ),
                    const SizedBox(height: 12),
                    _DesktopNavButton(
                      icon: Icons.add_circle_outline,
                      label: l10n.navTrade,
                      active: _isSelected('/home/trade'),
                      onTap: () => GoRouter.of(context).go('/home/trade'),
                    ),
                    const SizedBox(height: 12),
                    _DesktopNavButton(
                      icon: Icons.history,
                      label: l10n.navOrders,
                      active: _isSelected('/home/orders'),
                      onTap: () => GoRouter.of(context).go('/home/orders'),
                    ),
                  ],
                ),
                InkWell(
                  onTap: () async {
                    await logoutAndNavigate(context, ref);
                  },
                  borderRadius: BorderRadius.circular(14),
                  child: Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: AppColors.secondaryBg,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(
                      Icons.logout,
                      color: AppColors.textSecondary,
                      size: 22,
                    ),
                  ),
                ),
              ],
            ),
          ),
          Expanded(child: child),
        ],
      ),
    );
  }
}

class _DesktopNavButton extends StatelessWidget {
  const _DesktopNavButton({
    required this.icon,
    required this.label,
    required this.onTap,
    required this.active,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool active;

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: label,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        hoverColor: AppColors.accent.withAlpha(20),
        child: Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            color: active ? AppColors.accent.withAlpha(40) : Colors.transparent,
            borderRadius: BorderRadius.circular(16),
          ),
          alignment: Alignment.center,
          child: Icon(
            icon,
            size: 22,
            color: active ? AppColors.accent : AppColors.textSecondary,
          ),
        ),
      ),
    );
  }
}

class _NavDestination {
  const _NavDestination({
    required this.icon,
    required this.activeIcon,
    required this.label,
  });
  final IconData icon;
  final IconData activeIcon;
  final String label;
}
