package com.msa.android.presentation.screens.appstatus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.BuildConfig
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.theme.GoldenBorder

/**
 * Blocks the whole app until the user updates — no back button, no dismiss.
 * Shown from AppGate whenever AppStatus.NeedsUpdate is active (see
 * AppStatusViewModel — compares BuildConfig.VERSION_NAME against the
 * `version` field on appVersion/appVersion in Firestore).
 */
@Composable
fun ForceUpdateScreen() {
    val context = LocalContext.current

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
            Text("⬆️", fontSize = 64.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                "في تحديث جديد للتطبيق",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "لازم تحدّث التطبيق من متجر Google Play عشان تكمل استخدامه.",
                color = Color(0xFFCCCCCC),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { openPlayStore(context) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldenBorder)
            ) {
                Text("تحديث الآن", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/** Opens the Play Store app if installed, otherwise falls back to the web listing. */
internal fun openPlayStore(context: android.content.Context) {
    val appId = BuildConfig.APPLICATION_ID
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}
