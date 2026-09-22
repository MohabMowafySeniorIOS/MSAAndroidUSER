package com.msa.android.presentation.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.msa.android.domain.model.Order
import com.msa.android.domain.model.PricingMode
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * طلباتي.
 *
 * كل كارت بيوضّح حاجتين لازم العميل يعرفهم قبل ما يروح الفرع:
 * **الكود** اللي هيقوله للموظف، و**هل الرقم نهائي ولا تقديري**.
 */
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onBrowseProducts: () -> Unit,
    highlightOrderId: Long? = null,
    vm: OrdersViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    MSABackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                MSATopBar(
                    title = stringResource(R.string.orders_title),
                    showBack = true,
                    onBack = onBack
                )

                when {
                    state.loading && state.orders.isEmpty() -> Box(
                        Modifier.fillMaxSize(), Alignment.Center
                    ) { CircularProgressIndicator(color = GoldenBorder) }

                    state.failed -> Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.shop_load_failed),
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        GoldButton(
                            text = stringResource(R.string.retry),
                            big = false,
                            onClick = { vm.load() }
                        )
                    }

                    state.orders.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.orders_empty),
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                        GoldButton(
                            text = stringResource(R.string.cart_browse_products),
                            big = false,
                            filled = false,
                            onClick = onBrowseProducts,
                            modifier = Modifier.width(220.dp)
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.orders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                arabic = state.arabic,
                                // الطلب اللي لسه اتعمل بيتفتح مفتوح
                                // عشان العميل يشوف الكود على طول
                                startExpanded = order.id == highlightOrderId,
                                onCancel = { vm.cancel(order.id) }
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp)
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    arabic: Boolean,
    startExpanded: Boolean,
    onCancel: () -> Unit
) {
    var expanded by remember(order.id) { mutableStateOf(startExpanded) }
    var confirmingCancel by remember(order.id) { mutableStateOf(false) }

    ShopCard(onClick = { expanded = !expanded }) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = order.code,
                    color = GoldenBorder,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                order.createdAt?.let {
                    Text(text = shortDate(it), color = Color(0xFF9E9E9E), fontSize = 11.sp)
                }
            }

            StatusBadge(order.statusLabel(arabic), order.statusValue)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = NumberFormatter.two(order.total),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(
                        if (order.isEstimate) R.string.orders_estimate_label
                        else R.string.orders_locked_label
                    ),
                    color = if (order.isEstimate) Color(0xFFE8B138) else Color(0xFF7BD48F),
                    fontSize = 11.sp
                )
            }

            order.branch?.let {
                Text(
                    text = it.name(arabic),
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
            }
        }

        // العربون أهم سطر في الكارت لما يكون مستنّي دفع
        if (order.deposit.exists) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.orders_deposit),
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
                Text(
                    text = "${NumberFormatter.two(order.deposit.amount)} · ${order.deposit.label(arabic)}",
                    color = if (order.deposit.isPaid) Color(0xFF7BD48F) else Color(0xFFE8B138),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (expanded) {
            CardDivider()

            order.items.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = line.name(arabic),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${trimGrams(line.weightGrams)}g · ${line.karat} · ×${line.quantity}",
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = NumberFormatter.two(line.lineTotal),
                        color = GoldenBorder,
                        fontSize = 13.sp
                    )
                }
            }

            /*
             * تثبيت السعر انتهى — الطلب بقى بيتسعّر وقت الاستلام.
             * ده تغيير جوهري في شروط الطلب فلازم يبان بوضوح.
             */
            if (order.pricingMode == PricingMode.DEPOSIT && order.priceLockExpired) {
                Text(
                    text = stringResource(R.string.orders_lock_expired),
                    color = Color(0xFFFF9E99),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            order.note?.let {
                Text(text = it, color = Color(0xFF9E9E9E), fontSize = 12.sp)
            }

            if (order.canCancel) {
                CardDivider()

                if (confirmingCancel) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoldButton(
                            text = stringResource(R.string.orders_cancel_confirm),
                            big = false,
                            onClick = {
                                confirmingCancel = false
                                onCancel()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GoldButton(
                            text = stringResource(R.string.orders_cancel_keep),
                            big = false,
                            filled = false,
                            onClick = { confirmingCancel = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.orders_cancel),
                        color = Color(0xFFFF9E99),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { confirmingCancel = true }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, value: String) {
    val color = when (value) {
        "completed" -> Color(0xFF7BD48F)
        "ready", "confirmed" -> GoldenBorder
        "cancelled", "expired" -> Color(0xFFFF9E99)
        else -> Color(0xFFCCCCCC)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * التاريخ من `2026-09-14T12:04:28.000000Z` لـ `2026-09-14`.
 *
 * بنقص عند الحرف `T` بدل ما نحلّل التاريخ كله: العرض هنا محتاج
 * اليوم بس، والتحليل الكامل بيجيب مشاكل المنطقة الزمنية من غير
 * فايدة. الصيغة ثابتة من لارافيل.
 */
private fun shortDate(iso: String): String = iso.substringBefore('T')
