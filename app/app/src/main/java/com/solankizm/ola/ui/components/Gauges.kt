package com.solankizm.ola.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sweeping speedometer arc, MoveOS-style: a 270° track with a glowing
 * progress sweep in the ride-mode accent colour. Pure Canvas, no images.
 */
@Composable
fun SpeedArc(
    fraction: Float,            // 0..1 of top speed
    accent: Color,
    track: Color,
    modifier: Modifier = Modifier,
    size: Dp = 440.dp,
    stroke: Dp = 22.dp,
) {
    val sweep by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "sweep",
    )
    val glow by animateColorAsState(accent, tween(400), label = "glow")

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val startAngle = 135f
            val total = 270f
            val strokePx = stroke.toPx()
            val inset = strokePx / 2 + 4
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // background track
            drawArc(
                color = track,
                startAngle = startAngle,
                sweepAngle = total,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            // soft glow underlay
            drawArc(
                color = glow.copy(alpha = 0.18f),
                startAngle = startAngle,
                sweepAngle = total * sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx * 2.1f, cap = StrokeCap.Round),
            )
            // progress sweep, gradient along the accent
            drawArc(
                brush = Brush.sweepGradient(
                    0f to glow.copy(alpha = 0.6f),
                    0.5f to glow,
                    1f to glow,
                ),
                startAngle = startAngle,
                sweepAngle = total * sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * Vertical 10-segment LED battery gauge (like the v1.3 My Ola dashboard).
 * Fills bottom-up; low charge tints amber/red.
 */
@Composable
fun BatteryLeds(
    bars: Int,                 // 0..10
    accent: Color,
    track: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val gap = size.height * 0.02f
        val segH = (size.height - gap * 9) / 10f
        for (i in 0 until 10) {
            val filled = i < bars
            val fromBottom = 9 - i
            val y = fromBottom * (segH + gap)
            val color = when {
                !filled -> track
                bars <= 2 -> Color(0xFFFF3B4E)
                bars <= 4 -> Color(0xFFFF8A1F)
                else -> accent
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, y),
                size = Size(size.width, segH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(segH / 3, segH / 3),
            )
        }
    }
}
