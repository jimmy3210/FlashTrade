import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../l10n/app_localizations.dart';
import '../providers/auth_provider.dart';

Future<void> logoutAndNavigate(BuildContext context, WidgetRef ref) async {
  final messenger = ScaffoldMessenger.of(context);
  final failureMessage = AppLocalizations.of(
    context,
  ).logoutServerRevocationUnconfirmed;
  final result = await ref.read(authProvider.notifier).logout();
  if (!context.mounted) return;

  if (result == LogoutResult.serverRevocationUnconfirmed) {
    messenger.showSnackBar(SnackBar(content: Text(failureMessage)));
  }
  context.go('/login');
}
