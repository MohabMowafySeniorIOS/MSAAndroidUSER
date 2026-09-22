package com.msa.android.presentation.screens.appstatus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.presentation.common.components.MSABackground

/**
 * Shown from AppGate whenever `appVersion/appVersion.needMaintain == true`.
 * The doc is watched with a live Firestore listener (see AppStatusViewModel),
 * so this appears/disappears on its own the moment the flag changes in
 * Firestore — no need to relaunch the app either way.
 */
@Composable
fun MaintenanceScreen() {

    // Consume system back so the user can't get past this screen.
    BackHandler(enabled = true) {}

    MSABackground(showBars = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🛠️", fontSize = 64.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                "في شغل صيانة دلوقتي",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "في مشكلة بسيطة وإحنا بنحلها دلوقتي، التطبيق هيرجع يشتغل تاني قريب.\nمعلش على الإزعاج 🙏",
                color = Color(0xFFCCCCCC),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
