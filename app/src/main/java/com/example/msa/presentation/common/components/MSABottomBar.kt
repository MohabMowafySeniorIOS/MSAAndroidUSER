package com.msa.android.presentation.common.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R
import com.msa.android.presentation.theme.GoldenBorder

/**
 * Floating bottom bar that matches the iOS app:
 *   icon + label per tab.
 *
 *   Visual order (RTL):
 *     [الشاشة العالمية] [أسعار العملات] [السبائك] [الأخبار] [المزيد]
 *
 *  The currently-selected tab tints its icon and label gold.
 */
@Composable
fun MSABottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(60.dp)
            .shadow(8.dp, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xCC1C1C1E))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab.entries.forEach { tab ->
            BottomBarItem(
                tab = tab,
                selected = selected == tab,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = if (selected) GoldenBorder else Color.White,
        label = "tint"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 30.dp else 26.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0x33D4AF37) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(tab.iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(tint),
                modifier = Modifier.size(if (selected) 18.dp else 17.dp)
            )
        }
        Spacer(Modifier.height(1.dp))
        Text(
            text = stringResource(tab.titleRes),
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Visible
        )
    }
}

/**
 * Tabs matching iOS UITabBarController in Home.storyboard.
 *
 * Visual order in Arabic/RTL (right → left):
 *   [الشاشة العالمية] [أسعار العملات] [السبائك] [الأخبار] [المزيد]
 *
 * In the enum (LTR Kotlin order) that's:
 *   HOME → DOLLAR → BULLIONS → NEWS → MORE
 */
enum class BottomTab(val iconRes: Int, val titleRes: Int) {
    HOME    (R.drawable.ic_tab_home,       R.string.tab_home),
    DOLLAR  (R.drawable.ic_tab_dollar,     R.string.tab_dollar),
    BULLIONS(R.drawable.ic_tab_calculator, R.string.tab_bullions),
    NEWS    (R.drawable.ic_tab_news,       R.string.tab_news),
    MORE    (R.drawable.ic_tab_more,       R.string.tab_more)
}
