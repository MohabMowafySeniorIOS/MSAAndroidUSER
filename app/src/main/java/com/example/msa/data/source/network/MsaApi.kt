package com.msa.android.data.source.network

import com.msa.android.data.source.network.dto.ApiError
import com.msa.android.data.source.network.dto.AuthResponse
import com.msa.android.data.source.network.dto.ContactDto
import com.msa.android.data.source.network.dto.BranchDto
import com.msa.android.data.source.network.dto.BranchNearestEnvelope
import com.msa.android.data.source.network.dto.BullionMetalDto
import com.msa.android.data.source.network.dto.BullionVerifyDto
import com.msa.android.data.source.network.dto.BullionVerifyRequest
import com.msa.android.data.source.network.dto.CalendarDetailsDto
import com.msa.android.data.source.network.dto.CalendarEventsDto
import com.msa.android.data.source.network.dto.CalendarFiltersDto
import com.msa.android.data.source.network.dto.CalendarHistoryDto
import com.msa.android.data.source.network.dto.ContentBundleDto
import com.msa.android.data.source.network.dto.DataEnvelope
import com.msa.android.data.source.network.dto.DeviceTokenRequest
import com.msa.android.data.source.network.dto.FaqDto
import com.msa.android.data.source.network.dto.FomcEventDto
import com.msa.android.data.source.network.dto.FomcNextEnvelope
import com.msa.android.data.source.network.dto.LoginRequest
import com.msa.android.data.source.network.dto.MeResponse
import com.msa.android.data.source.network.dto.MessageResponse
import com.msa.android.data.source.network.dto.ChangePasswordRequest
import com.msa.android.data.source.network.dto.NewsApiResponse
import com.msa.android.data.source.network.dto.PortfolioItemDto
import com.msa.android.data.source.network.dto.PortfolioPayloadDto
import com.msa.android.data.source.network.dto.UpdateProfileRequest
import com.msa.android.data.source.network.dto.UserDto
import com.msa.android.data.source.network.dto.PageDto
import com.msa.android.data.source.network.dto.AddToCartRequest
import com.msa.android.data.source.network.dto.CartResponse
import com.msa.android.data.source.network.dto.OrderDto
import com.msa.android.data.source.network.dto.OrdersResponse
import com.msa.android.data.source.network.dto.OrderResponse
import com.msa.android.data.source.network.dto.PlaceOrderRequest
import com.msa.android.data.source.network.dto.ProductResponse
import com.msa.android.data.source.network.dto.ProductsResponse
import com.msa.android.data.source.network.dto.SetCartQuantityRequest
import com.msa.android.data.source.network.dto.ShopInfoDto
import com.msa.android.data.source.network.dto.RegisterRequest
import com.msa.android.data.source.network.dto.RegisterResponse
import com.msa.android.data.source.network.dto.ResendOtpRequest
import com.msa.android.data.source.network.dto.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST endpoints under `https://msagold.com/api/v1/`. iOS uses Alamofire with
 * the same base URL (see iOS `hostName` constant in `APIClient`); we declare
 * one interface here and Retrofit synthesizes the rest.
 */
interface MsaApi {
    /**
     * GET /news — fetches the admin-curated MSA news feed.
     * iOS sources this from `\(hostName)news` in `getManualNews()`.
     */
    @GET("news")
    suspend fun getNews(): NewsApiResponse

