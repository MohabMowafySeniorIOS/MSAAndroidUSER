package com.msa.android.presentation.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.Text
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
import com.msa.android.domain.model.ItemType
import com.msa.android.domain.model.PortfolioEntry
import com.msa.android.domain.model.PortfolioMetal
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.MSADisplayFamily
import java.text.NumberFormat
import java.util.Locale

private val green = Color(0xFF18A957)
private val red = Color(0xFFE0523F)
private val muted = Color(0xFFB0AAAA)

/** أرقام بفاصلة آلاف — بأرقام إنجليزية عشان الجداول تفضل متساوية */
internal fun money(value: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 2
    }.format(value)

internal fun grams(value: Double): String =
    NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
    }.format(value)

@Composable
fun WalletScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onLogin: () -> Unit,
    vm: WalletViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var zakatFor by remember { mutableStateOf<PortfolioMetal?>(null) }

    // الشاشتين بياخدوا نسخ منفصلة من الـ ViewModel، فلما ترجع من
    // شاشة الإضافة المحفظة مش عارفة إن حاجة اتضافت. بنعيد التحميل
    // مع كل رجوع للشاشة.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(title = stringResource(R.string.wallet_title), onBack = onBack)

            when {
                // من غير تسجيل دخول السيرفر بيرفض الطلب، فبنوضّح
                // السبب بدل ما نعرض رسالة خطأ مبهمة
                !state.isLoggedIn -> SignInPrompt(onLogin)

                state.isEmpty -> EmptyWallet(onAdd)

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { SummaryCard(state) }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetalCard(state, PortfolioMetal.GOLD, Modifier.weight(1f))
                            MetalCard(state, PortfolioMetal.SILVER, Modifier.weight(1f))
                        }
                    }

                    item {
                        ZakatShortcut(
                            onGold = { zakatFor = PortfolioMetal.GOLD },
                            onSilver = { zakatFor = PortfolioMetal.SILVER },
                            hasSilver = (state.totals.forMetal(PortfolioMetal.SILVER)?.itemsCount ?: 0) > 0
                        )
                    }

                    item {
                        Text(
                            text = stringResource(R.string.wallet_items, state.totals.itemsCount),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(state.items, key = { it.id }) { entry ->
                        ItemRow(entry) { onEdit(entry.id) }
                    }

                    item {
                        GoldButton(
                            text = stringResource(R.string.wallet_add),
                            onClick = onAdd,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

/** تنبيه فيه حسبة الزكاة جاهزة من قطع المحفظة */
@Composable
private fun ZakatDialog(result: WalletZakat, onDismiss: () -> Unit) {
    val metalName = stringResource(
        if (result.metal == PortfolioMetal.GOLD) R.string.gold else R.string.silver
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF261B1A),
        title = {
            Text(
                text = stringResource(R.string.wallet_zakat_of, metalName),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ZakatRow(stringResource(R.string.wallet_zakat_pure),
                         "${grams(result.pureGrams)} ${stringResource(R.string.gram)}")
                ZakatRow(stringResource(R.string.wallet_zakat_nisab),
                         "${grams(result.nisab)} ${stringResource(R.string.gram)}")

                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(1.dp)
                    .background(GoldenBorder.copy(alpha = 0.25f)))
                Spacer(Modifier.height(10.dp))

                if (result.isDue) {
                    ZakatRow(stringResource(R.string.wallet_zakat_grams),
                             "${grams(result.zakatGrams)} ${stringResource(R.string.gram)}")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${money(result.zakatValue)} ${stringResource(R.string.egp)}",
                        color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.wallet_zakat_due),
                         color = green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(stringResource(R.string.wallet_zakat_not_due),
                         color = muted, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.wallet_zakat_remaining,
                            grams(result.nisab - result.pureGrams)
                        ),
                        color = muted, fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = Gold, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ZakatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = muted, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SignInPrompt(onLogin: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.wallet_login_title),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MSADisplayFamily,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.wallet_login_hint),
            color = muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        GoldButton(text = stringResource(R.string.auth_login_action), onClick = onLogin)
    }
}

@Composable
private fun EmptyWallet(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.wallet_empty_title),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MSADisplayFamily,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.wallet_empty_hint),
            color = muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        GoldButton(text = stringResource(R.string.wallet_add), onClick = onAdd)
    }
}

