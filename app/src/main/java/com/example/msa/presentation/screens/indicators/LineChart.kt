package com.msa.android.presentation.screens.indicators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.domain.model.ChartDataSet
import com.msa.android.domain.model.PricePoint
import com.msa.android.domain.model.TimePeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Cream-coloured line chart with grid + axis labels, modeled on the iOS
 * `GoldLineChart` view. Drag your finger across the chart to reveal a
 * tooltip with the value at that point.
 */
@Composable
fun LineChart(
    dataSet: ChartDataSet,
    modifier: Modifier = Modifier
) {
    val lineColor  = Color(0xFFE8D5A0)
    val gridColor  = Color.White.copy(alpha = 0.07f)
    val cardBg     = Color(0xFF222222)

    var touchX by remember { mutableStateOf<Float?>(null) }
    var touchIdx by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
    ) {
        // Title
        Text(
            text = dataSet.title,
            color = Color(0xFFE8D5A0),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        val pts = dataSet.points
        if (pts.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد بيانات", color = Color.Gray)
            }
            return
        }

        val minV = dataSet.minValue
        val maxV = dataSet.maxValue
        val range = (maxV - minV).let { if (it == 0.0) 1.0 else it }
        val period = guessPeriod(pts)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF1A1A1A))
                .pointerInput(dataSet) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val (_, _, chartW, leftPad) = chartGeometry(size.width.toFloat(), size.height.toFloat())
                            val t = ((offset.x - leftPad) / chartW).coerceIn(0f, 1f)
                            val idx = (t * (pts.size - 1)).roundToInt()
                            touchX = offset.x
                            touchIdx = idx
                        },
                        onDrag = { change, _ ->
                            val (_, _, chartW, leftPad) = chartGeometry(size.width.toFloat(), size.height.toFloat())
                            val t = ((change.position.x - leftPad) / chartW).coerceIn(0f, 1f)
                            val idx = (t * (pts.size - 1)).roundToInt()
                            touchX = change.position.x
                            touchIdx = idx
                        },
                        onDragEnd   = { touchX = null; touchIdx = null },
                        onDragCancel= { touchX = null; touchIdx = null }
                    )
                }
                .pointerInput(dataSet) {
                    // Long-press to "pin" — same affordance as iOS hint
                    detectTapGestures(
                        onLongPress = { offset ->
                            val (_, _, chartW, leftPad) = chartGeometry(size.width.toFloat(), size.height.toFloat())
                            val t = ((offset.x - leftPad) / chartW).coerceIn(0f, 1f)
                            val idx = (t * (pts.size - 1)).roundToInt()
                            touchX = offset.x
                            touchIdx = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val (topPad, botPad, chartW, leftPad) = chartGeometry(w, h)
                val chartH = h - topPad - botPad

                // ── Grid lines + Y labels ─────────────────────────
                val yLabels = makeYLabels(minV, maxV, count = 7)
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                yLabels.forEach { v ->
                    val yFrac = ((v - minV) / range).toFloat()
                    val y = topPad + chartH - (yFrac * chartH)

                    // grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, y),
                        end   = Offset(w - 8f, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    // label
                    drawContext.canvas.nativeCanvas.drawText(
                        formatLabel(v),
                        leftPad - 4f,
                        y + labelPaint.textSize / 3f,
                        labelPaint
                    )
                }

                // ── X labels ──────────────────────────────────────
                val xLabels = makeXLabels(pts, count = 7)
                val xLabelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#888888")
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                xLabels.forEach { (date, idx) ->
                    val xFrac = idx.toFloat() / (pts.size - 1).toFloat()
                    val x = leftPad + xFrac * chartW
                    drawContext.canvas.nativeCanvas.drawText(
                        formatDate(date, period),
                        x,
                        h - botPad / 2f + 4f,
                        xLabelPaint
                    )
                }

                // ── Main line ─────────────────────────────────────
                val linePath = Path().apply {
                    pts.forEachIndexed { i, p ->
                        val xFrac = i.toFloat() / (pts.size - 1).toFloat()
                        val yFrac = ((p.value - minV) / range).toFloat()
                        val x = leftPad + xFrac * chartW
                        val y = topPad + chartH - yFrac * chartH
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // ── Touch indicator ───────────────────────────────
                val idx = touchIdx
                if (idx != null && idx in pts.indices) {
                    val pt = pts[idx]
                    val xFrac = idx.toFloat() / (pts.size - 1).toFloat()
                    val yFrac = ((pt.value - minV) / range).toFloat()
                    val x = leftPad + xFrac * chartW
                    val y = topPad + chartH - yFrac * chartH

                    // dashed vertical
                    drawLine(
                        color = Color(0xFFC9A84C).copy(alpha = 0.6f),
                        start = Offset(x, topPad),
                        end = Offset(x, h - botPad),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f))
                    )
                    drawCircle(
                        color = Color(0xFFC9A84C),
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )

                    // value bubble (drawn via native canvas for background fill)
                    val bubbleText = formatValue(pt.value)
                    val bubblePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 11.sp.toPx()
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val bgPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#C9A84C")
                        isAntiAlias = true
                    }
                    val textW = bubblePaint.measureText(bubbleText) + 16.dp.toPx()
                    val textH = 22.dp.toPx()
                    val bubbleX = x.coerceIn(textW / 2 + 4.dp.toPx(), w - textW / 2 - 4.dp.toPx())
                    val bubbleY = (y - 28.dp.toPx()).coerceAtLeast(topPad + 4.dp.toPx())
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        bubbleX - textW / 2, bubbleY - textH / 2,
                        bubbleX + textW / 2, bubbleY + textH / 2,
                        6.dp.toPx(), 6.dp.toPx(), bgPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        bubbleText,
                        bubbleX,
                        bubbleY + bubblePaint.textSize / 3f,
                        bubblePaint
                    )
                }
            }
        }
    }
}

