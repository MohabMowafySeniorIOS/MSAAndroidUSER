package com.msa.android.presentation.screens.technicalanalysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * التحليل الفني / الإرشادات الفنية — comparison dashboard.
 *
 *   Layout (top → bottom):
 *     • Header
 *     • Period tabs (أمس / أسبوع / شهر / سنة)
 *     • Period date range card
 *     • 3 rows × 2 cards: gold21/24, jewelryUSD/ounceUSD, silver/bankUSD
 *     • Full-width price-gap card
 *     • 2 cards: قيمة التغيير / نسبة التغيير
 *     • 2 cards: أعلى سعر / أقل سعر
 *     • 2 buttons: حالة سوق الذهب / الدولار
 */
@Composable
fun TechnicalAnalysisScreen(
    onBack: () -> Unit,
    vm: TechnicalAnalysisViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            MSATopBar(
                title = stringResource(R.string.technical_analysis),
                showBack = true,
                onBack = onBack
            )

            // Period tabs (always visible — don't depend on loading state)
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                PeriodTabBar(selected = state.period, onSelect = vm::setPeriod)
            }

            when {
                state.loading || state.data == null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldenBorder)
                    }
                }
                else -> {
                    val d = state.data!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PeriodDateCard(
                            startDate = d.startDate,
                            endDate = d.endDate,
                            periodLabel = stringResource(R.string.period_label),
                            onCalendarTap = { /* TODO: future custom date picker */ }
                        )

                        // Row 1
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            InstrumentCard(
                                title = stringResource(R.string.gold_21_price),
                                snapshot = d.gold21,
                                unit = stringResource(R.string.egp),
                                decimals = 0,
                                modifier = Modifier.weight(1f)
                            )
                            InstrumentCard(
                                title = stringResource(R.string.gold_24_price),
                                snapshot = d.gold24,
                                unit = stringResource(R.string.egp),
                                decimals = 0,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            InstrumentCard(
                                title = stringResource(R.string.dollar_sagha),
                                snapshot = d.jewelryUSD,
                                unit = stringResource(R.string.egp),
                                decimals = 2,
                                modifier = Modifier.weight(1f)
                            )
                            InstrumentCard(
                                title = stringResource(R.string.ounce_world_price),
                                snapshot = d.goldUSD,
                                unit = stringResource(R.string.usd),
                                decimals = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            InstrumentCard(
                                title = stringResource(R.string.silver_price),
                                snapshot = d.silver,
                                unit = stringResource(R.string.egp),
                                decimals = 0,
                                modifier = Modifier.weight(1f)
                            )
                            InstrumentCard(
                                title = stringResource(R.string.bank_dollar),
                                snapshot = d.bankUSD,
                                unit = stringResource(R.string.egp),
                                decimals = 2,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        PriceGapCard(snapshot = d.priceGap)

                        // قيمة التغيير / نسبة التغيير
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val changeText = buildString {
                                if (d.gold24.change < 0) append("-")
                                append(formatNumber(d.gold24.change.absoluteValue, 0))
                                append(" ").append(stringResource(R.string.egp))
                            }
                            StatCard(
                                title = stringResource(R.string.change_value),
                                value = changeText,
                                valueColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            val pctText = run {
                                val sign = if (d.gold24.changePct >= 0) "+" else ""
                                "%s%.2f%%".format(Locale.US, sign, d.gold24.changePct)
                            }
                            StatCard(
                                title = stringResource(R.string.change_percent),
                                value = pctText,
                                valueColor = if (d.gold24.isPositive) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // أعلى سعر / أقل سعر
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                title = stringResource(R.string.highest_price),
                                value = "${formatNumber(d.highPrice, 0)} ${stringResource(R.string.egp)}",
                                valueColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = stringResource(R.string.lowest_price),
                                value = "${formatNumber(d.lowPrice, 0)} ${stringResource(R.string.egp)}",
                                valueColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // حالة سوق الذهب / الدولار
                        MarketStatusButton(
                            title = stringResource(R.string.gold_market_status),
                            isUp = d.goldMarketUp,
                            onClick = { /* informational */ }
                        )
                        MarketStatusButton(
                            title = stringResource(R.string.dollar_market_status),
                            isUp = d.dollarMarketUp,
                            onClick = { /* informational */ }
                        )

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
