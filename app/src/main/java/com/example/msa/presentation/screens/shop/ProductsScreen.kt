package com.msa.android.presentation.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.msa.android.domain.model.Product
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * المنتجات — سبائك ومشغولات.
 *
 * **السعر اللي في الكارت جاي محسوب من السيرفر.** مش بنضرب سعر الجرام
 * × الوزن هنا: السيرفر بيقرا Firestore بنفسه وبيرجّع الرقم، وده نفس
 * الرقم اللي بيتسجّل في الطلب — فمستحيل العميل يشوف سعر ويطلب بسعر
 * تاني.
 */
@Composable
fun ProductsScreen(
    onBack: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenCart: () -> Unit,
    onNeedsLogin: () -> Unit,
    vm: ProductsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // الشاشة بترجع من السلة أو من التفاصيل — العدّاد لازم يبقى محدّث
    LaunchedEffect(Unit) { vm.refreshCartCount() }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            snackbar.showSnackbar(it)
            vm.clearToast()
        }
    }

    MSABackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

                MSATopBar(
                    title = stringResource(R.string.shop_title),
                    showBack = true,
                    onBack = onBack
                )

                when {
                    state.loading && state.products.isEmpty() -> Box(
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

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item(key = "filter") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                KindFilter(state.kind) { vm.setKind(it) }
                                StalePriceNote(state.prices.isStale)

                                // المتجر مقفول من اللوحة — المنتجات
                                // بتفضل معروضة بس الطلب متوقّف
                                if (!state.shop.isEnabled) {
                                    Text(
                                        text = stringResource(R.string.shop_disabled),
                                        color = Color(0xFFFF9E99),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (state.products.isEmpty()) {
                            item(key = "empty") {
                                Text(
                                    text = stringResource(R.string.shop_empty),
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                                )
                            }
                        }

                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                arabic = state.arabic,
                                canOrder = state.shop.isEnabled,
                                onClick = { onOpenProduct(product.id) },
                                onAdd = { vm.addToCart(product, onNeedsLogin) }
                            )
                        }
                    }
                }
            }

            // زرار السلة العايم — بيظهر بس لو فيها حاجة
            if (state.cartCount > 0) {
                CartFab(
                    count = state.cartCount,
                    onClick = onOpenCart,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 28.dp)
                )
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    arabic: Boolean,
    canOrder: Boolean,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    ShopCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = product.thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33FFFFFF))
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name(arabic),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                SpecLine(product)

                if (product.manufacturingPerGram > 0) {
                    Text(
                        text = stringResource(
                            R.string.shop_manufacturing_per_gram,
                            com.msa.android.presentation.common.NumberFormatter
                                .two(product.manufacturingPerGram)
                        ),
                        color = Color(0xFF9E9E9E),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(2.dp))
                PriceText(product)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StockLabel(product)

            // الزرار بيتقفل لو المخزون خلص أو المتجر مقفول أو السعر
            // لسه ما وصلش — إضافة من غير سعر معناها سلة بإجمالي صفر
            GoldButton(
                text = stringResource(R.string.shop_add_to_cart),
                big = false,
                enabled = canOrder && product.inStock && product.hasPrice,
                onClick = onAdd,
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

@Composable
private fun StockLabel(product: Product) {
    when {
        !product.inStock -> Text(
            text = stringResource(R.string.shop_out_of_stock),
            color = Color(0xFFFF9E99),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        // بنعرض الكمية بس لما تقلّ — «متاح ٢» بتستعجل القرار،
        // «متاح ٥٠٠» مالهاش أي معنى للعميل
        product.stock != null && product.stock <= 5 -> Text(
            text = stringResource(R.string.shop_stock_left, product.stock.toString()),
            color = GoldenBorder,
            fontSize = 12.sp
        )

        else -> Spacer(Modifier.width(1.dp))
    }
}

@Composable
private fun CartFab(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(GoldenBorder)
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = Color(0xFF3F2D2C),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.shop_view_cart, count.toString()),
            color = Color(0xFF3F2D2C),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
