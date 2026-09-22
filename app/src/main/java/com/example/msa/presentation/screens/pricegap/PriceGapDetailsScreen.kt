package com.msa.android.presentation.screens.pricegap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import java.util.Locale

/**
 * «تفاصيل الفجوة السعرية» — بتتفتح من صف الفجوة في الرئيسية.
 *
 * [metal] بييجي من الراوت وبيتحدّد من الوضع المختار في الرئيسية:
 * اللي مختار «فضة» بيدوس على رقم فضة فلازم يلاقي تفاصيل فضة. كل
 * اللافتات والعيارات والشرح بتتغيّر معاه.
 *
 * التخطيط نفسه ما اتغيّرش: مؤشّر + كارت الأسعار + كارت الفجوة + شرح.
 */
@Composable
fun PriceGapDetailsScreen(
    onBack: () -> Unit,
    metal: GapMetal = GapMetal.GOLD,
    vm: PriceGapDetailsViewModel = hiltViewModel()
) {
    val fullState by vm.state.collectAsStateWithLifecycle()
    val state = fullState.forMetal(metal)

    val silver = metal == GapMetal.SILVER

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            MSATopBar(
                title = stringResource(
                    if (silver) R.string.price_gap_silver else R.string.price_gap_gold
                ),
                showBack = true,
                onBack = onBack
            )

            // ── Gauge ──
            PriceGapGauge(
                value = state.gaugeValue,
                // فجوة الفضة بجنيهات قليلة مش مئات، فمقياس الدهب
                // كان هيخلّي الإبرة واقفة في النص مهما حصل
                scale = if (silver) 5.0 else 100.0,
                decimals = if (silver) 2 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Card 1: تفاصيل الأسعار ──
            DetailCard(title = stringResource(R.string.price_details_title)) {
                DetailRow(
                    label = stringResource(
                        if (silver) R.string.label_silver999_global else R.string.label_gold24_global
                    ),
                    value = String.format(Locale.US, "%.2f USD", state.worldPerGramUsd),
                    valueColor = Color.White
                )
                DividerThin()
                DetailRow(
                    label = stringResource(
                        if (silver) R.string.label_silver999_local else R.string.label_gold24_local
                    ),
                    // الفضة سعر جرامها بالعشرات، فالتقريب للجنيه الصحيح
                    // كان هيخفي فروق حقيقية — خانتين للفضة وصحيح للدهب
                    value = String.format(
                        Locale.US,
                        if (silver) "%.2f ${stringResource(R.string.egp)}"
                        else "%.0f ${stringResource(R.string.egp)}",
                        state.localHighEgp
                    ),
                    valueColor = Color.White
                )
                DividerThin()
                DetailRow(
                    label = stringResource(
                        if (silver) R.string.label_silver_in_dollar else R.string.label_gold_in_dollar
                    ),
                    value = String.format(Locale.US, "%.2f ${stringResource(R.string.egp)}", state.dollarSagha),
                    valueColor = Color.White
                )
                DividerThin()
                DetailRow(
                    label = stringResource(R.string.label_dollar_official),
                    value = String.format(Locale.US, "%.2f ${stringResource(R.string.egp)}", fullState.dollarBank),
                    valueColor = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Card 2: تفاصيل الفجوة السعرية ──
            DetailCard(title = stringResource(R.string.gap_details_title)) {
                val gapFormat =
                    if (silver) "%.2f ${stringResource(R.string.gap_currency_egp)}"
                    else "%.0f ${stringResource(R.string.gap_currency_egp)}"

                DetailRow(
                    label = stringResource(if (silver) R.string.gap_in_999 else R.string.gap_in_24),
                    value = String.format(Locale.US, gapFormat, state.gapHighAbs),
                    valueColor = if (state.gapHighIsNegative) Color(0xFFE53935) else Color(0xFF00C853)
                )
                DividerThin()
                DetailRow(
                    label = stringResource(if (silver) R.string.gap_in_925 else R.string.gap_in_21),
                    value = String.format(Locale.US, gapFormat, state.gapLowAbs),
                    valueColor = if (state.gapLowIsNegative) Color(0xFFE53935) else Color(0xFF00C853)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Card 3: explanation ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x801C1C1E))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(
                        if (silver) R.string.gap_explanation_title_silver
                        else R.string.gap_explanation_title
                    ),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (silver) R.string.gap_explanation_body_silver
                        else R.string.gap_explanation_body
                    ),
                    color = Color(0xFFCFCFCF),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x801C1C1E))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = GoldenBorder,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // value on END (left in RTL) — matches iOS screenshot
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        // label on START (right in RTL)
        Text(
            text = label,
            color = Color(0xFFE6E6E6),
            fontSize = 14.sp,
            modifier = Modifier.weight(1.4f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DividerThin() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(Color(0x33FFFFFF))
    )
}
