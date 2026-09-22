package com.msa.android.domain.repository

import com.msa.android.domain.model.*
import kotlinx.coroutines.flow.Flow

/** Realtime gold + silver prices from `metals` collection. */
interface MetalsRepository {
    fun observeMetals(): Flow<List<Metal>>
    fun observeOuncePrice(): Flow<OuncePrice>
    fun observeLastUpdated(): Flow<Long?>
}

/** Realtime bank rates collection. */
interface BanksRepository {
    /**
     * Observe bank rates for a given currency. iOS layout:
     *   `currencies/{currency}/banks` — each doc has buy/sell/logo/trend.
     * Default `USD` matches iOS `selectedCurrency = "USD"`.
     */
    fun observeBanks(currency: String = "USD"): Flow<List<BankRate>>
    /** Central bank dollar value used by Home calculations. */
    fun observeCentralBankDollar(): Flow<Double>
}

interface FaqRepository {
    fun observeFaqs(lang: String): Flow<List<FaqItem>>
}

interface PagesRepository {
    fun observePage(type: PageType): Flow<PageContent?>
}

interface ContactRepository {
    suspend fun send(message: ContactMessage): Result<Unit>
}

interface OnBoardingRepository {
    fun observePages(): Flow<List<OnBoardingPage>>
}

interface VersionRepository {
    fun observeVersion(): Flow<AppVersionInfo>

    /**
     * Live per-screen maintenance flags from the same `appVersion/appVersion`
     * doc. Separate from [observeVersion] so a screen can be taken down without
     * blocking the whole app.
     */
    fun observeScreenMaintenance(): Flow<ScreenMaintenance>
}

/**
 * 1:1 port of iOS bullionsScreenView Firestore calls.
 *
 *   Billions/{metalId}                                       ← name_ar, name_en
 *   Billions/{metalId}/{metalId}/{anyDoc}                    ← CompanyName: [String]
 *   Billions/{metalId}/{metalId}/{metalId}/{company}/{anyDoc}
 *      ← gram: [{name_ar, name_en, count, cashBack, Manufacturing}, ...]
 */
interface BullionRepository {
    fun observeMetals(): Flow<List<BullionMetalType>>
    fun observeCompanies(metalId: String): Flow<List<String>>
    fun observeGrams(metalId: String, company: String): Flow<List<BullionGramItem>>
}

/**
 * News articles. Mirrors iOS AllNewsVC.getNews():
 *   Firestore.firestore()
 *     .collection("news")
 *     .order(by: "publishedAt", descending: true)
 *     .addSnapshotListener { ... }
 */
interface NewsRepository {
    fun observeNews(): Flow<List<NewsItem>>
    fun observeMsaNews(): Flow<List<NewsItem>>
}

/**
 * التحقّق من كود QR على سبيكة.
 *
 * بيضرب على الباك اند بتاعنا (`POST bullion-codes/verify`) مش على
 * Firestore: الأكواد بتتولّد وبتتطبع وبتتلغى من لوحة التحكم، وكل مسح
 * بيتسجّل هناك.
 *
 * الرمي معناه **شبكة أو سيرفر** — مش «الكود غلط». الكود الغلط بيرجع
 * كنتيجة عادية بـ`Status.UNKNOWN`.
 */
interface QRRepository {
    suspend fun verify(code: String): com.msa.android.domain.model.BullionVerification
}

/**
 * The user's portfolio of physical-asset holdings (gold bars, pounds, silver).
 * Persisted locally — no server backing yet — so all CRUD is suspending against
 * a DataStore-backed store. Live updates flow through [observeItems].
 */
interface PortfolioRepository {
    fun observeItems(): kotlinx.coroutines.flow.Flow<List<com.msa.android.domain.model.PortfolioItem>>
    suspend fun addItem(item: com.msa.android.domain.model.PortfolioItem)
    suspend fun updateItem(item: com.msa.android.domain.model.PortfolioItem)
    suspend fun removeItem(id: String)
}

/**
 * اجتماعات وقرارات الفيدرالي الأمريكي (FOMC).
 *
 * دوال معلّقة بترجّع [Result] مش `Flow` زي باقي الريبوزيتوريز هنا: دي
 * بيانات من REST مش من Firestore، فمفيش stream أصلاً — وبرضه الشاشة
 * محتاجة تفرّق بين "بيحمّل" و"فشل" عشان تعرض زرار إعادة محاولة، وده
 * `Flow<List<T>>` مش بيوصّله.
 */
interface FomcRepository {
    /** كل الاجتماعات مرتّبة من الأحدث للأقدم */
    suspend fun events(forceRefresh: Boolean = false): Result<List<com.msa.android.domain.model.FomcEvent>>

    /** الاجتماع القادم — `null` يعني مفيش اجتماع معلن قدّام */
    suspend fun next(): Result<com.msa.android.domain.model.FomcEvent?>

    suspend fun event(id: Long): Result<com.msa.android.domain.model.FomcEvent?>

    /** بيتنادى مع السحب للتحديث */
    suspend fun invalidate()
}

/**
 * التقويم الاقتصادي.
 *
 * منفصل عن [FomcRepository] عن قصد: اجتماع الفيدرالي وحدث التقويم
 * كيانين مختلفين في الشكل والمصدر ودورة التحديث. إنهم بيتعرضوا في
 * نفس الشاشة قرار واجهة، مش سبب لدمج البيانات.
 */
interface CalendarRepository {

