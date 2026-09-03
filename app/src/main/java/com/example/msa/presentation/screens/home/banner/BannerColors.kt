package com.msa.android.presentation.screens.home.banner

import androidx.compose.ui.graphics.Color

/**
 * ألوان البراند المستخدمة في البانر — مأخوذة من Brand Assets / Colors.ai.
 *
 * معرّفة هنا محلياً عشان فولدر البانر يشتغل من غير ما يعتمد على
 * Theme.kt بتاعك. لما تنقل ألوان البراند للـ theme، امسح الملف ده
 * وبدّل الأسماء بالمقابل بتاعها من `com.msa.android.presentation.theme`.
 */

/** الذهبي الأساسي. */
val BannerGold = Color(0xFFE8B138)

/** الكريمي — بداية التدرج الذهبي. */
val BannerCream = Color(0xFFFEFBF4)

/** الحبر — النص فوق أي خلفية ذهبية. */
val BannerInk = Color(0xFF150F0E)

/** خلفية الكارت قبل ما الميديا تحمّل. */
val BannerPlaceholder = Color(0xFF1A1211)
