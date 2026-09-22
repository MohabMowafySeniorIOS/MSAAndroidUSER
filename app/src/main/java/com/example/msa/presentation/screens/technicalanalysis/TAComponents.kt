package com.msa.android.presentation.screens.technicalanalysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.domain.model.PriceSnapshot
import com.msa.android.domain.model.TAPeriod
import com.msa.android.presentation.theme.GoldenBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── PeriodTabBar ─────────────────────────────────────────────────────────

/** 4-segment pill bar at the top of the TA screen. */
@Composable
fun PeriodTabBar(
    selected: TAPeriod,
    onSelect: (TAPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, GoldenBorder.copy(alpha = 0.4f), RoundedCornerShape(25.dp))
            .padding(4.dp)
    ) {
        TAPeriod.entries.forEach { period ->
            val isSel = selected == period
            val bg = if (isSel) Color(0xFFE8C870) else Color.Transparent
            val fg = if (isSel) Color.Black else Color(0xFFAAAAAA)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onSelect(period) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayAr,
                    color = fg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── PeriodDateCard ───────────────────────────────────────────────────────

@Composable
fun PeriodDateCard(
    startDate: Date,
    endDate: Date,
    periodLabel: String,
    onCalendarTap: () -> Unit
) {
    val df = remember1 { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x801C1C1E))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarToday,
            contentDescription = null,
            tint = GoldenBorder,
            modifier = Modifier
                .size(28.dp)
                .clickable { onCalendarTap() }
        )

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.End) {
            Text(periodLabel, color = GoldenBorder, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${df.format(endDate)} - ${df.format(startDate)}",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

/** Quick `remember` shorthand — gives us a stable formatter without an import dance. */
@Composable
private fun <T> remember1(producer: () -> T): T =
    androidx.compose.runtime.remember { producer() }

// ─── InstrumentCard ───────────────────────────────────────────────────────

@Composable
fun InstrumentCard(
    title: String,
    snapshot: PriceSnapshot,
    unit: String,
    decimals: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x801C1C1E))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = GoldenBorder,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        DividerThin()
        Spacer(Modifier.height(8.dp))

        OpenCloseRow(
            label = "افتتاح",
            valueText = "${formatNumber(snapshot.open, decimals)} $unit"
        )
        Spacer(Modifier.height(6.dp))
        OpenCloseRow(
            label = "إغلاق",
            valueText = "${formatNumber(snapshot.close, decimals)} $unit"
        )
        Spacer(Modifier.height(10.dp))

        ChangeBadge(
            pct = snapshot.changePct,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
    }
}

// ─── PriceGapCard ─────────────────────────────────────────────────────────

@Composable
fun PriceGapCard(snapshot: PriceSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x801C1C1E))
            .padding(12.dp)
    ) {
        Text(
            text = "فجوة السعر",
            color = GoldenBorder,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        DividerThin()
        Spacer(Modifier.height(8.dp))

        OpenCloseRow(label = "افتتاح", valueText = "${formatNumber(snapshot.open, 2)} جنيه")
        Spacer(Modifier.height(6.dp))
        OpenCloseRow(label = "إغلاق", valueText = "${formatNumber(snapshot.close, 2)} جنيه")
        Spacer(Modifier.height(10.dp))

        ChangeBadge(
            pct = snapshot.changePct,
            modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center)
        )
    }
}

// ─── StatCard (قيمة التغيير / نسبة التغيير / أعلى سعر / أقل سعر) ─────────

@Composable
fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x801C1C1E))
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, color = Color(0xFFAAAAAA), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── MarketStatusButton ───────────────────────────────────────────────────

@Composable
fun MarketStatusButton(
    title: String,
    isUp: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isUp) Color(0xFF1E7A3E) else Color(0xFF8B1A1A)
    val iconColor   = if (isUp) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val icon = if (isUp) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.5.dp, borderColor, RoundedCornerShape(25.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─── ChangeBadge ──────────────────────────────────────────────────────────

@Composable
fun ChangeBadge(pct: Double, modifier: Modifier = Modifier, fontSize: Int = 13) {
    val isPositive = pct >= 0
    val bg = if (isPositive) Color(0xFF2ECC71) else Color(0xFFE74C3C)

    val sign = if (isPositive) "+" else ""
    val text = "%s%.2f%%".format(Locale.US, sign, pct)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── tiny helpers ─────────────────────────────────────────────────────────

@Composable
private fun OpenCloseRow(label: String, valueText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // RTL-end (left of screen) = value text
        Text(
            text = valueText,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        // RTL-start (right of screen) = label
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DividerThin() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0xFF3A3A3A))
    )
}

internal fun formatNumber(v: Double, decimals: Int): String {
    val fmt = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return fmt.format(v)
}
