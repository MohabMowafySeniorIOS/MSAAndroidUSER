package com.msa.android.presentation.screens.home.banner

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * ⚠️⚠️ اقرا ده الأول ⚠️⚠️
 *
 * الموديول ده بيعمل Retrofit خاص بيه عشان الملفات تشتغل من غير ما تعتمد
 * على حاجة في مشروعك. **لو عندك Retrofit متظبط أصلاً في NetworkModule
 * (وده الأغلب)** اعمل الآتي بدل ما تسيب الملف ده زي ما هو:
 *
 *   1. امسح `provideBannerRetrofit` و `provideBannerOkHttp` و BASE_URL.
 *   2. سيب `provideBannerApi` بس، وخلّيها كده:
 *
 *          @Provides
 *          @Singleton
 *          fun provideBannerApi(retrofit: Retrofit): BannerApi =
 *              retrofit.create(BannerApi::class.java)
 *
 *      (أو انقل السطرين دول للـ NetworkModule بتاعك وامسح الملف ده خالص)
 *
 * كده البانر هيستخدم نفس الـ OkHttp والـ interceptors والتوكن بتوع باقي
 * التطبيق بدل ما يعمل client تاني على الفاضي.
 */
@Module
@InstallIn(SingletonComponent::class)
object BannerModule {

    /** الـ base URL الحقيقي بتاع MSA. لازم ينتهي بـ "/" */
    private const val BASE_URL = "https://api.msagold.com/api/v1/"

    @Provides
    @Singleton
    @BannerRetrofit
    fun provideBannerOkHttp(): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @BannerRetrofit
    fun provideBannerRetrofit(@BannerRetrofit client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBannerApi(@BannerRetrofit retrofit: Retrofit): BannerApi =
        retrofit.create(BannerApi::class.java)
}

/**
 * Qualifier عشان الـ Retrofit بتاع البانر ما يتضاربش مع الـ Retrofit
 * الأساسي بتاع المشروع لو الاتنين موجودين في نفس الوقت.
 * هيتشال مع الموديول لو ربطته بالـ NetworkModule بتاعك.
 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BannerRetrofit
