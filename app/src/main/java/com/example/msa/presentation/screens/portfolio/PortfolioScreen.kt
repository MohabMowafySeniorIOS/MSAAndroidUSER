package com.msa.android.presentation.screens.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.msa.android.domain.model.PortfolioItemWithValue
import com.msa.android.domain.model.PortfolioSummary
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PROFIT_GREEN = Color(0xFF2ECC71)
private val LOSS_RED     = Color(0xFFE74C3C)

@Composable
fun PortfolioScreen(
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    vm: PortfolioViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    MSABackground {
        Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                MSATopBar(
                    title = stringResource(R.string.portfolio_title),
                    showBack = true,
                    onBack = onBack
                )

                when {
                    state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldenBorder)
                    }
                    state.items.isEmpty() -> EmptyState(onAddItem)
                    else -> Column(modifier = Modifier.fillMaxSize()) {
                        SummaryCard(state.summary, state.pricesReady)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.items, key = { it.item.id }) { item ->
                                PortfolioItemCard(item) { pendingDelete = item.item.id }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddItem,
                containerColor = GoldenBorder,
                contentColor = Color.Black,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.portfolio_add))
            }
        }

        pendingDelete?.let { id ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text(stringResource(R.string.portfolio_delete_title), color = Color.White) },
                text = { Text(stringResource(R.string.portfolio_delete_message), color = Color(0xFFCCCCCC)) },
                confirmButton = {
                    TextButton(onClick = {
                        vm.removeItem(id); pendingDelete = null
                    }) { Text(stringResource(R.string.portfolio_delete_confirm), color = LOSS_RED) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) {
                        Text(stringResource(R.string.portfolio_cancel), color = Color(0xFFCCCCCC))
                    }
                },
                containerColor = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(onAddItem: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0x33D4AF37)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.TrendingUp, null, tint = GoldenBorder, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.portfolio_empty_title),
            color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.portfolio_empty_message),
            color = Color(0xFFBBBBBB), fontSize = 14.sp,
            textAlign = TextAlign.Center, lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddItem,
            colors = ButtonDefaults.buttonColors(containerColor = GoldenBorder, contentColor = Color.Black),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.portfolio_add), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryCard(summary: PortfolioSummary, pricesReady: Boolean) {
    val profitColor = if (summary.isProfit) PROFIT_GREEN else LOSS_RED
    val profitIcon  = if (summary.isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    val sign        = if (summary.totalProfit >= 0) "+" else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC1C1C1E))
            .border(1.dp, GoldenBorder.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.portfolio_current_value),
             color = Color(0xFF999999), fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(formatEgp(summary.totalCurrentValue),
             color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        AnimatedVisibility(visible = pricesReady) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.height(6.dp))
                Icon(profitIcon, null, tint = profitColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$sign${formatEgp(summary.totalProfit)}  " +
                           "($sign${"%.2f".format(Locale.US, summary.totalProfitPercent)}%)",
                    color = profitColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF3A3A3A)))
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Stat(stringResource(R.string.portfolio_invested), formatEgp(summary.totalInvested),
                 Modifier.weight(1f))
            Stat(stringResource(R.string.portfolio_items), summary.itemCount.toString(),
                 Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF888888), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PortfolioItemCard(item: PortfolioItemWithValue, onDelete: () -> Unit) {
    val profitColor = if (item.isProfit) PROFIT_GREEN else LOSS_RED
    val sign        = if (item.profit >= 0) "+" else ""
    val dateText    = remember(item.item.purchaseDate) {
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            .format(Date(item.item.purchaseDate))
    }
    val qtyText = formatQuantity(item.item.quantity, item.item.type.measuredByWeight)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xCC1C1C1E))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.item.type.displayAr,
                     color = GoldenBorder, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("${stringResource(R.string.portfolio_quantity)}: $qtyText",
                     color = Color(0xFFCCCCCC), fontSize = 12.sp)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF3A3A3A)))
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Metric(stringResource(R.string.portfolio_invested),
                   formatEgp(item.item.purchaseTotalPrice), Modifier.weight(1f))
            Metric(stringResource(R.string.portfolio_current_value),
                   if (item.pricesAvailable) formatEgp(item.currentValue) else "—",
                   Modifier.weight(1f))
        }

        if (item.pricesAvailable) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(profitColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$sign${formatEgp(item.profit)}  " +
                               "($sign${"%.2f".format(Locale.US, item.profitPercent)}%)",
                        color = profitColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(dateText, color = Color(0xFF888888), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFF888888), fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatEgp(value: Double): String {
    val v = if (value < 0) -value else value
    val sign = if (value < 0) "-" else ""
    val formatted = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(v)
    return "$sign$formatted ج.م"
}

private fun formatQuantity(q: Double, byWeight: Boolean): String {
    val nf = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0; maximumFractionDigits = 3
    }
    return if (byWeight) "${nf.format(q)} جم" else nf.format(q)
}
