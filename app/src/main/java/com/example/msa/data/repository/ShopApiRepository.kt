package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.AddToCartRequest
import com.msa.android.data.source.network.dto.CartDto
import com.msa.android.data.source.network.dto.OrderDto
import com.msa.android.data.source.network.dto.PlaceOrderRequest
import com.msa.android.data.source.network.dto.ProductDto
import com.msa.android.data.source.network.dto.SetCartQuantityRequest
import com.msa.android.data.source.network.dto.ShopInfoDto
import com.msa.android.data.source.network.dto.ShopPricesDto
import com.msa.android.domain.model.Branch
import com.msa.android.domain.model.Cart
import com.msa.android.domain.model.CartLine
import com.msa.android.domain.model.CartTotals
import com.msa.android.domain.model.Order
import com.msa.android.domain.model.OrderDeposit
import com.msa.android.domain.model.OrderLine
import com.msa.android.domain.model.PricingMode
import com.msa.android.domain.model.Product
import com.msa.android.domain.model.ProductKind
import com.msa.android.domain.model.ProductPrice
import com.msa.android.domain.model.ShopInfo
import com.msa.android.domain.model.ShopPrices
import com.msa.android.domain.repository.ShopRepository
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * المتجر من الـ API.
 *
 * ## مفيش كاش للأسعار
 *
 * `BranchApiRepository` بيكاش عشرة دقايق لأن الفروع مبتتغيّرش. هنا
 * العكس: السعر بيتغيّر مع السوق، والكاش معناه إن العميل يشوف رقم
 * وبيطلب برقم تاني. السيرفر نفسه بيكاش ٤٥ ثانية، وده كفاية.
 *
 * ## الأخطاء بتوصل للمستخدم بنصّها
 *
 * السيرفر بيرفض بـ 422 ورسالة مفهومة بالعربي: «السعر مش محدّث دلوقتي
 * فمش ممكن نثبّته»، «المنتج ده خلص من المخزون». لو حوّلناها لـ «حصل
 * خطأ» المستخدم مش هيعرف يعمل إيه — فبنفكّ `message` من جسم الرد.
 */