/** الكارت الرئيسي — القيمة الحالية والأرقام المجمّعة */
@Composable
private fun SummaryCard(state: WalletState) {
    val totals = state.totals

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.wallet_current_value),
            color = muted,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = money(totals.currentValue),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MSADisplayFamily
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.egp),
                color = muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Spacer(Modifier.weight(1f))
            ProfitBadge(totals.profit, totals.profitPercent)
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(14.dp))

        Row {
            Stat(stringResource(R.string.wallet_total_paid), money(totals.totalPaid), Modifier.weight(1f))
            Stat(stringResource(R.string.wallet_total_weight),
                 "${grams(totals.totalWeight)} ${stringResource(R.string.gram)}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Stat(stringResource(R.string.wallet_shop_price), money(totals.shopSellValue), Modifier.weight(1f))
            Stat(stringResource(R.string.wallet_cashback), money(totals.cashbackTotal), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfitBadge(profit: Double, percent: Double) {
    val positive = profit >= 0
    val color = if (positive) green else red

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${if (positive) "+" else ""}${grams(percent)}%",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${if (positive) "+" else ""}${money(profit)}",
            color = color,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = muted, fontSize = 12.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoldenBorder.copy(alpha = 0.2f))
    )
}

/** كارت لكل معدن — ذهب وفضة جنب بعض */
@Composable
private fun MetalCard(state: WalletState, metal: PortfolioMetal, modifier: Modifier = Modifier) {
    val data = state.totals.forMetal(metal)
    val isGold = metal == PortfolioMetal.GOLD

    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isGold) Gold else Color(0xFFC6C6C6))
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(if (isGold) R.string.gold else R.string.silver),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        if (data == null || data.itemsCount == 0) {
            Text("—", color = muted, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.wallet_no_items), color = muted, fontSize = 12.sp)
        } else {
            Text(money(data.currentValue), color = Color.White,
                 fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${grams(data.totalWeight)} ${stringResource(R.string.gram)} · " +
                       stringResource(R.string.wallet_pieces, data.itemsCount),
                color = muted, fontSize = 12.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${if (data.profit >= 0) "+" else ""}${grams(data.profitPercent)}%",
                color = if (data.profit >= 0) green else red,
                fontSize = 13.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wallet_avg_buy, money(data.avgBuyPrice)),
                color = muted, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ZakatShortcut(onGold: () -> Unit, onSilver: () -> Unit, hasSilver: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Gold.copy(alpha = 0.12f))
            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.wallet_zakat_title),
             color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(stringResource(R.string.wallet_zakat_hint), color = muted, fontSize = 12.sp)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldButton(
                text = stringResource(R.string.gold),
                filled = false, big = false,
                onClick = onGold,
                modifier = Modifier.weight(1f)
            )
            // زرار الفضة بيظهر بس لو عنده فضة فعلاً
            if (hasSilver) {
                GoldButton(
                    text = stringResource(R.string.silver),
                    filled = false, big = false,
                    onClick = onSilver,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** صف القطعة في القايمة */
@Composable
private fun ItemRow(entry: PortfolioEntry, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(entry.type.labelRes()),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.wallet_karat_chip, entry.karat),
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Gold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "${grams(entry.weight)} ${stringResource(R.string.gram)} · " +
                       stringResource(R.string.wallet_bought_at, money(entry.gramPrice)),
                color = muted,
                fontSize = 12.sp
            )
            entry.note?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = muted.copy(alpha = 0.75f), fontSize = 11.sp, maxLines = 1)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(money(entry.currentValue), color = Color.White,
                 fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${if (entry.isProfit) "+" else ""}${grams(entry.profitPercent)}%",
                color = if (entry.isProfit) green else red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

internal fun ItemType.labelRes(): Int = when (this) {
    ItemType.BULLION -> R.string.wallet_type_bullion
    ItemType.COIN -> R.string.wallet_type_coin
    ItemType.JEWELLERY -> R.string.wallet_type_jewellery
    ItemType.SCRAP -> R.string.wallet_type_scrap
}
