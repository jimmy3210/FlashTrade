import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';

/// TechBackground
///
/// An animated, lightweight "科技纹理" background implemented with
/// `CustomPainter`. It draws a deep radial/linear gradient base, faint
/// grid lines and glowing nodes. A slow animation translates the grid
/// and pulses node intensities to produce a subtle dynamic effect.
class TechBackground extends StatefulWidget {
  const TechBackground({super.key, this.animate = true});

  /// When false the background is static (useful for screenshots/tests).
  final bool animate;

  @override
  State<TechBackground> createState() => _TechBackgroundState();
}

class _TechBackgroundState extends State<TechBackground>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 40),
    );
    if (widget.animate) {
      _ctrl.repeat();
    } else {
      _ctrl.value = 0.0;
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return RepaintBoundary(
      child: AnimatedBuilder(
        animation: _ctrl,
        builder: (context, child) {
          return CustomPaint(
            painter: _TechPainter(progress: _ctrl.value),
            size: Size.infinite,
          );
        },
      ),
    );
  }
}

class _TechPainter extends CustomPainter {
  _TechPainter({required this.progress});

  final double progress; // 0.0 - 1.0

  @override
  void paint(Canvas canvas, Size size) {
    _drawBaseGradient(canvas, size);
    _drawGrid(canvas, size);
    _drawNodes(canvas, size);
  }

  void _drawBaseGradient(Canvas canvas, Size size) {
    final Rect rect = Offset.zero & size;
    final Gradient g = RadialGradient(
      center: const Alignment(0.9, -0.8),
      radius: 1.2,
      colors: [const Color(0xFF071126), const Color(0xFF0B0E14)],
      stops: const [0.0, 1.0],
    );
    final Paint p = Paint()..shader = g.createShader(rect);
    canvas.drawRect(rect, p);

    // Add a faint linear overlay to give depth
    final Gradient lin = LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: [Colors.transparent, Colors.black.withValues(alpha: 0.25)],
    );
    final Paint overlay = Paint()..shader = lin.createShader(rect);
    canvas.drawRect(rect, overlay);
  }

  void _drawGrid(Canvas canvas, Size size) {
    final Paint paint = Paint()
      ..color = const Color(0xFF2196F3).withValues(alpha: 0.04)
      ..strokeWidth = 1.0
      ..style = PaintingStyle.stroke;

    // Base spacing and an offset driven by progress for slow motion
    final double base = 60.0;
    final double offsetX = (progress * base * 2) % base;
    final double offsetY = (progress * base * 1.5) % base;

    // Draw vertical lines
    for (double x = -base; x <= size.width + base; x += base) {
      final double fx = x + offsetX - base;
      canvas.drawLine(Offset(fx, 0), Offset(fx, size.height), paint);
    }

    // Draw horizontal lines
    for (double y = -base; y <= size.height + base; y += base) {
      final double fy = y + offsetY - base;
      canvas.drawLine(Offset(0, fy), Offset(size.width, fy), paint);
    }

    // Add a subtle diagonal accent lines (sparse)
    final Paint diag = Paint()
      ..color = const Color(0xFF2196F3).withValues(alpha: 0.02)
      ..strokeWidth = 1.0;
    for (double i = -size.height; i < size.width; i += 220) {
      canvas.drawLine(
        Offset(i + progress * 40, 0),
        Offset(i - size.height + progress * 40, size.height),
        diag,
      );
    }
  }

  void _drawNodes(Canvas canvas, Size size) {
    // Draw a handful of glowing nodes whose positions slowly oscillate.
    final int count = 9;
    final double seed = progress * pi * 2;
    for (int i = 0; i < count; i++) {
      final double t = i / (count - 1);
      // base positions spread across the screen but slightly biased
      final double bx = lerpDouble(size.width * 0.1, size.width * 0.9, t)!;
      final double by = lerpDouble(
        size.height * 0.15,
        size.height * 0.85,
        1 - t,
      )!;

      // small oscillation
      final double ox = sin(seed + i * 0.7) * 40.0 * (0.5 + 0.5 * sin(i));
      final double oy = cos(seed * 0.6 + i * 0.9) * 24.0;

      final Offset pos = Offset(bx + ox, by + oy);

      // pulse scale
      final double pulse = 0.4 + 0.6 * (0.5 + 0.5 * sin(seed * 2 + i));

      // outer glow
      final Paint glow = Paint()
        ..color = const Color(0xFF64B5F6).withValues(alpha: 0.06 * pulse)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 18);
      canvas.drawCircle(pos, 22 * pulse, glow);

      // inner core
      final Paint core = Paint()
        ..color = const Color(0xFF90CAF9).withValues(alpha: 0.18 * pulse);
      canvas.drawCircle(pos, 6 * pulse, core);

      // tiny bright center
      final Paint dot = Paint()
        ..color = Colors.white.withValues(alpha: 0.9 * pulse);
      canvas.drawCircle(pos, 1.6 * pulse, dot);
    }
  }

  @override
  bool shouldRepaint(covariant _TechPainter oldDelegate) {
    return oldDelegate.progress != progress;
  }
}