@Singleton
class ShopApiRepository @Inject constructor(
    private val api: MsaApi
) : ShopRepository {

    private companion object { const val TAG = "ShopRepo" }

    override suspend fun products(kind: ProductKind?): Result<Pair<List<Product>, ShopPrices>> =
        runCatching {
            val response = api.products(kind = kind?.apiValue, perPage = 50)
            response.data.map { it.toDomain() } to response.prices.toDomain()
        }.onFailure { Log.w(TAG, "products failed", it) }

    override suspend fun product(id: Long): Result<Product> =
        runCatching { api.product(id).data.toDomain() }
            .onFailure { Log.w(TAG, "product $id failed", it) }

    override suspend fun shopInfo(): Result<ShopInfo> =
        runCatching { api.shopInfo().data.toDomain() }
            .onFailure { Log.w(TAG, "shopInfo failed", it) }

    // ── السلة ──────────────────────────────────────────────────────

    override suspend fun cart(): Result<Cart> =
        runCatching { api.cart().data.toDomain() }
            .onFailure { Log.w(TAG, "cart failed", it) }

    override suspend fun addToCart(productId: Long, quantity: Int): Result<Cart> =
        unwrap { api.addToCart(AddToCartRequest(productId, quantity)) }
            .map { it.data.toDomain() }

    override suspend fun setQuantity(productId: Long, quantity: Int): Result<Cart> =
        unwrap { api.setCartQuantity(SetCartQuantityRequest(productId, quantity)) }
            .map { it.data.toDomain() }

    override suspend fun removeFromCart(productId: Long): Result<Cart> =
        runCatching { api.removeFromCart(productId).data.toDomain() }
            .onFailure { Log.w(TAG, "removeFromCart failed", it) }

    override suspend fun clearCart(): Result<Cart> =
        runCatching { api.clearCart().data.toDomain() }
            .onFailure { Log.w(TAG, "clearCart failed", it) }

    // ── الطلبات ────────────────────────────────────────────────────

    override suspend fun orders(): Result<List<Order>> =
        runCatching { api.orders(perPage = 30).data.map { it.toDomain() } }
            .onFailure { Log.w(TAG, "orders failed", it) }

    override suspend fun order(id: Long): Result<Order> =
        runCatching { api.order(id).data.toDomain() }
            .onFailure { Log.w(TAG, "order $id failed", it) }

    override suspend fun placeOrder(
        mode: PricingMode,
        branchId: Long?,
        customerName: String?,
        customerPhone: String?,
        note: String?
    ): Result<Order> = unwrap {
        api.placeOrder(
            PlaceOrderRequest(
                pricingMode = mode.apiValue,
                branchId = branchId,
                customerName = customerName?.takeIf { it.isNotBlank() },
                customerPhone = customerPhone?.takeIf { it.isNotBlank() },
                note = note?.takeIf { it.isNotBlank() }
            )
        )
    }.map { it.data.toDomain() }

    override suspend fun cancelOrder(id: Long): Result<Order> =
        unwrap { api.cancelOrder(id) }.map { it.data.toDomain() }

    // ── مساعدات ────────────────────────────────────────────────────

    /**
     * بيفك الرد وبيطلّع رسالة السيرفر لو فشل.
     *
     * لارافيل بيرجّع `{"message": "..."}` في 422 و403، وأحياناً
     * `{"errors": {"field": ["..."]}}` في أخطاء التحقق. بناخد أول
     * رسالة موجودة — أي واحدة فيهم أنفع من نص عام.
     */
    private inline fun <T> unwrap(call: () -> Response<T>): Result<T> = runCatching {
        val response = call()
        val body = response.body()

        if (response.isSuccessful && body != null) {
            return@runCatching body
        }

        throw ShopRequestException(
            message = response.errorBody()?.string().parseMessage()
                ?: "حصل خطأ، جرّب تاني",
            code = response.code()
        )
    }.onFailure { Log.w(TAG, "request failed", it) }

    private fun String?.parseMessage(): String? {
        if (this.isNullOrBlank()) return null

        return runCatching {
            val json = JSONObject(this)

            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optJSONObject("errors")
                    ?.let { errors ->
                        errors.keys().asSequence().firstOrNull()
                            ?.let { key -> errors.optJSONArray(key)?.optString(0) }
                    }
        }.getOrNull()
    }
}

/**
 * خطأ برسالة جاهزة للعرض — الشاشة بتعرض `message` زي ما هي.
 *
 * [code] لازم يتحمل معاها: `unwrap` بيحوّل **كل** رد فاشل للاستثناء
 * ده، فلو ما حملناش الكود كان الفرق بين «مش مسجّل دخول» (401 —
 * لازم نودّيه لشاشة الدخول) و«المخزون خلص» (422 — نعرض الرسالة)
 * هيضيع، والاتنين هيتعاملوا كرسالة نصية.
 */
class ShopRequestException(
    message: String,
    val code: Int = 0
) : RuntimeException(message) {

    val isUnauthorized: Boolean get() = code == 401
}

/* ── التحويل للنماذج ─────────────────────────────────────────────── */

private fun ProductDto.toDomain(): Product = Product(
    id = id,
    nameAr = name?.ar.orEmpty(),
    nameEn = name?.en.orEmpty(),
    descriptionAr = description?.ar,
    descriptionEn = description?.en,
    imageUrl = mediaUrl?.takeIf { it.isNotBlank() },
    // بنقع على الصورة الكاملة لو المصغّرة ناقصة — نفس ما السيرفر
    // بيعمل، بس الحماية هنا كمان عشان الكارت ما يبقاش فاضي
    thumbUrl = thumbUrl?.takeIf { it.isNotBlank() } ?: mediaUrl?.takeIf { it.isNotBlank() },
    kind = ProductKind.from(kind?.value),
    metal = metal,
    karat = karat,
    weightGrams = weightGrams,
    manufacturingPerGram = manufacturing?.perGram ?: 0.0,
    taxPercent = taxPercent,
    stock = stock,
    inStock = inStock,
    allowDeposit = allowDeposit,
    allowOnArrival = allowOnArrival,
    price = price?.let {
        ProductPrice(
            gramPrice = it.gramPrice,
            metalValue = it.metalValue,
            manufacturingTotal = it.manufacturingTotal,
            taxAmount = it.taxAmount,
            total = it.total,
            priced = it.priced
        )
    }
)

