package com.msa.android.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.R

/**
 * Top bar that exactly mimics iOS:
 *  • Centered title
 *  • White rounded-square back button at the START (RTL = right side)
 *  • Optional share button at the END (RTL = left side)
 *  • Sits BELOW the system status bar (safe area, like iOS)
 *
 * The back arrow uses `ic_back_arrow.xml` which has `android:autoMirrored="true"`
 * — Android automatically flips it in RTL layouts so it always points "back".
 *
 * Either side button can appear independently:
 *   showBack = true,  showShare = true  → back on right, share on left
 *   showBack = false, showShare = true  → only share on left
 *   showBack = true,  showShare = false → only back on right
 */
@Composable
fun MSATopBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    showShare: Boolean = false,
    onShare: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Back button — START side (right in RTL)
        if (showBack) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Share button — END side (left in RTL)
        if (showShare) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // العنوان لوحده — لوجو الهيدر اتشال من كل الشاشات
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
