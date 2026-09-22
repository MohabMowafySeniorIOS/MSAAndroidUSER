package com.msa.android.presentation.screens.language

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * شاشة اللغة — نفس `ChooseLanguageVCFirst` في iOS.
 *
 * بتشتغل في وضعين زي الآيفون بالظبط (`is_fro_side`):
 *   • `onBack == null` → وضع أول تشغيل: ترحيب من غير رجوع.
 *   • `onBack != null` → مفتوحة من «المزيد»: توب بار وزرار رجوع.
 *
 * ملحوظة: التطبيق مترجم للعربي والإنجليزي بس (`resourceConfigurations`
 * فيها "en" و"ar")، فمفيش أردو هنا زي الآيفون — لو اتضافت `values-ur`
 * ضيف صف تالت هنا.
 */
@Composable
fun LanguageScreen(
    onApplied: () -> Unit,
    onBack: (() -> Unit)? = null,
    vm: LanguageViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            if (onBack != null) {
                MSATopBar(title = stringResource(R.string.language_title), onBack = onBack)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (onBack == null) Modifier.statusBarsPadding() else Modifier)
                    .padding(horizontal = 24.dp)
                    .padding(top = if (onBack == null) 40.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(Modifier.weight(0.3f))

            if (onBack == null) {
                Text(
                    text = stringResource(R.string.welcome_msa_emoji),
                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
            }
            Text(
                text = stringResource(R.string.choose_language_hint),
                color = Color.White, fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(28.dp))

            LanguageRow("عربي", "🇪🇬", state.selected == "ar") { vm.select("ar") }
            Spacer(Modifier.height(14.dp))
            LanguageRow("English", "🇺🇸", state.selected == "en") { vm.select("en") }

            Spacer(Modifier.weight(0.4f))

            GoldButton(text = stringResource(R.string.apply)) {
                vm.apply {
                    (context as? Activity)?.recreate()
                    onApplied()
                }
            }
            Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun LanguageRow(label: String, flag: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .then(if (selected) Modifier.border(1.5.dp, GoldenBorder, RoundedCornerShape(14.dp)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (selected) GoldenBorder else Color.White
        )
        Spacer(Modifier.weight(1f))
        Text(text = label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(6.dp))
        Text(text = flag, fontSize = 20.sp)
    }
}
