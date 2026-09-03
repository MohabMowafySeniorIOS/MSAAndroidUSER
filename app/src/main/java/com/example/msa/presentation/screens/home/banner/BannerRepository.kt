package com.msa.android.presentation.screens.home.banner

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * بيجيب بانرات الرئيسية من الباك اند.
 *
 * ⚠️ **مفيش كاش خالص**: كل مرة التطبيق يتفتح بنسأل الـ API من جديد.
 * يعني لو الداشبورد وقّفت بانر أو نشرت واحد جديد، المستخدم بيشوف الجديد
 * من أول فتحة — مش بعد ما الكاش يقدم. الثمن إن مفيش بانر بيظهر وهو
 * أوفلاين، وده مقبول لأن البانر إعلان مش محتوى أساسي.
 */
@Singleton
class BannerRepository @Inject constructor(
    private val api: BannerApi
) {

    companion object {
        private const val TAG = "BannerRepo"

        /**
         * اتعرض الـ popup في التشغيلة دي؟
         *
         * فلاج في الذاكرة على مستوى الـ process — مفيش أي حاجة بتتحفظ
         * على الديسك. بيترمي لوحده لما التطبيق يتقفل خالص، وبيفضل موجود
         * طول ما التطبيق شغال (حتى لو راح للخلفية).
         *
         * لازم يبقى هنا مش جوه الـ ViewModel: لو المستخدم راح لتاب تاني
         * ورجع للرئيسية ممكن الـ ViewModel يتعمل من جديد — والفلاج ده هو
         * اللي بيمنع الإعلان إنه يظهر مرتين في نفس الجلسة.
         *
         * عايزه يظهر كل مرة الرئيسية تتفتح مش كل فتحة تطبيق؟ خلّي
         * `shouldShowPopup` ترجّع `items.isNotEmpty()` وبس.
         */
        @Volatile
        private var shownThisLaunch = false
    }

    // MARK: - Fetch

    /**
     * بيجيب البانرات المفعّلة من الشبكة.
     *
     * بترجّع `null` لو الريكوست فشل — مفيش كاش نرجع له، فمفيش حاجة
     * نعرضها.
     */
    suspend fun fetch(): List<Banner>? = withContext(Dispatchers.IO) {
        try {
            // فلترة محلية على اللي شغّال دلوقتي (status + الفترة الزمنية).
            api.getBanners().data.orEmpty().toDomain().activeNow()
        } catch (t: Throwable) {
            Log.e(TAG, "fetchBanners error ----> ${t.message}")
            null
        }
    }

    // MARK: - ظهور الـ popup

    /** هل نعرض الـ popup دلوقتي؟ (مرة واحدة كل تشغيلة للتطبيق) */
    fun shouldShowPopup(items: List<Banner>): Boolean =
        items.isNotEmpty() && !shownThisLaunch

    /**
     * بنسجّله وهو بيتعرض مش وهو بيتقفل — عشان لو المستخدم قفل الإعلان
     * ورجع للرئيسية ما يظهرش تاني في نفس الجلسة.
     */
    fun markPopupShown() {
        shownThisLaunch = true
    }
}
