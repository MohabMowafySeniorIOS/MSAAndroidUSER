package com.msa.android.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.msa.android.R

/**
 * خلفية كل شاشات التطبيق — نفس `BGSwiftUIView` في iOS بالظبط.
 *
 * الخلفية بقت صورة واحدة جاهزة بأشكال الهوية بدل ثلاث طبقات:
 *   • قبل: تدرّج + لمعة ذهب + نقشة بشفافية 15%
 *   • دلوقتي: لون أساس + الصورة كاملة الوضوح
 *
 * الصورة نفسها فيها التدرّج والأشكال، فاللمعة الذهبية اتشالت — كانت
 * هتغيّر ألوان التصميم لو فضلت فوقه. اللون الأساس تحتها بلون الصورة
 * الغالب عشان لو الشاشة أطول من نسبة الصورة، الفراغ يبقى بنفس اللون.
 *
 * `showBars` سايبها زي ما هي عشان الكود القديم اللي بيبعتها ما يقعش.
 */
@Composable
fun MSABackground(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") showBars: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // الطبقة 1: لون الأساس — نفس اللون الغالب في الصورة
            .background(Color(0xFF2E2020))
    ) {
        // الطبقة 2: خلفية الهوية، كاملة الوضوح وبتملا الشاشة
        Image(
            painter = painterResource(R.drawable.bg_texture),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Foreground content
        content()
    }
}
