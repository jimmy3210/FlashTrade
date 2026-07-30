import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../l10n/app_localizations.dart';

class StatusBadge extends StatelessWidget {
  const StatusBadge(this.status, {super.key});
  final String status;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final (color, label) = switch (status.toUpperCase()) {
      'PENDING' => (AppColors.warning, l10n.statusPending),
      'FILLED' => (AppColors.gain, l10n.statusFilled),
      'CANCELLED' => (AppColors.textSecondary, l10n.statusCancelled),
      'REJECTED' => (AppColors.loss, l10n.statusUnknown),
      _ => (AppColors.textSecondary, status),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withAlpha(38),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: color.withAlpha(128)),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

class OrderTypeBadge extends StatelessWidget {
  const OrderTypeBadge(this.orderType, {super.key});
  final String orderType;

  @override
  Widget build(BuildContext context) {
    final isLimit = orderType.toUpperCase() == 'LIMIT';
    final l10n = AppLocalizations.of(context);
    final label = isLimit ? l10n.orderTypeLimit : l10n.orderTypeMarket;
    final color = isLimit ? AppColors.accent : AppColors.textSecondary;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: color.withAlpha(30),
        borderRadius: BorderRadius.circular(3),
        border: Border.all(color: color.withAlpha(100)),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontSize: 10,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
