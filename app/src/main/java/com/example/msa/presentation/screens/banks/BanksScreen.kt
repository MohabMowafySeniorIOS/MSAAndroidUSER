package com.msa.android.presentation.screens.banks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.msa.android.R
import com.msa.android.domain.model.BankRate
import com.msa.android.domain.model.Trend
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.GreenTrend
import com.msa.android.presentation.theme.RedTrend

/**
 * Dollar/Currency prices screen. Mirrors iOS DollarPricesOnBankVC + DollarCell:
 *   • Header row: شراء | بيع | البنك
 *   • Row: [bank logo] [bank name] [sell value + arrow] [buy value + arrow]
 *   • Central bank row gets gold tint
 *   • Up arrow → green; Down arrow → red; Same → green (iOS default)
 */
@Composable
fun BanksScreen(
    onChangeCurrency: () -> Unit = {},
    vm: BanksViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(
                title = stringResource(R.string.currency_prices),
                showBack = false,
                showShare = true,
                onShare = { com.msa.android.presentation.common.shareApp(ctx) }
            )

            // Currency selector — shows current code and a button to change it
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC1C1C1E))
                        .border(1.dp, GoldenBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.currency_label) + ": ",
                        color = Color(0xFFCCCCCC),
                        fontSize = 13.sp
                    )
                    Text(
                        text = state.selectedCurrency,
                        color = GoldenBorder,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.weight(1f))

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldenBorder)
                        .clickable { onChangeCurrency() }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.change_currency),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Header row — [bank, buy, sell]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.bank),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.8f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.buy_price),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.sell_price),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Center
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    BankRow(
                        item = item,
                        notificationsEnabled = state.notifEnabled[item.name]
                            ?: (item.name.contains("المركز") || item.name.contains("Central")),
                        onNotificationToggle = { enabled ->
                            vm.setBankNotificationEnabled(item.name, enabled)
                        },
                        onClick = {
                            item.bankUrl?.takeIf { it.isNotBlank() }?.let {
                                uriHandler.openUri(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable

private fun BankRow(

    item: BankRate,

    notificationsEnabled: Boolean,

    onNotificationToggle: (Boolean) -> Unit,

    onClick: () -> Unit

) {

    val isCentralBank = item.name.contains("المركز") || item.name.contains("Central")

    val rowBg = if (isCentralBank) Color(0xFFB58934) else Color.Transparent

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .clip(RoundedCornerShape(12.dp))

            .background(rowBg)

            .border(1.dp, GoldenBorder, RoundedCornerShape(12.dp))

    ) {

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .clickable { onClick() }

                .padding(horizontal = 10.dp, vertical = 12.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {
            // Order is [name+logo, buy, sell] so in RTL it visually reads right→left as:
            //   [logo + name (right)] | [buy + arrow (middle)] | [sell + arrow (left)]
            // Inside the name+logo group: logo FIRST (rightmost), name SECOND (toward middle).

            Row(
                modifier = Modifier.weight(1.8f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start    // start = right in RTL, packs items to the right
            ) {
                AsyncImage(
                    model = item.logo,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(4.dp)
                )
                Spacer(Modifier.width(12.dp))      // spacing between logo and name
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }

            TrendValue(item.buy,  item.trend, modifier = Modifier.weight(1.1f))
            TrendValue(item.sell, item.trend, modifier = Modifier.weight(1.1f))
        }

        // سويتش تفعيل/تعطيل إشعار تغيّر السعر - بيظهر بس على تبويب الدولار
        // (زي iOS DollarCell بالظبط)، لإن السيرفر مابيبعتش إشعارات تغيّر
        // السعر لكل بنك إلا للدولار.
        if (item.currency == "USD") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.bank_price_notifications),
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationToggle,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GoldenBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun TrendValue(value: String, trend: Trend, modifier: Modifier = Modifier) {
    val arrowRes = if (trend == Trend.DOWN) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
    val arrowTint = when (trend) {
        Trend.UP, Trend.SAME -> GreenTrend
        Trend.DOWN -> RedTrend
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(arrowRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(arrowTint),
            modifier = Modifier.size(22.dp)      // ← bigger arrow (was the default ~18dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,                     // ← bigger font
            fontWeight = FontWeight.Bold
        )
    }
}
