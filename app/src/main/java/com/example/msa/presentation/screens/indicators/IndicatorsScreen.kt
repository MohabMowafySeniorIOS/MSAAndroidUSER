package com.msa.android.presentation.screens.indicators

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.domain.model.MetalType2
import com.msa.android.domain.model.TimePeriod
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * المؤشرات — line-chart dashboard. Metal segmented picker switches between
 * gold and silver, period dropdown picks the time range, and the body shows
 * one chart per metric (4 for gold, 3 for silver).
 */
@Composable
fun IndicatorsScreen(
    onBack: () -> Unit,
    vm: IndicatorsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var periodOpen by remember { mutableStateOf(false) }

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            MSATopBar(
                title = stringResource(R.string.indicators),
                showBack = true,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HintBanner()

                MetalSegmentedPicker(
                    selected = state.metal,
                    onSelect = vm::setMetal
                )

                PeriodDropdown(
                    selected = state.period,
                    isExpanded = periodOpen,
                    onToggle = { periodOpen = !periodOpen },
                    onSelect = {
                        vm.setPeriod(it)
                        periodOpen = false
                    }
                )

                if (state.loading) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldenBorder)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.loading_data),
                                color = Color(0xFF888888),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    state.charts.forEach { ds ->
                        LineChart(
                            dataSet = ds,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(60.dp))
                }
            }
        }
    }
}

// ── Hint banner ────────────────────────────────────────────────────────────

@Composable
private fun HintBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2A2A))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.chart_hint),
            color = Color(0xFFCCCCCC),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Metal segmented picker ─────────────────────────────────────────────────

@Composable
private fun MetalSegmentedPicker(
    selected: MetalType2,
    onSelect: (MetalType2) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, GoldenBorder.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
            .padding(4.dp)
    ) {
        MetalType2.entries.forEach { metal ->
            val selectedBg = if (selected == metal) Color(0xFFE8C870) else Color.Transparent
            val fg = if (selected == metal) Color.Black else Color(0xFFC9A84C)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(selectedBg)
                    .clickable { onSelect(metal) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = metal.displayAr,
                    color = fg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Period dropdown ────────────────────────────────────────────────────────

@Composable
private fun PeriodDropdown(
    selected: TimePeriod,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (TimePeriod) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chev"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, GoldenBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
    ) {
        // Trigger row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A))
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFFC9A84C),
                modifier = Modifier.rotate(rotation).size(18.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = selected.displayAr,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                TimePeriod.entries.forEach { period ->
                    val rowBg = if (period == selected) Color(0xFF3A3A3A) else Color(0xFF252525)
                    val rowFg = if (period == selected) Color(0xFFC9A84C) else Color.White
                    val rowWeight = if (period == selected) FontWeight.SemiBold else FontWeight.Normal

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .clickable { onSelect(period) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = period.displayAr,
                            color = rowFg,
                            fontSize = 15.sp,
                            fontWeight = rowWeight,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                    if (period != TimePeriod.entries.last()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color(0xFF3A3A3A))
                        )
                    }
                }
            }
        }
    }
}