    /* ── الحساب ─────────────────────────────────────────────────────
     * بنرجّع Response<T> مش T مباشرة عشان نقدر نقرا جسم الخطأ:
     * لارافيل بيرجع رسائل التحقق في 422 والمستخدم لازم يشوفها.
     */

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    /** التوكن بيرجع من هنا بس — بعد تأكيد الكود */
    @POST("verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<AuthResponse>

    @POST("resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequest): Response<MessageResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("me")
    suspend fun me(): Response<MeResponse>

    @POST("logout")
    suspend fun logout(): Response<MessageResponse>

    @DELETE("account")
    suspend fun deleteAccount(): Response<MessageResponse>

    /**
     * تحديث توكن الجهاز واللغة.
     *
     * محتاج توكن دخول. بينده لما FCM يجدّد التوكن، ولما المستخدم يغيّر
     * لغة التطبيق. من غيره التوكن اللي عند السيرفر بيبوظ مع الوقت
     * والإشعار الموجّه بيروح في الهوا — التوكن كان بيتبعت مع التسجيل
     * والدخول بس.
     */
    @POST("device-token")
    suspend fun updateDeviceToken(@Body body: DeviceTokenRequest): Response<Unit>

    /* ── محتوى شاشة «المزيد» ───────────────────────────────────────── */

    /** كل المحتوى في طلب واحد — بيتنده عند فتح التطبيق */
    @GET("content")
    suspend fun content(): DataEnvelope<ContentBundleDto>

    @GET("pages/{slug}")
    suspend fun page(@Path("slug") slug: String): DataEnvelope<PageDto>

    @GET("faqs")
    suspend fun faqs(): DataEnvelope<List<FaqDto>>

    @GET("contact")
    suspend fun contact(): DataEnvelope<ContactDto>

    /**
     * التحقّق من كود QR على سبيكة.
     *
     * مفيش `Response<>` حواليها عن قصد: السيرفر بيرجّع ٢٠٠ للكود
     * المجهول زي السليم بالظبط، فمفيش حالة HTTP محتاجة تفسير هنا.
     * أي استثناء معناه شبكة أو سيرفر — مش «الكود غلط».
     */
    @POST("bullion-codes/verify")
    suspend fun verifyBullionCode(
        @Body body: BullionVerifyRequest,
        @Query("lang") lang: String,
    ): DataEnvelope<BullionVerifyDto>

    /** السبائك — الشجرة كاملة في طلب واحد */
    @GET("bullions")
    suspend fun bullions(): DataEnvelope<List<BullionMetalDto>>

    /* ── البروفايل ─────────────────────────────────────────────────── */

    @GET("profile")
    suspend fun profile(): Response<DataEnvelope<UserDto>>

    @PUT("profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<DataEnvelope<UserDto>>

    @PUT("profile/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<MessageResponse>

    /* ── المحفظة ───────────────────────────────────────────────────── */

    /**
     * الأسعار الحالية بتتبعت مع الطلب لأنها جاية من Firestore لحظة
     * بلحظة والسيرفر مش مشترك فيها.
     */
    @GET("portfolio")
    suspend fun portfolio(
        @Query("gold21") gold21: Double,
        @Query("silver999") silver999: Double
    ): Response<DataEnvelope<PortfolioPayloadDto>>

    /** إضافة قطعة — multipart عشان الصورة الاختيارية */
    @Multipart
    @POST("portfolio")
    suspend fun addPortfolioItem(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<DataEnvelope<PortfolioItemDto>>

    /**
     * التعديل بـ POST مش PUT: PHP مبيفكّش multipart في طلبات PUT،
     * فالحقول كانت هتوصل فاضية من غير أي رسالة خطأ.
     */
    @Multipart
    @POST("portfolio/{id}")
    suspend fun updatePortfolioItem(
        @Path("id") id: Long,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?
    ): Response<DataEnvelope<PortfolioItemDto>>

    @DELETE("portfolio/{id}")
    suspend fun deletePortfolioItem(@Path("id") id: Long): Response<MessageResponse>

    /* ── اجتماعات الفيدرالي (FOMC) ─────────────────────────────────
     * مفتوحة من غير توكن — البيانات عامة والمستخدم المفروض يشوف موعد
     * الاجتماع الجاي قبل ما يسجّل أصلاً.
     *
     * بنرجّع `DataEnvelope<T>` مباشرة مش `Response<T>`: مفيش هنا أخطاء
     * تحقّق المستخدم محتاج يقراها، فأي فشل بيتمسك كـ exception في
     * `FomcApiRepository` ويتحوّل لـ Result.failure.
     */

    /**
     * @param period  `upcoming` | `past` | `all`
     * @param limit   قايمة بسيطة من غير ترقيم صفحات (أقصى ١٠٠)
     */
    @GET("fomc/events")
    suspend fun fomcEvents(
        @Query("period") period: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): DataEnvelope<List<FomcEventDto>>

    @GET("fomc/events/upcoming")
    suspend fun fomcUpcoming(
        @Query("limit") limit: Int = 10
    ): DataEnvelope<List<FomcEventDto>>

    @GET("fomc/events/{id}")
    suspend fun fomcEvent(@Path("id") id: Long): DataEnvelope<FomcEventDto>

    /** الاجتماع القادم — `data` بيرجع null لو مفيش اجتماع معلن */
    @GET("fomc/next")
    suspend fun fomcNext(): FomcNextEnvelope

    /* ── التقويم الاقتصادي ─────────────────────────────────────────
     * مفتوح من غير توكن زي الفيدرالي. الرد **مجمّع بالتاريخ** من
     * السيرفر: لو جمّعنا في التطبيق، الصفحات كانت هتقطع اليوم في نصه
     * ويظهر عنوانه مرتين.
     */

    /**
     * أحداث مدى زمني.
     *
     * @param from  `YYYY-MM-DD`. الافتراضي في السيرفر: من امبارح
     * @param to    `YYYY-MM-DD`. الافتراضي: ٦ أيام قدّام — بيغطي كل
     *   تبويبات الشاشة (امبارح/النهاردة/بكرة/الأسبوع) في طلب واحد
     * @param impact قايمة بفواصل: `high,medium`
     * @param currencies قايمة بفواصل: `USD,EUR`
     */
    @GET("calendar/events")
    suspend fun calendarEvents(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("impact") impact: String? = null,
        @Query("currencies") currencies: String? = null,
        /**
         * لغة أسماء المؤشرات: `ar` أو `en`.
         *
         * المصدر بيبعت الأسماء بالإنجليزي بس، والسيرفر بيعرّبها من
         * قاموس عنده. المؤشر اللي مش في القاموس بيرجع بالإنجليزي —
         * اسم صحيح أحسن من خانة فاضية.
         *
         * بنبعت اللغة صريحة مش بنعتمد على `Accept-Language`: المستخدم
         * ممكن يكون مختار عربي جوه التطبيق وجهازه إنجليزي.
         */
        @Query("lang") lang: String? = null
    ): DataEnvelope<CalendarEventsDto>

    @GET("calendar/events/{id}")
    suspend fun calendarEvent(
        @Path("id") id: Long,
        @Query("lang") lang: String? = null
    ): DataEnvelope<CalendarDetailsDto>

    /** سلسلة المؤشر — الرسم البياني وتبويب التاريخ */
    @GET("calendar/events/{id}/history")
    suspend fun calendarHistory(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 24,
        @Query("lang") lang: String? = null
    ): DataEnvelope<CalendarHistoryDto>

    /** العملات الموجودة فعلاً — عشان الفلتر ما يبقاش قايمة ثابتة */
    @GET("calendar/filters")
    suspend fun calendarFilters(): DataEnvelope<CalendarFiltersDto>

    /* ── الفروع ────────────────────────────────────────────────────
     * مفتوحة من غير توكن. لو بعتنا `lat`/`lng` السيرفر بيرتّب بالأقرب
     * وبيضيف `distance_km`؛ من غيرهم بيرجّع القايمة بترتيب اللوحة —
     * وده اللي بيظهر للمستخدم اللي رفض إذن الموقع.
     */

    @GET("branches")
    suspend fun branches(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radius") radiusKm: Double? = null,
        @Query("limit") limit: Int? = null
    ): DataEnvelope<List<BranchDto>>

    @GET("branches/nearest")
    suspend fun nearestBranch(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): BranchNearestEnvelope

    @GET("branches/{id}")
    suspend fun branch(@Path("id") id: Long): DataEnvelope<BranchDto>

    /* ── المتجر ─────────────────────────────────────────────────────
     *
     * المنتجات مفتوحة من غير توكن — العميل بيتصفّح ويشوف الأسعار قبل
     * ما يسجّل. السلة والطلبات محتاجة توكن (`AuthInterceptor` بيضيفه).
     *
     * **السعر بييجي محسوباً من السيرفر.** مفيش أي مسار هنا بياخد سعر
     * من التطبيق، وده مقصود: التطبيق المعدّل اللي بيبعت سعر بتاعه هو
     * أسهل طريقة لسرقة المحل.
     */

    @GET("products")
    suspend fun products(
        @Query("kind") kind: String? = null,
        @Query("metal") metal: String? = null,
        @Query("per_page") perPage: Int? = null
    ): ProductsResponse

    @GET("products/shop")
    suspend fun shopInfo(): DataEnvelope<ShopInfoDto>

    @GET("products/{id}")
    suspend fun product(@Path("id") id: Long): ProductResponse

    @GET("cart")
    suspend fun cart(): CartResponse

    @POST("cart")
    suspend fun addToCart(@Body body: AddToCartRequest): Response<CartResponse>

    @PUT("cart")
    suspend fun setCartQuantity(@Body body: SetCartQuantityRequest): Response<CartResponse>

    @DELETE("cart/items/{productId}")
    suspend fun removeFromCart(@Path("productId") productId: Long): CartResponse

    @DELETE("cart")
    suspend fun clearCart(): CartResponse

    @GET("orders")
    suspend fun orders(@Query("per_page") perPage: Int? = null): OrdersResponse

    @GET("orders/{id}")
    suspend fun order(@Path("id") id: Long): DataEnvelope<OrderDto>

    /*
     * بنرجّع `Response<T>` مش `T` عشان نقرا جسم الخطأ: السيرفر بيرفض
     * الطلب بـ 422 ورسالة مفهومة («السعر مش محدّث دلوقتي فمش ممكن
     * نثبّته»، «المنتج خلص من المخزون») — والمستخدم لازم يشوفها بدل
     * «حصل خطأ».
     */
    @POST("orders")
    suspend fun placeOrder(@Body body: PlaceOrderRequest): Response<OrderResponse>

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Long): Response<OrderResponse>

    companion object {
        const val BASE_URL = "https://backend.msagold.com/api/v1/"
    }
}
