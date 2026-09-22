package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* ── المنتجات ────────────────────────────────────────────────────────
 *
 * **السعر بييجي من السيرفر، مش بيتحسب هنا.**
 *
 * ممكن يبان أسهل إننا نضرب سعر الجرام اللي في الرئيسية × الوزن
 * محلياً، بس ساعتها كل جهاز بيحسب لوحده، والرقم اللي العميل بيشوفه
 * ممكن يخالف الرقم اللي السيرفر بيسجّله في الطلب. السيرفر بيقرا نفس
 * مستندات Firestore وبيرجّع `price` جاهز — إحنا بنعرضه وخلاص.
 *
 * `gram_price` بيرجع كمان عشان نعرضه تحت السعر («٧٤٨٥٫٧١ للجرام»)،
 * فالعميل يقدر يراجع الحساب بنفسه.
 */

@JsonClass(generateAdapter = true)
data class ProductDto(
    val id: Long,

    // اللغتين مع بعض — تبديل اللغة مبيحتاجش طلب شبكة جديد
    val name: LocalizedTextDto? = null,
    val description: LocalizedTextDto? = null,

    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "thumb_url") val thumbUrl: String? = null,

    val kind: LabeledValueDto? = null,
    val metal: String = "gold",
    val karat: Int = 21,

    @Json(name = "weight_grams") val weightGrams: Double = 0.0,

    val manufacturing: ManufacturingDto? = null,

    @Json(name = "tax_percent") val taxPercent: Double = 0.0,

    /** `null` = مخزون مفتوح. الشاشة بتخفي العدّاد في الحالة دي */
    val stock: Int? = null,
    @Json(name = "in_stock") val inStock: Boolean = true,

    @Json(name = "allow_deposit")    val allowDeposit: Boolean = true,
    @Json(name = "allow_on_arrival") val allowOnArrival: Boolean = true,

    val price: ProductPriceDto? = null,

    val order: Int = 0
)

