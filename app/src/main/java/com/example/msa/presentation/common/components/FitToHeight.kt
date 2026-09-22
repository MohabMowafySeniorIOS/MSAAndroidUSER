package com.msa.android.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * بيصغّر محتواه عشان يترص في ارتفاع محدد — من غير ما يغيّر تصميمه.
 *
 * الرئيسية المفروض تاخد ارتفاع الشاشة من غير سكرول. الشاشات بتختلف
 * كتير في الطول، فبدل ما نكتب أحجام مختلفة لكل مقاس، بنقيس المحتوى
 * بحجمه الطبيعي وبنرسمه مصغّر بنسبة واحدة لو مش راضي يترص.
 *
 * ## ليه تصغير موحّد مش أحجام مختلفة؟
 *
 * لو غيّرنا كل `fontSize` و`padding` لوحده، كل مقاس شاشة بيبقى تصميم
 * مستقل لازم يتراجع بالعين. التصغير الموحّد بيحافظ على النِّسب زي ما
 * هي بالظبط — الشاشة الصغيّرة بتبقى نفس التصميم من بعيد شوية.
 *
 * ## السكرول لسه موجود
 *
 * `minScale` أرضية: تحتها التصغير بيبقى نص مش مقروء. لو المحتوى لسه
 * أطول من المساحة عند الأرضية دي، الكومبوزابل بيقول ارتفاعه الحقيقي
 * والحاوية اللي فوقه (لو `verticalScroll`) بتسكرول عادي.
 *
 * **مفيش أي حالة بيتقص فيها محتوى** — يا بيترص، يا بيسكرول.
 *
 * ## إزاي التصغير بيحصل
 *
 * `graphicsLayer` بتأثر على **الرسم** مش على القياس، فالمحتوى جواه
 * بيتقاس بحجمه الطبيعي مرة واحدة بس. ده مهم لسببين:
 *
 *  - مفيش `subcompose` مرتين، يعني مفيش نسختين من الـ state ولا
 *    طلبين شبكة.
 *  - الضغطات بتتظبط لوحدها — Compose بيعكس الترانسفورم وهو بيدوّر على
 *    اللي المستخدم دوس عليه.
 *
 * النسبة بتتخزّن في `mutableFloatStateOf` وبتتقرا **جوه لامبدا
 * `graphicsLayer`** بس. القراءة هناك معناها إن تغييرها بيعيد الرسم
 * لوحده من غير ما يعيد القياس — وده اللي بيمنع لوب القياس اللانهائي.
 *
 * @param targetHeight الارتفاع اللي المفروض المحتوى يترص فيه. بنمرّره
 *   كبارامتر مش بناخده من الـ constraints، لأن الكومبوزابل ده جوه
 *   `verticalScroll` والارتفاع المتاح هناك بيبقى لا نهائي.
 * @param minScale أقل نسبة مسموح بيها. الافتراضي ٠٫٧٠ — تحتها النص
 *   ١٤sp بيبقى أقل من ١٠sp.
 */
@Composable
fun FitToHeight(
    targetHeight: Dp,
    modifier: Modifier = Modifier,
    minScale: Float = 0.70f,
    content: @Composable () -> Unit
) {
    val targetPx = with(LocalDensity.current) { targetHeight.roundToPx() }

    // النسبة الحالية. بتتكتب في مرحلة القياس وبتتقرا في مرحلة الرسم بس.
    val scaleState = remember { mutableFloatStateOf(1f) }

    SubcomposeLayout(modifier) { constraints ->

        val placeables = subcompose(Unit) {
            Box(
                Modifier.graphicsLayer {
                    val s = scaleState.floatValue
                    scaleX = s
                    scaleY = s
                    // من فوق وفي النص: المحتوى بيفضل ملزوق بأول
                    // المساحة، وبيتوسّط أفقياً بدل ما يزيح ناحية.
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
            ) {
                content()
            }
        }.map {
            it.measure(
                Constraints(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    // بنقيسه بحجمه الطبيعي — من غير سقف للارتفاع
                    maxHeight = Constraints.Infinity
                )
            )
        }

        val naturalHeight = placeables.maxOfOrNull { it.height } ?: 0

        val scale = when {
            naturalHeight <= 0 || targetPx <= 0 -> 1f
            naturalHeight <= targetPx -> 1f          // راضي أصلاً، سيبه
            else -> (targetPx.toFloat() / naturalHeight).coerceAtLeast(minScale)
        }

        scaleState.floatValue = scale

        // الارتفاع اللي بنحجزه هو المرسوم فعلاً مش الطبيعي — عشان اللي
        // تحتنا يترص صح، والحاوية تعرف تسكرول لو لسه فيه زيادة.
        val placedHeight = (naturalHeight * scale).roundToInt()

        layout(constraints.maxWidth, placedHeight) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}
