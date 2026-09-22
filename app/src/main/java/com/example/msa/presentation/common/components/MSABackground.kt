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
    /**
     * صورة الخلفية. الافتراضي خلفية الذهب — الشاشة الرئيسية بتبعت
     * خلفية الفضة لما المستخدم يختار «فضة».
     */
    textureRes: Int = R.drawable.bg_texture,
    /**
     * لون الأساس تحت الصورة. بيبان بس لو الشاشة أطول من نسبة الصورة،
     * فلازم يبقى قريب من اللون الغالب فيها وإلا هيبان شريط غريب تحت.
     */
    baseColor: Color = Color(0xFF2E2020),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // الطبقة 1: لون الأساس — نفس اللون الغالب في الصورة
            .background(baseColor)
    ) {
        // الطبقة 2: خلفية الهوية، كاملة الوضوح وبتملا الشاشة.
        //
        // Crossfade مش Image مباشرة: التبديل بين خلفية الدهب والفضة
        // من غير انتقال بيبان كـ"وميض" مفاجئ وسط الشاشة.
        androidx.compose.animation.Crossfade(
            targetState = textureRes,
            animationSpec = androidx.compose.animation.core.tween(320),
            label = "bg_texture"
        ) { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                // Crop زي ما هي: الصورة بتملا الشاشة كلها وبتتقص من
                // الجناب بدل ما تتمطّ — نفس سلوك الخلفية الحالية
                // بالظبط، فمقاس الخلفية الجديدة هيطلع زي القديمة.
                contentScale = ContentScale.Crop
            )
        }

        // Foreground content
        content()
    }
}
