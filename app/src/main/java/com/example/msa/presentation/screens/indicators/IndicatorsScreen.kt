package com.msa.android.presentation.screens.indicators

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.common.components.SegmentedToggle
import com.msa.android.presentation.common.components.TVSymbols
import com.msa.android.presentation.common.components.TVWidget
import com.msa.android.presentation.common.components.TradingViewPanel

/**
 * المؤشرات — رسم بياني حيّ من تريدنج فيو.
 *
 * قبل كده كانت الشاشة بتعرض بيانات مولّدة محلياً بمولّد أرقام عشوائي
 * (`PriceDataGenerator`) حوالين نقاط ثابتة، يعني الرسم مكانش بيعبّر عن
 * السوق. دلوقتي البيانات من مصدر سوق حقيقي.
 */
@Composable
fun IndicatorsScreen(
    onBack: () -> Unit,
    isArabic: Boolean = true
) {
    var goldSelected by remember { mutableStateOf(true) }

    MSABackground {
        Column(Modifier.fillMaxSize()) {
            MSATopBar(title = stringResource(R.string.indicators), onBack = onBack)

            SegmentedToggle(
                selected = goldSelected,
                options = listOf(
                    true to stringResource(R.string.gold),
                    false to stringResource(R.string.silver)
                ),
                onSelect = { goldSelected = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            TradingViewPanel(
                widget = TVWidget.CHART,
                symbol = if (goldSelected) TVSymbols.GOLD else TVSymbols.SILVER,
                isArabic = isArabic,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = stringResource(R.string.market_global_note),
                color = Color(0xFF9E9898),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }
    }
}
