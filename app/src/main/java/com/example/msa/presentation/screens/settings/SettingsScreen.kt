package com.msa.android.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.data.source.local.NotificationPreferences.Category
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder

/**
 * الإعدادات — تحكّم منفصل لكل نوع إشعار.
 * نفس الشاشة والترتيب في iOS (`NotificationSettingsVC`).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            MSATopBar(title = stringResource(R.string.settings_title), onBack = onBack)

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.notifications_section),
                color = Gold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingSwitch(
                    title = stringResource(R.string.notif_gold),
                    subtitle = stringResource(R.string.notif_gold_desc),
                    checked = state.enabled[Category.GOLD] ?: true
                ) { vm.toggle(Category.GOLD, it) }

                SettingSwitch(
                    title = stringResource(R.string.notif_silver),
                    subtitle = stringResource(R.string.notif_silver_desc),
                    checked = state.enabled[Category.SILVER] ?: true
                ) { vm.toggle(Category.SILVER, it) }

                SettingSwitch(
                    title = stringResource(R.string.notif_dollar),
                    subtitle = stringResource(R.string.notif_dollar_desc),
                    checked = state.enabled[Category.DOLLAR] ?: true
                ) { vm.toggle(Category.DOLLAR, it) }

                SettingSwitch(
                    title = stringResource(R.string.notif_news),
                    subtitle = stringResource(R.string.notif_news_desc),
                    checked = state.enabled[Category.NEWS] ?: true
                ) { vm.toggle(Category.NEWS, it) }

                SettingSwitch(
                    title = stringResource(R.string.notif_general),
                    subtitle = stringResource(R.string.notif_general_desc),
                    checked = state.enabled[Category.GENERAL] ?: true
                ) { vm.toggle(Category.GENERAL, it) }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.notif_system_hint),
                color = Color(0xFF9E9898),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 22.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, color = Color(0xFFB0AAAA), fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Gold,
                uncheckedThumbColor = Color(0xFFB0AAAA),
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