@JsonClass(generateAdapter = true)
data class ManufacturingDto(
    val mode: String = "per_gram",
    val value: Double = 0.0,
    @Json(name = "per_gram") val perGram: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ProductPriceDto(
    @Json(name = "gram_price")          val gramPrice: Double = 0.0,
    @Json(name = "metal_value")         val metalValue: Double = 0.0,
    @Json(name = "manufacturing_total") val manufacturingTotal: Double = 0.0,
    @Json(name = "tax_amount")          val taxAmount: Double = 0.0,
    val total: Double = 0.0,
    val currency: String = "EGP",

    /**
     * `false` معناه السعر لسه ما وصلش من Firestore.
     *
     * الشاشة بتعرض «جاري التحميل» بدل صفر جنيه — نفس اللي الرئيسية
     * بتعمله لما المستند يبقى فاضي.
     */
    val priced: Boolean = false
)

/** `{ "value": "bullion", "label": { "ar": "سبائك", "en": "Bullion" } }` */
@JsonClass(generateAdapter = true)
data class LabeledValueDto(
    val value: String? = null,
    val label: LocalizedTextDto? = null
)

/** أسعار الجرام الحالية — بترجع مع كل رد فيه أسعار */
@JsonClass(generateAdapter = true)
data class ShopPricesDto(
    @Json(name = "gold_gram_21")    val goldGram21: Double = 0.0,
    @Json(name = "silver_gram_999") val silverGram999: Double = 0.0,
    @Json(name = "fetched_at")      val fetchedAt: String? = null,

    /** السعر جه من آخر نسخة محفوظة مش من Firestore مباشرةً */
    @Json(name = "is_stale") val isStale: Boolean = false
)

/**
 * `/products` — غلاف لارافيل المقسّم صفحات + `prices` إضافية.
 *
 * نفس شكل `/news` اللي التطبيق بيفكّه أصلاً، بس بحقل زيادة.
 */
@JsonClass(generateAdapter = true)
data class ProductsResponse(
    val data: List<ProductDto> = emptyList(),
    val meta: MetaDto? = null,
    val prices: ShopPricesDto? = null
)

@JsonClass(generateAdapter = true)
data class ProductResponse(
    val data: ProductDto,
    val prices: ShopPricesDto? = null
)

/* ── إعدادات المتجر ──────────────────────────────────────────────── */

@JsonClass(generateAdapter = true)
data class ShopInfoDto(
    @Json(name = "is_enabled")      val isEnabled: Boolean = true,
    @Json(name = "deposit_percent") val depositPercent: Double = 0.0,
    @Json(name = "min_deposit")     val minDeposit: Double = 0.0,
    @Json(name = "lock_hours")      val lockHours: Int = 24,
    @Json(name = "max_items_per_order") val maxItemsPerOrder: Int = 20,
    val terms: LocalizedTextDto? = null,
    @Json(name = "deposit_instructions") val depositInstructions: LocalizedTextDto? = null,
    val prices: ShopPricesDto? = null
)

/* ── السلة ───────────────────────────────────────────────────────────
 *
 * كل نداء على السلة بيرجّع السلة **كاملة** بالإجماليات، فبعد أي
 * تعديل الشاشة بتتحدّث من نفس الرد من غير طلب تاني.
 */

@JsonClass(generateAdapter = true)
data class CartResponse(
    val message: String? = null,
    val data: CartDto = CartDto()
)

@JsonClass(generateAdapter = true)
data class CartDto(
    val items: List<CartItemDto> = emptyList(),
    val count: Int = 0,
    val totals: CartTotalsDto? = null,
    val prices: ShopPricesDto? = null
)

@JsonClass(generateAdapter = true)
data class CartItemDto(
    val id: Long = 0,
    val quantity: Int = 1,
    val product: ProductDto? = null,
    @Json(name = "line_total") val lineTotal: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CartTotalsDto(
    @Json(name = "metal_total")         val metalTotal: Double = 0.0,
    @Json(name = "manufacturing_total") val manufacturingTotal: Double = 0.0,
    @Json(name = "tax_total")           val taxTotal: Double = 0.0,
    val total: Double = 0.0,
    @Json(name = "deposit_amount")      val depositAmount: Double = 0.0,
    val priced: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AddToCartRequest(
    @Json(name = "product_id") val productId: Long,
    val quantity: Int = 1
)

@JsonClass(generateAdapter = true)
data class SetCartQuantityRequest(
    @Json(name = "product_id") val productId: Long,
    /** صفر بيحذف الصف — مش خطأ */
    val quantity: Int
)

/* ── الطلبات ─────────────────────────────────────────────────────── */

@JsonClass(generateAdapter = true)
data class PlaceOrderRequest(
    @Json(name = "pricing_mode")   val pricingMode: String,
    @Json(name = "branch_id")      val branchId: Long? = null,
    @Json(name = "customer_name")  val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderResponse(
    val message: String? = null,
    val data: OrderDto
)

@JsonClass(generateAdapter = true)
data class OrdersResponse(
    val data: List<OrderDto> = emptyList(),
    val meta: MetaDto? = null
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: Long,
    val code: String = "",

    @Json(name = "pricing_mode") val pricingMode: LabeledValueDto? = null,
    val status: LabeledValueDto? = null,
    val deposit: OrderDepositDto? = null,

    @Json(name = "base_prices") val basePrices: OrderBasePricesDto? = null,
    val totals: OrderTotalsDto? = null,

    @Json(name = "price_locked_until") val priceLockedUntil: String? = null,
    @Json(name = "price_lock_expired") val priceLockExpired: Boolean = false,

    val branch: BranchDto? = null,
    val items: List<OrderItemDto> = emptyList(),

    @Json(name = "customer_name")  val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    val note: String? = null,

    @Json(name = "can_cancel") val canCancel: Boolean = false,

    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderDepositDto(
    val status: String = "none",
    val label: LocalizedTextDto? = null,
    val amount: Double = 0.0,
    val method: String? = null,
    @Json(name = "paid_at") val paidAt: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderBasePricesDto(
    val gold: Double = 0.0,
    val silver: Double = 0.0,
    @Json(name = "fetched_at") val fetchedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderTotalsDto(
    @Json(name = "metal_total")         val metalTotal: Double = 0.0,
    @Json(name = "manufacturing_total") val manufacturingTotal: Double = 0.0,
    @Json(name = "tax_total")           val taxTotal: Double = 0.0,
    val total: Double = 0.0,
    val currency: String = "EGP",

    /**
     * `true` في طلبات «التسعير عند الاستلام».
     *
     * لازم الشاشة تعرض التنبيه ده جنب الرقم — العميل اللي فاكر
     * الرقم نهائي هيتفاجئ في الفرع.
     */
    @Json(name = "is_estimate") val isEstimate: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    val id: Long = 0,
    @Json(name = "product_id") val productId: Long? = null,
    val name: LocalizedTextDto? = null,
    val kind: String = "bullion",
    val metal: String = "gold",
    val karat: Int = 21,
    @Json(name = "weight_grams") val weightGrams: Double = 0.0,
    val quantity: Int = 1,
    @Json(name = "gram_price") val gramPrice: Double = 0.0,
    @Json(name = "manufacturing_per_gram") val manufacturingPerGram: Double = 0.0,
    @Json(name = "metal_value")         val metalValue: Double = 0.0,
    @Json(name = "manufacturing_total") val manufacturingTotal: Double = 0.0,
    @Json(name = "tax_amount")          val taxAmount: Double = 0.0,
    @Json(name = "line_total")          val lineTotal: Double = 0.0
)
