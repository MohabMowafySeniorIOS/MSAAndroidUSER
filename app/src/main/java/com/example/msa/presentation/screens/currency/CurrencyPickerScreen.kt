package com.msa.android.presentation.screens.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/** Static list — same set as the iOS CurrencyTypeVC picker. */
data class CurrencyOption(val code: String, val arabicName: String)

private val SUPPORTED_CURRENCIES = listOf(
    CurrencyOption("USD", "الدولار الأمريكي"),
    CurrencyOption("EUR", "اليورو"),
    CurrencyOption("GBP", "الجنيه الإسترليني"),
    CurrencyOption("SAR", "الريال السعودي"),
    CurrencyOption("AED", "الدرهم الإماراتي"),
    CurrencyOption("KWD", "الدينار الكويتي"),
    CurrencyOption("QAR", "الريال القطري"),
    CurrencyOption("BHD", "الدينار البحريني"),
    CurrencyOption("OMR", "الريال العماني"),
    CurrencyOption("JOD", "الدينار الأردني"),
    CurrencyOption("CHF", "الفرنك السويسري"),
    CurrencyOption("CAD", "الدولار الكندي"),
    CurrencyOption("AUD", "الدولار الأسترالي"),
    CurrencyOption("CNY", "اليوان الصيني"),
    CurrencyOption("JPY", "الين الياباني"),
  //  CurrencyOption("TRY", "الليرة التركية")
)

@Composable
fun CurrencyPickerScreen(
    currentCurrency: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    MSABackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            MSATopBar(
                title = stringResource(R.string.choose_currency),
                showBack = true,
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SUPPORTED_CURRENCIES, key = { it.code }) { opt ->
                    CurrencyRow(
                        code = opt.code,
                        name = opt.arabicName,
                        selected = opt.code == currentCurrency,
                        onClick = { onSelect(opt.code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    code: String,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) GoldenBorder else Color(0x33C9A05A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0x33C9A05A) else Color(0xCC1C1C1E))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = GoldenBorder,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = code,
            color = GoldenBorder,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