private fun ShopPricesDto?.toDomain(): ShopPrices = ShopPrices(
    goldGram21 = this?.goldGram21 ?: 0.0,
    silverGram999 = this?.silverGram999 ?: 0.0,
    isStale = this?.isStale ?: false
)

private fun ShopInfoDto.toDomain(): ShopInfo = ShopInfo(
    isEnabled = isEnabled,
    depositPercent = depositPercent,
    lockHours = lockHours,
    termsAr = terms?.ar,
    termsEn = terms?.en,
    depositInstructionsAr = depositInstructions?.ar,
    depositInstructionsEn = depositInstructions?.en,
    prices = prices.toDomain()
)

private fun CartDto.toDomain(): Cart = Cart(
    items = items.mapNotNull { line ->
        // منتج ناقص في الرد معناه بيانات متضاربة — بنتخطّى الصف بدل
        // ما الشاشة تعرض كارت من غير اسم ولا سعر
        line.product?.let {
            CartLine(
                id = line.id,
                quantity = line.quantity,
                product = it.toDomain(),
                lineTotal = line.lineTotal
            )
        }
    },
    count = count,
    totals = totals?.let {
        CartTotals(
            metalTotal = it.metalTotal,
            manufacturingTotal = it.manufacturingTotal,
            taxTotal = it.taxTotal,
            total = it.total,
            depositAmount = it.depositAmount,
            priced = it.priced
        )
    } ?: CartTotals(),
    prices = prices.toDomain()
)

private fun OrderDto.toDomain(): Order = Order(
    id = id,
    code = code,
    pricingMode = if (pricingMode?.value == PricingMode.ON_ARRIVAL.apiValue)
        PricingMode.ON_ARRIVAL else PricingMode.DEPOSIT,
    statusValue = status?.value ?: "pending",
    statusLabelAr = status?.label?.ar,
    statusLabelEn = status?.label?.en,
    deposit = OrderDeposit(
        status = deposit?.status ?: "none",
        labelAr = deposit?.label?.ar,
        labelEn = deposit?.label?.en,
        amount = deposit?.amount ?: 0.0
    ),
    metalTotal = totals?.metalTotal ?: 0.0,
    manufacturingTotal = totals?.manufacturingTotal ?: 0.0,
    taxTotal = totals?.taxTotal ?: 0.0,
    total = totals?.total ?: 0.0,
    isEstimate = totals?.isEstimate ?: false,
    priceLockedUntil = priceLockedUntil,
    priceLockExpired = priceLockExpired,
    branch = branch?.let {
        Branch(
            id = it.id,
            nameAr = it.nameAr.orEmpty(),
            nameEn = it.nameEn.orEmpty(),
            addressAr = it.addressAr,
            addressEn = it.addressEn,
            cityAr = it.cityAr,
            cityEn = it.cityEn,
            latitude = it.latitude,
            longitude = it.longitude,
            phone = it.phone,
            whatsapp = it.whatsapp,
            workingHoursAr = it.workingHoursAr,
            workingHoursEn = it.workingHoursEn,
            imageUrl = it.imageUrl,
            directionsUrl = it.directionsUrl,
            distanceKm = it.distanceKm
        )
    },
    items = items.map {
        OrderLine(
            id = it.id,
            nameAr = it.name?.ar.orEmpty(),
            nameEn = it.name?.en.orEmpty(),
            metal = it.metal,
            karat = it.karat,
            weightGrams = it.weightGrams,
            quantity = it.quantity,
            gramPrice = it.gramPrice,
            lineTotal = it.lineTotal
        )
    },
    note = note,
    canCancel = canCancel,
    createdAt = createdAt
)