    /**
     * أحداث مدى زمني، مجمّعة بالتاريخ (التجميع جاي من السيرفر).
     *
     * `impacts` و`currencies` فاضيين = من غير فلتر.
     */
    suspend fun days(
        from: java.time.LocalDate,
        to: java.time.LocalDate,
        impacts: Set<com.msa.android.domain.model.EventImpact> = emptySet(),
        currencies: Set<String> = emptySet(),
        forceRefresh: Boolean = false
    ): Result<List<com.msa.android.domain.model.CalendarDay>>

    /** الحدث + الإصدار الجاي لنفس المؤشر */
    suspend fun details(id: Long): Result<com.msa.android.domain.model.EventDetails?>

    /** سلسلة المؤشر — الرسم البياني وتبويب التاريخ */
    suspend fun history(id: Long, limit: Int = 24): Result<com.msa.android.domain.model.EventHistory>

    /** العملات الموجودة فعلاً — عشان الفلتر ما يبقاش قايمة ثابتة في التطبيق */
    suspend fun currencies(): Result<List<com.msa.android.domain.model.CalendarCurrency>>

    suspend fun invalidate()
}

/**
 * فروع الشركة.
 *
 * الترتيب بالأقرب بيحصل في السيرفر — التطبيق بيبعت إحداثيات المستخدم
 * بس. كده منطق المسافة واحد بين iOS وأندرويد ومبيتكتبش مرتين.
 */
interface BranchRepository {
    /**
     * @param lat/[lng] موقع المستخدم، أو `null` لو رفض الإذن —
     *   وقتها السيرفر بيرجّع القايمة بترتيب اللوحة بدل ما تفضى.
     */
    suspend fun branches(
        lat: Double? = null,
        lng: Double? = null
    ): Result<List<com.msa.android.domain.model.Branch>>

    suspend fun branch(id: Long): Result<com.msa.android.domain.model.Branch?>

    suspend fun invalidate()
}

/**
 * المتجر: المنتجات والسلة والطلبات.
 *
 * **مفيش دالة بتاخد سعر.** السعر بييجي محسوباً من السيرفر في
 * [com.msa.android.domain.model.ProductPrice] — التطبيق بيعرضه بس.
 * ده مقصود: المحفظة بتبعت الأسعار مع الطلب لأنها عرض بس، لكن هنا
 * فيه فلوس بتتحرك، وتطبيق معدّل بيبعت سعر بتاعه هو أسهل طريقة لسرقة
 * المحل.
 */
interface ShopRepository {
    /** @return المنتجات + أسعار الجرام اللي السيرفر حسب بيها */
    suspend fun products(
        kind: com.msa.android.domain.model.ProductKind? = null
    ): Result<Pair<List<com.msa.android.domain.model.Product>, com.msa.android.domain.model.ShopPrices>>

    suspend fun product(id: Long): Result<com.msa.android.domain.model.Product>

    /** إعدادات المتجر من اللوحة — نسبة العربون والشروط */
    suspend fun shopInfo(): Result<com.msa.android.domain.model.ShopInfo>

    /* ── السلة ─────────────────────────────────────────────────────
     * كل دالة بترجّع السلة كاملة بالإجماليات، فالشاشة بتتحدّث من نفس
     * الرد من غير نداء تاني.
     */

    suspend fun cart(): Result<com.msa.android.domain.model.Cart>
    suspend fun addToCart(productId: Long, quantity: Int = 1): Result<com.msa.android.domain.model.Cart>
    suspend fun setQuantity(productId: Long, quantity: Int): Result<com.msa.android.domain.model.Cart>
    suspend fun removeFromCart(productId: Long): Result<com.msa.android.domain.model.Cart>
    suspend fun clearCart(): Result<com.msa.android.domain.model.Cart>

    /* ── الطلبات ───────────────────────────────────────────────── */

    suspend fun orders(): Result<List<com.msa.android.domain.model.Order>>
    suspend fun order(id: Long): Result<com.msa.android.domain.model.Order>

    /**
     * الفشل هنا بيرجع بـ **رسالة السيرفر** مش نص عام: «السعر مش محدّث
     * دلوقتي فمش ممكن نثبّته» و«المنتج خلص من المخزون» رسايل المستخدم
     * محتاج يقراها عشان يعرف يعمل إيه.
     */
    suspend fun placeOrder(
        mode: com.msa.android.domain.model.PricingMode,
        branchId: Long? = null,
        customerName: String? = null,
        customerPhone: String? = null,
        note: String? = null
    ): Result<com.msa.android.domain.model.Order>

    suspend fun cancelOrder(id: Long): Result<com.msa.android.domain.model.Order>
}

/**
 * توكن الجهاز عند السيرفر.
 *
 * موجود عشان الإشعارات الموجّهة (اللي بتروح على التوكن مش على توبيك)
 * توصل فعلاً. التوكن كان بيتبعت مع التسجيل والدخول بس، وFCM بيجدّده
 * لوحده — فمستخدم مش بيعمل دخول كل شوية كان التوكن بتاعه عندنا بيبوظ
 * ومحدش واخد باله.
 */
interface DeviceRepository {

    /**
     * بيقرا توكن FCM الحالي واللغة وبيبعتهم للسيرفر.
     *
     * بيرجع `false` من غير ما يرمي لو المستخدم مش مسجّل دخول أو الشبكة
     * وقعت — النداء ده بيحصل في خلفية حاجات تانية (فتح التطبيق · تغيير
     * اللغة)، وما ينفعش يوقّف أي واحدة منهم.
     */
    suspend fun syncToken(): Boolean
}