// ── helpers ────────────────────────────────────────────────────────────────

private data class ChartGeometry(val topPad: Float, val botPad: Float, val chartW: Float, val leftPad: Float)

private fun chartGeometry(w: Float, h: Float): ChartGeometry {
    val leftPad = 50f
    val rightPad = 8f
    val topPad   = 8f
    val botPad   = 28f
    return ChartGeometry(topPad, botPad, w - leftPad - rightPad, leftPad)
}

private fun makeYLabels(min: Double, max: Double, count: Int): List<Double> {
    val step = (max - min) / (count - 1)
    return (0 until count).map { min + step * it }
}

private fun makeXLabels(points: List<PricePoint>, count: Int): List<Pair<Date, Int>> {
    if (points.size < 2) return emptyList()
    val step = (points.size / count).coerceAtLeast(1)
    return (0 until points.size step step).map { points[it].date to it }
}

private fun guessPeriod(pts: List<PricePoint>): TimePeriod {
    if (pts.size < 2) return TimePeriod.H24
    val span = (pts.last().date.time - pts.first().date.time) / (1000.0 * 86400)
    return when {
        span < 2    -> TimePeriod.H24
        span < 8    -> TimePeriod.WEEK
        span < 32   -> TimePeriod.MONTH
        span < 92   -> TimePeriod.MONTHS3
        span < 182  -> TimePeriod.MONTHS6
        span < 270  -> TimePeriod.MONTHS9
        span < 400  -> TimePeriod.YEAR1
        span < 740  -> TimePeriod.YEARS2
        else        -> TimePeriod.YEARS3
    }
}

private fun formatDate(date: Date, period: TimePeriod): String {
    val pattern = when (period) {
        TimePeriod.H24                                                        -> "HH:mm"
        TimePeriod.WEEK, TimePeriod.MONTH                                     -> "d/M"
        TimePeriod.MONTHS3, TimePeriod.MONTHS6, TimePeriod.MONTHS9, TimePeriod.YEAR1 -> "MMM"
        TimePeriod.YEARS2, TimePeriod.YEARS3                                  -> "yyyy"
    }
    return SimpleDateFormat(pattern, Locale("ar")).format(date)
}

private fun formatLabel(v: Double): String = when {
    v >= 1000 -> String.format(Locale.US, "%.0f", v)
    v >= 100  -> String.format(Locale.US, "%.1f", v)
    else      -> String.format(Locale.US, "%.2f", v)
}

private fun formatValue(v: Double): String =
    if (v >= 1000) String.format(Locale.US, "%.0f", v) else String.format(Locale.US, "%.2f", v)
