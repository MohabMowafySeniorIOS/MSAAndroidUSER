package com.msa.android.presentation.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.msa.android.R
import com.msa.android.domain.model.Branch
import com.msa.android.domain.model.CartLine
import com.msa.android.domain.model.PricingMode
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * السلة وإتمام الطلب في شاشة واحدة.
 *
 * فصلهم لشاشتين كان هيضيف خطوة من غير فايدة: الطلب هنا مالوش دفع
 * أونلاين ولا عنوان شحن — اختيار الفرع والطريقة وخلاص.
 *
 * **الإجماليات كلها من السيرفر.** السلة نفسها في السيرفر مش في
 * التطبيق، عشان الرقم اللي العميل شايفه يبقى هو الرقم اللي هيتسجّل.
 */
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    onBrowseProducts: () -> Unit,
    vm: CartViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(state.placedOrder) {
        state.placedOrder?.let {
            val id = it.id
            vm.clearPlacedOrder()
            onOrderPlaced(id)
        }
    }

    MSABackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                MSATopBar(
                    title = stringResource(R.string.cart_title),
                    showBack = true,
                    onBack = onBack
                )

                when {
                    state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldenBorder)
                    }

                    state.cart.isEmpty -> EmptyCart(onBrowseProducts)

                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, bottom = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StalePriceNote(state.cart.prices.isStale)

                        state.cart.items.forEach { line ->
                            CartLineCard(
                                line = line,
                                arabic = state.arabic,
                                onQuantity = { vm.setQuantity(line.product.id, it) },
                                onRemove = { vm.remove(line.product.id) }
                            )
                        }

                        Totals(state)

                        ModePicker(
                            mode = state.mode,
                            depositAllowed = state.depositAllowed,
                            onArrivalAllowed = state.onArrivalAllowed,
                            depositAmount = state.cart.totals.depositAmount,
                            depositPercent = state.shop.depositPercent,
                            lockHours = state.shop.lockHours,
                            onSelect = { vm.setMode(it) }
                        )

                        // تعليمات دفع العربون بتتكتب من اللوحة، فبتتغيّر
                        // من غير تحديث للتطبيق
                        if (state.mode == PricingMode.DEPOSIT) {
                            state.shop.depositInstructions(state.arabic)?.let {
                                ShopCard {
                                    Text(
                                        text = it,
                                        color = Color(0xFFCCCCCC),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        BranchPicker(
                            branches = state.branches,
                            selectedId = state.selectedBranchId,
                            arabic = state.arabic,
                            onSelect = { vm.setBranch(it) }
                        )

                        NoteField(value = state.note, onChange = { vm.setNote(it) })

                        state.shop.terms(state.arabic)?.let {
                            Text(
                                text = it,
                                color = Color(0xFF9E9E9E),
                                fontSize = 11.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            if (!state.cart.isEmpty) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xCC000000))
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    GoldButton(
                        text = stringResource(
                            if (state.mode == PricingMode.DEPOSIT) R.string.cart_place_with_deposit
                            else R.string.cart_place_on_arrival
                        ),
                        loading = state.placing,
                        // الطلب من غير سعر معناه إجمالي صفر، والسيرفر
                        // هيرفضه برضه — فالأحسن نمنعه من هنا
                        enabled = state.cart.totals.priced
                            && state.shop.isEnabled
                            && state.selectedBranchId != null,
                        onClick = { vm.placeOrder() }
                    )

                    if (state.selectedBranchId == null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.cart_pick_branch_first),
                            color = Color(0xFFFF9E99),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp)
            )
        }
    }
}

@Composable
private fun EmptyCart(onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.cart_empty),
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        GoldButton(
            text = stringResource(R.string.cart_browse_products),
            big = false,
            filled = false,
            onClick = onBrowse,
            modifier = Modifier.width(220.dp)
        )
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    arabic: Boolean,
    onQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    ShopCard {
        Row(verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = line.product.thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33FFFFFF))
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = line.product.name(arabic),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                SpecLine(line.product, fontSize = 11)
                Text(
                    text = NumberFormatter.two(line.lineTotal),
                    color = GoldenBorder,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.cart_remove),
                color = Color(0xFFFF9E99),
                fontSize = 12.sp,
                modifier = Modifier.clickable { onRemove() }.padding(4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            QuantityStepper(quantity = line.quantity, onChange = onQuantity)
        }
    }
}

