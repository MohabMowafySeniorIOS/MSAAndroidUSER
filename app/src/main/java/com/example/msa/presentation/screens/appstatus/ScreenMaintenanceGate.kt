package com.msa.android.presentation.screens.appstatus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msa.android.R
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.theme.GoldenBorderSoft

/**
 * Wraps ONE tab's content (Home · Dollar prices · News · Bullions).
 *
 * When that screen's flag is true in `appVersion/appVersion` the content is
 * replaced by [ScreenMaintenanceView]; the bottom bar lives outside this gate
 * in MainScreen, so the user can still switch to the other tabs — that's the
 * difference from [AppGate], which takes the whole app down.
 *
 * The flags are watched with a live snapshot listener, so the overlay appears
 * and disappears on its own when the value changes in the Firebase console.
 *
 * The ViewModel is hoisted to the caller's scope by default, so all four tabs
 * share a single Firestore listener rather than opening one each.
 */
@Composable
fun ScreenMaintenanceGate(
    screen: MaintainedScreen,
    vm: ScreenMaintenanceViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ui = state.forScreen(screen)

    if (!ui.isUnderMaintenance) {
        content()
        return
    }

    // Firestore copy wins when present; otherwise the bundled translation for
    // the active locale (values/ = EN, values-ar/ = AR).
    ScreenMaintenanceView(
        title = ui.title.ifBlank { stringResource(R.string.maintenance_screen_title) },
        message = ui.message.ifBlank { stringResource(R.string.maintenance_screen_message) }
    )
}

/**
 * The "this screen is under maintenance" placeholder, full-screen.
 *
 * Note there's no back handler and no exit button (unlike the app-wide
 * MaintenanceScreen): the user isn't trapped here, they just pick another tab.
 * Bottom padding keeps the card clear of the floating bottom bar.
 */
@Composable
fun ScreenMaintenanceView(
    title: String = stringResource(R.string.maintenance_screen_title),
    message: String = stringResource(R.string.maintenance_screen_message)
) {
    MSABackground(showBars = false) {
        ScreenMaintenanceCard(
            title = title,
            message = message,
            modifier = Modifier
                .fillMaxSize()
                // مساحة الشريط السفلي العائم
                .padding(bottom = 90.dp)
        )
    }
}

/**
 * الكارت لوحده — من غير خلفية ولا مسافة سفلية مفروضة.
 *
 * اتفصل عن [ScreenMaintenanceView] عشان الشاشات اللي ليها هيدر بزرار
 * رجوع، زي شاشة الفيدرالي. النسخة الكاملة بتغطي الشاشة **كلها** بما
 * فيها الهيدر، وساعتها المستخدم اللي دخل الشاشة من الرئيسية بيبقى
 * محبوس من غير طريقة يرجع بيها — التبويبات مش عندها المشكلة دي لأن
 * شريطها السفلي برّه الجيت أصلاً.
 */
@Composable
fun ScreenMaintenanceCard(
    title: String = stringResource(R.string.maintenance_screen_title),
    message: String = stringResource(R.string.maintenance_screen_message),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x14FFFFFF))
                .border(1.dp, GoldenBorderSoft, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🛠️", fontSize = 56.sp)
            Spacer(Modifier.height(18.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                color = Color(0xFFCCCCCC),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
