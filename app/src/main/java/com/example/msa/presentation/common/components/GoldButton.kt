package com.msa.android.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msa.android.presentation.theme.Gold
import com.msa.android.presentation.theme.GoldenBorder
import com.msa.android.presentation.theme.MSAGradients

/**
 * الزرار الأساسي.
 *
 * `filled = false` بيدي نسخة بحدّ ذهبي من غير تعبئة — للأفعال الثانوية.
 * `loading = true` بيعرض مؤشر تحميل وبيقفل الضغط، فمفيش طلبين بيتبعتوا
 * لو المستخدم دوس مرتين بسرعة.
 */
@Composable
fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = true,
    big: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val height = if (big) 56.dp else 46.dp
    val clickable = enabled && !loading

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .then(
                if (filled) Modifier.background(MSAGradients.goldButton())
                else Modifier.border(1.5.dp, GoldenBorder, RoundedCornerShape(height / 2))
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Color.White),
                enabled = clickable,
                onClick = onClick
            )
            .alpha(if (clickable) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = if (filled) Color.White else Gold,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = text,
                color = if (filled) Color.White else GoldenBorder,
                fontSize = if (big) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
