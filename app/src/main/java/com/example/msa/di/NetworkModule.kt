package com.msa.android.di

import com.msa.android.BuildConfig
import com.msa.android.data.source.network.AuthInterceptor
import com.msa.android.data.source.network.ApiLoadingInterceptor
import com.msa.android.data.source.network.MsaApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Networking layer DI — OkHttp + Moshi + Retrofit. One shared OkHttpClient
 * configured with sensible timeouts and HTTP logging on debug builds. Moshi
 * uses the reflective Kotlin adapter so we don't have to ship the codegen.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // بقى فيه هيدر Authorization في الطلبات، فاللوج الكامل
            // هيطبع التوكن. سيبها BODY في الديبج بس.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        auth: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            // التوكن قبل اللوج عشان الهيدر يبان في اللوج وقت التشخيص
            .addInterceptor(auth)
            .addInterceptor(ApiLoadingInterceptor())
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(JsonAdapters())
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(MsaApi.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideMsaApi(retrofit: Retrofit): MsaApi =
        retrofit.create(MsaApi::class.java)
}
