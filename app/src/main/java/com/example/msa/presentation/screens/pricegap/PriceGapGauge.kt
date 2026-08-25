package com.msa.android.presentation.screens.pricegap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.msa.android.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Semicircle gauge that mirrors iOS "الفجوة السعرية" details screen.
 *
 *   • Arc spans the top half (180° clockwise from 9 o'clock to 3 o'clock).
 *   • Color sweep: green (left, -100) → light-green → yellow (top, 0) → orange → red (right, +100).
 *     (Reversed on purpose for our Arabic/RTL-only audience — reads the mirror
 *     image of a standard LTR gauge.)
 *   • Gold needle rotates by [value] clamped to ±100.
 *   • Scale ticks: -60, -80, -100 on the left  /  60, 80, 100 on the right.
 *   • The signed numeric value is rendered in red just below the needle pivot.
 */
@Composable
fun PriceGapGauge(
    value: Double,
    modifier: Modifier = Modifier
) {
    val clamped = value.coerceIn(-100.0, 100.0)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(320.dp, 200.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.92f                      // center near the bottom so the arc fills upward
            val radius = (h * 0.85f).coerceAtMost(w * 0.46f)
            val strokeWidth = 28.dp.toPx()

            // ---------- arc ----------
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(cx - radius, cy - radius)

            // Sweep gradient — the bottom half is hidden by `useCenter = false` + 180° sweep,
            // so the visible colours live in the 0.5 → 1.0 range of the brush.
            val sweepBrush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFFE53935),    // bottom half — never visible
                    0.50f to Color(0xFF00C853),    // 9 o'clock = -100 = green
                    0.62f to Color(0xFF8BC34A),    // upper-left = light green
                    0.75f to Color(0xFFFFC107),    // 12 o'clock = 0 = yellow
                    0.88f to Color(0xFFFF6F00),    // upper-right = orange
                    1.00f to Color(0xFFE53935)     // 3 o'clock = +100 = red
                ),
                center = Offset(cx, cy)
            )

            drawArc(
                brush = sweepBrush,
                startAngle = 180f,         // 9 o'clock
                sweepAngle = 180f,         // CW through 12 o'clock to 3 o'clock
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // ---------- scale ticks & labels ----------
            // Tick marks at ±60, ±80, ±100
            val tickValues = intArrayOf(60, 80, 100, -60, -80, -100)
            val tickLabelPx = 11.sp.toPx()
            tickValues.forEach { v ->
                val angleDeg = 270.0 + (v / 100.0) * 90.0      // map value → Canvas angle (reversed for RTL)
                val angleRad = Math.toRadians(angleDeg)
                val rOuter = radius + strokeWidth / 2f + 4.dp.toPx()
                val rInner = radius + strokeWidth / 2f - 6.dp.toPx()
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                // Tick mark
                drawLine(
                    color = Color(0xCCFFFFFF),
                    start = Offset(cx + rInner * cosA, cy + rInner * sinA),
                    end   = Offset(cx + rOuter * cosA, cy + rOuter * sinA),
                    strokeWidth = 2.dp.toPx()
                )

                // Label, slightly outside the tick
                val labelRadius = rOuter + 16.dp.toPx()
                val lx = cx + labelRadius * cosA
                val ly = cy + labelRadius * sinA
                val text = v.toString()
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = tickLabelPx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    text, lx, ly + tickLabelPx / 3f, paint
                )
            }

            // ---------- needle ----------
            val needleAngleDeg = 270.0 + (clamped / 100.0) * 90.0     // reversed for RTL
            val needleAngleRad = Math.toRadians(needleAngleDeg)
            val needleLen = radius - strokeWidth / 2f - 6.dp.toPx()
            val nex = cx + needleLen * cos(needleAngleRad).toFloat()
            val ney = cy + needleLen * sin(needleAngleRad).toFloat()
            drawLine(
                color = Color(0xFFD4AF37),
                start = Offset(cx, cy),
                end = Offset(nex, ney),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Tip ball at end of needle
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = 7.dp.toPx(),
                center = Offset(nex, ney)
            )

            // Center pivot
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = 8.dp.toPx(),
                center = Offset(cx, cy)
            )
        }

        // Big numeric value, red, sitting in the gauge bowl — rounded to the
        // nearest whole number (matches iOS) with the EGP label beneath it.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = String.format(Locale.US, "%.0f", value),
                color = Color(0xFFE53935),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.gap_currency_egp),
                color = Color(0xFFE53935),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
