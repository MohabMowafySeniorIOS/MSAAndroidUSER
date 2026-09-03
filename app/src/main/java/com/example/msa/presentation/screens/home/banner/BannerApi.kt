package com.msa.android.presentation.screens.home.banner

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * إندبوينت البانرات.
 *
 * الرابط الكامل: `https://api.msagold.com/api/v1/banners`
 *
 * ⚠️ المسار هنا **relative** (مالوش "/" في الأول) عشان ما يمسحش الـ path
 * بتاع الـ baseUrl. اختار السطر اللي يناسب الـ Retrofit بتاعك:
 *
 *   • baseUrl = "https://api.msagold.com/api/v1/"  →  @GET("banners")      ← الحالي
 *   • baseUrl = "https://api.msagold.com/api/"     →  @GET("v1/banners")
 *   • baseUrl = "https://api.msagold.com/"         →  @GET("api/v1/banners")
 */
interface BannerApi {

    /**
     * @param perPage الـ API افتراضياً بيرجّع ١٥ بانر في الصفحة. بنطلب ٥٠
     *   عشان كل البانرات تيجي في ريكوست واحد ومنحتاجش نلف على الصفحات.
     *   لو الباك اند مش بيدعم البارامتر ده بيتجاهله، فمفيش ضرر.
     */
    @GET("banners")
    suspend fun getBanners(
        @Query("per_page") perPage: Int = 50
    ): BannersResponseDto
}
