package com.msa.android.presentation.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.domain.model.Product
import com.msa.android.domain.model.ProductKind
import com.msa.android.presentation.common.NumberFormatter
import com.msa.android.presentation.theme.GoldenBorder

/**
 * عناصر مشتركة بين شاشات المتجر.
 */

/** خلفية الكارت الموحّدة — نفس كروت الفروع بالظبط */
@Composable
fun ShopCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/**
 * السعر — أو «جاري التحميل» لو لسه ما وصلش.
 *
 * ده مش تفصيلة شكلية: لو عرضنا صفر جنيه لما Firestore يتأخّر، العميل
 * هيفتكر إن المنتج ببلاش. الرئيسية بتعمل نفس الحاجة بالظبط.
 */
@Composable
fun PriceText(
    product: Product,
    big: Boolean = false
) {
    if (!product.hasPrice) {
        Text(
            text = stringResource(R.string.shop_price_loading),
            color = Color(0xFF9E9E9E),
            fontSize = if (big) 15.sp else 13.sp
        )

        return
    }

    val price = product.price!!

    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = NumberFormatter.two(price.total),
                color = GoldenBorder,
                fontSize = if (big) 24.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.currency_egp),
                color = GoldenBorder.copy(alpha = 0.8f),
                fontSize = if (big) 14.sp else 11.sp
            )
        }

        // سعر الجرام تحت الإجمالي — العميل يقدر يراجع الحساب بنفسه
        Text(
            text = stringResource(
                R.string.shop_gram_price,
                NumberFormatter.two(price.gramPrice)
            ),
            color = Color(0xFF9E9E9E),
            fontSize = if (big) 12.sp else 11.sp
        )
    }
}

/** الوزن والعيار — "١٠ جرام · عيار ٢٤" */
@Composable
fun SpecLine(product: Product, color: Color = Color(0xFFCCCCCC), fontSize: Int = 12) {
    Text(
        text = stringResource(
            R.string.shop_spec_line,
            trimGrams(product.weightGrams),
            product.karat.toString(),
            stringResource(if (product.isGold) R.string.metal_gold else R.string.metal_silver)
        ),
        color = color,
        fontSize = fontSize.sp
    )
}

/**
 * الوزن من غير أصفار زايدة: ٢٥٫٥ مش ٢٥٫٥٠٠، و١٠ مش ١٠٫٠٠٠.
 *
 * الوزن بيتخزّن بـ ٣ خانات عشرية عشان القطع الصغيرة، بس عرض
 * "١٠٫٠٠٠ جرام" لسبيكة عشرة جرام بيبان غريب.
 */
fun trimGrams(value: Double): String =
    NumberFormatter.three(value).trimEnd('0').trimEnd('.')

/** فلتر النوع — الكل / سبائك / مشغولات */
@Composable
fun KindFilter(
    selected: ProductKind?,
    onSelect: (ProductKind?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(stringResource(R.string.shop_kind_all), selected == null) { onSelect(null) }
        FilterChip(
            stringResource(R.string.shop_kind_bullion),
            selected == ProductKind.BULLION
        ) { onSelect(ProductKind.BULLION) }
        FilterChip(
            stringResource(R.string.shop_kind_jewellery),
            selected == ProductKind.JEWELLERY
        ) { onSelect(ProductKind.JEWELLERY) }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) GoldenBorder.copy(alpha = 0.18f) else Color(0x33000000))
            .border(
                1.dp,
                GoldenBorder.copy(alpha = if (active) 0.7f else 0.3f),
                CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (active) GoldenBorder else Color(0xFFCCCCCC),
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * تنبيه إن السعر من نسخة محفوظة.
 *
 * السيرفر بيرجّع `is_stale` لما Firestore ما يردّش وهو بيستخدم آخر
 * سعر معروف. المستخدم لازم يعرف — وبرضه الطلب بعربون هيترفض من
 * السيرفر لو السعر قديم فعلاً، فالتنبيه ده بيوضّح السبب مقدّماً.
 */
@Composable
fun StalePriceNote(visible: Boolean) {
    if (!visible) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x33E53935))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.shop_price_stale),
            color = Color(0xFFFF9E99),
            fontSize = 12.sp
        )
    }
}

/** صف في ملخّص الحساب */
@Composable
fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = if (bold) GoldenBorder else Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (bold) Color.White else Color(0xFFCCCCCC),
            fontSize = if (bold) 15.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = if (bold) 17.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/** فاصل خفيف بين أقسام الكارت */
@Composable
fun CardDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(GoldenBorder.copy(alpha = 0.2f))
    )
}

/** عدّاد الكمية — ناقص / رقم / زائد */
@Composable
fun QuantityStepper(
    quantity: Int,
    onChange: (Int) -> Unit,
    enabled: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−", enabled && quantity > 1) { onChange(quantity - 1) }

        Text(
            text = quantity.toString(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        StepperButton("+", enabled) { onChange(quantity + 1) }
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(GoldenBorder.copy(alpha = if (enabled) 0.15f else 0.05f))
            .border(
                1.dp,
                GoldenBorder.copy(alpha = if (enabled) 0.5f else 0.15f),
                CircleShape
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = GoldenBorder.copy(alpha = if (enabled) 1f else 0.35f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