@Composable
private fun Totals(state: CartState) {
    ShopCard {
        SummaryRow(
            stringResource(R.string.shop_metal_value),
            NumberFormatter.two(state.cart.totals.metalTotal)
        )

        if (state.cart.totals.manufacturingTotal > 0) {
            SummaryRow(
                stringResource(R.string.shop_manufacturing),
                NumberFormatter.two(state.cart.totals.manufacturingTotal)
            )
        }

        if (state.cart.totals.taxTotal > 0) {
            SummaryRow(
                stringResource(R.string.shop_tax),
                NumberFormatter.two(state.cart.totals.taxTotal)
            )
        }

        CardDivider()

        SummaryRow(
            stringResource(R.string.shop_total),
            NumberFormatter.two(state.cart.totals.total),
            bold = true
        )

        /*
         * في «التسعير عند الاستلام» الرقم ده تقديري — السعر بيتحسب
         * تاني وقت ما العميل يوصل الفرع. لازم يعرف ده قبل ما يطلب،
         * مش لما يتفاجئ في الفرع.
         */
        if (state.mode == PricingMode.ON_ARRIVAL) {
            Text(
                text = stringResource(R.string.cart_estimate_note),
                color = Color(0xFFE8B138),
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ModePicker(
    mode: PricingMode,
    depositAllowed: Boolean,
    onArrivalAllowed: Boolean,
    depositAmount: Double,
    depositPercent: Double,
    lockHours: Int,
    onSelect: (PricingMode) -> Unit
) {
    ShopCard {
        Text(
            text = stringResource(R.string.cart_how_to_order),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        if (depositAllowed) {
            ModeOption(
                selected = mode == PricingMode.DEPOSIT,
                title = stringResource(R.string.shop_mode_deposit),
                detail = stringResource(
                    R.string.cart_deposit_detail,
                    NumberFormatter.two(depositAmount),
                    NumberFormatter.int(depositPercent),
                    lockHours.toString()
                ),
                onClick = { onSelect(PricingMode.DEPOSIT) }
            )
        }

        if (onArrivalAllowed) {
            ModeOption(
                selected = mode == PricingMode.ON_ARRIVAL,
                title = stringResource(R.string.shop_mode_on_arrival),
                detail = stringResource(R.string.shop_mode_on_arrival_hint),
                onClick = { onSelect(PricingMode.ON_ARRIVAL) }
            )
        }
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) GoldenBorder.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                1.dp,
                GoldenBorder.copy(alpha = if (selected) 0.7f else 0.25f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioDot(selected)

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = if (selected) GoldenBorder else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Text(text = detail, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .border(1.5.dp, GoldenBorder.copy(alpha = if (selected) 1f else 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(GoldenBorder))
        }
    }
}

/**
 * اختيار فرع الاستلام.
 *
 * القايمة جاية مرتّبة بالأقرب من السيرفر لو الموقع متاح، وأول فرع
 * بيبقى مختار تلقائياً — ده المتوقّع في أغلب الحالات.
 */
@Composable
private fun BranchPicker(
    branches: List<Branch>,
    selectedId: Long?,
    arabic: Boolean,
    onSelect: (Long) -> Unit
) {
    ShopCard {
        Text(
            text = stringResource(R.string.cart_pickup_branch),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        if (branches.isEmpty()) {
            Text(
                text = stringResource(R.string.branches_empty),
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp
            )

            return@ShopCard
        }

        branches.forEach { branch ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(branch.id) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioDot(branch.id == selectedId)

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = branch.name(arabic),
                        color = if (branch.id == selectedId) GoldenBorder else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    branch.address(arabic)?.let {
                        Text(text = it, color = Color(0xFF9E9E9E), fontSize = 11.sp)
                    }
                }

                branch.distanceKm?.let {
                    Text(
                        text = stringResource(
                            R.string.branch_distance_km,
                            NumberFormatter.two(it)
                        ),
                        color = GoldenBorder.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteField(value: String, onChange: (String) -> Unit) {
    ShopCard {
        Text(
            text = stringResource(R.string.cart_note),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33000000))
                .border(1.dp, GoldenBorder.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= 1000) onChange(it) },
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                cursorBrush = SolidColor(GoldenBorder),
                modifier = Modifier.fillMaxSize()
            )

            if (value.isEmpty()) {
                Text(
                    text = stringResource(R.string.cart_note_hint),
                    color = Color(0xFF777777),
                    fontSize = 13.sp
                )
            }
        }
    }
}
