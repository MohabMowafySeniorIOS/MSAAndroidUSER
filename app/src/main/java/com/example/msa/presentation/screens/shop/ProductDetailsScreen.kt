package com.msa.android.presentation.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.msa.android.R
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * تفاصيل المنتج.
 *
 * تفصيل الحساب معروض كامل (قيمة المعدن + المصنعية + الضريبة) عن قصد:
 * العميل اللي بيشتري دهب بيحسب بنفسه، والرقم المجمّع من غير تفصيل
 * بيخلي الناس تشك. الأرقام دي كلها من السيرفر.
 */
@Composable
fun ProductDetailsScreen(
    productId: Long,
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    onNeedsLogin: () -> Unit,
    vm: ProductDetailsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(productId) { vm.load(productId) }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            snackbar.showSnackbar(it)
            vm.clearToast()
        }
    }

    // الإضافة نجحت — بنودّيه للسلة على طول بدل ما يدوّر عليها
    LaunchedEffect(state.added) {
        if (state.added) {
            vm.clearAdded()
            onOpenCart()
        }
    }

    MSABackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                MSATopBar(
                    title = stringResource(R.string.shop_product_details),
                    showBack = true,
                    onBack = onBack
                )

                val product = state.product

                when {
                    state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldenBorder)
                    }

                    state.failed || product == null -> Box(
                        Modifier.fillMaxSize().padding(32.dp), Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.shop_load_failed),
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, bottom = 130.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.35f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33FFFFFF))
                        )

                        Text(
                            text = product.name(state.arabic),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        SpecLine(product, fontSize = 14)

                        product.description(state.arabic)?.let {
                            Text(
                                text = it,
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }

                        PriceBreakdown(product = product, quantity = state.quantity)

                        // الطرق المتاحة للمنتج ده — المشرف بيقدر يقفل
                        // أي واحدة فيهم من اللوحة
                        AvailableModes(
                            deposit = product.allowDeposit,
                            onArrival = product.allowOnArrival
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.shop_quantity),
                                color = Color.White,
                                fontSize = 15.sp
                            )

                            QuantityStepper(
                                quantity = state.quantity,
                                onChange = { vm.setQuantity(it) },
                                enabled = product.inStock
                            )
                        }
                    }
                }
            }

            state.product?.let { product ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xCC000000))
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    GoldButton(
                        text = if (product.inStock)
                            stringResource(R.string.shop_add_to_cart)
                        else stringResource(R.string.shop_out_of_stock),
                        enabled = product.inStock && product.hasPrice,
                        loading = state.adding,
                        onClick = { vm.addToCart(onNeedsLogin) }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
            )
        }
    }
}

/**
 * تفصيل الحساب.
 *
 * الأرقام هنا للقطعة الواحدة (زي ما السيرفر رجّعها) مضروبة في
 * الكمية محلياً **للعرض بس** — السيرفر بيعيد الحساب كامل وقت
 * الإضافة للسلة ووقت الطلب، فالرقم النهائي دايماً بتاعه.
 */
@Composable
private fun PriceBreakdown(
    product: com.msa.android.domain.model.Product,
    quantity: Int
) {
    val price = product.price

    ShopCard {
        if (price == null || !price.priced) {
            Text(
                text = stringResource(R.string.shop_price_loading),
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )

            return@ShopCard
        }

        SummaryRow(
            label = stringResource(R.string.shop_gram_price_label),
            value = NumberFormatter.two(price.gramPrice)
        )
        SummaryRow(
            label = stringResource(R.string.shop_metal_value),
            value = NumberFormatter.two(price.metalValue * quantity)
        )

        if (price.manufacturingTotal > 0) {
            SummaryRow(
                label = stringResource(R.string.shop_manufacturing),
                value = NumberFormatter.two(price.manufacturingTotal * quantity)
            )
        }

        if (price.taxAmount > 0) {
            SummaryRow(
                label = stringResource(R.string.shop_tax),
                value = NumberFormatter.two(price.taxAmount * quantity)
            )
        }

        CardDivider()

        SummaryRow(
            label = stringResource(R.string.shop_total),
            value = NumberFormatter.two(price.total * quantity),
            bold = true
        )
    }
}

@Composable
private fun AvailableModes(deposit: Boolean, onArrival: Boolean) {
    ShopCard {
        Text(
            text = stringResource(R.string.shop_order_methods),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (deposit) {
            ModeLine(
                title = stringResource(R.string.shop_mode_deposit),
                detail = stringResource(R.string.shop_mode_deposit_hint)
            )
        }

        if (onArrival) {
            ModeLine(
                title = stringResource(R.string.shop_mode_on_arrival),
                detail = stringResource(R.string.shop_mode_on_arrival_hint)
            )
        }
    }
}

@Composable
private fun ModeLine(title: String, detail: String) {
    Column {
        Text(text = "• $title", color = GoldenBorder, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(text = detail, color = Color(0xFF9E9E9E), fontSize = 12.sp, lineHeight = 18.sp)
    }
}
