package com.msa.android.domain.model

/**
 * منتج للبيع — سبيكة أو مشغولة.
 *
 * **[price] بييجي محسوباً من السيرفر.** ممكن نحسبه هنا من سعر الجرام
 * اللي في الرئيسية، بس ساعتها كل جهاز بيحسب لوحده والرقم ممكن يخالف
 * اللي السيرفر بيسجّله في الطلب. السيرفر بيقرا نفس مستندات Firestore،
 * فالرقم واحد في الحالتين.
 */
data class Product(
    val id: Long,

    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String?,
    val descriptionEn: String?,

    val imageUrl: String?,
    val thumbUrl: String?,

    val kind: ProductKind,
    val metal: String,
    val karat: Int,

    val weightGrams: Double,
    val manufacturingPerGram: Double,
    val taxPercent: Double,

    /** `null` = مخزون مفتوح، صفر = خلص. الفرق مقصود */
    val stock: Int?,
    val inStock: Boolean,

    val allowDeposit: Boolean,
    val allowOnArrival: Boolean,

    val price: ProductPrice?
) {
    fun name(arabic: Boolean): String =
        (if (arabic) nameAr else nameEn).ifBlank { if (arabic) nameEn else nameAr }

    fun description(arabic: Boolean): String? =
        (if (arabic) descriptionAr else descriptionEn)?.takeIf { it.isNotBlank() }
            ?: (if (arabic) descriptionEn else descriptionAr)?.takeIf { it.isNotBlank() }

    val isGold: Boolean get() = metal != "silver"

    /** فيه سعر جاهز للعرض؟ غير كده الشاشة بتعرض «جاري التحميل» */
    val hasPrice: Boolean get() = price?.priced == true
}

enum class ProductKind(val apiValue: String) {
    BULLION("bullion"),        // سبائك
    JEWELLERY("jewellery");    // مشغولات

    companion object {
        fun from(value: String?): ProductKind =
            if (value == "jewellery") JEWELLERY else BULLION
    }
}

data class ProductPrice(
    val gramPrice: Double,
    val metalValue: Double,
    val manufacturingTotal: Double,
    val taxAmount: Double,
    val total: Double,

    /**
     * `false` معناه السعر لسه ما وصلش من Firestore.
     * الشاشة بتعرض «جاري التحميل» مش صفر جنيه.
     */
    val priced: Boolean
)

/** أسعار الجرام الحالية زي ما السيرفر قراها */
data class ShopPrices(
    val goldGram21: Double = 0.0,
    val silverGram999: Double = 0.0,

    /** السعر من آخر نسخة محفوظة — Firestore مش راد */
    val isStale: Boolean = false
)

/** إعدادات المتجر من اللوحة — بتتغيّر من غير تحديث للتطبيق */
data class ShopInfo(
    val isEnabled: Boolean = true,
    val depositPercent: Double = 0.0,
    val lockHours: Int = 24,
    val termsAr: String? = null,
    val termsEn: String? = null,
    val depositInstructionsAr: String? = null,
    val depositInstructionsEn: String? = null,
    val prices: ShopPrices = ShopPrices()
) {
    fun terms(arabic: Boolean): String? =
        (if (arabic) termsAr else termsEn)?.takeIf { it.isNotBlank() }

    fun depositInstructions(arabic: Boolean): String? =
        (if (arabic) depositInstructionsAr else depositInstructionsEn)?.takeIf { it.isNotBlank() }
}

/* ── السلة ─────────────────────────────────────────────────────────── */

data class Cart(
    val items: List<CartLine> = emptyList(),
    val count: Int = 0,
    val totals: CartTotals = CartTotals(),
    val prices: ShopPrices = ShopPrices()
) {
    val isEmpty: Boolean get() = items.isEmpty()
}

data class CartLine(
    val id: Long,
    val quantity: Int,
    val product: Product,
    val lineTotal: Double
)

data class CartTotals(
    val metalTotal: Double = 0.0,
    val manufacturingTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val total: Double = 0.0,
    val depositAmount: Double = 0.0,
    val priced: Boolean = false
)

/* ── الطلبات ───────────────────────────────────────────────────────── */

enum class PricingMode(val apiValue: String) {
    /** بعربون — السعر بيتثبّت وقت الطلب */
    DEPOSIT("deposit"),

    /** من غير عربون — السعر بيتحسب وقت الاستلام من الفرع */
    ON_ARRIVAL("on_arrival")
}

data class Order(
    val id: Long,
    val code: String,

    val pricingMode: PricingMode,
    val statusValue: String,
    val statusLabelAr: String?,
    val statusLabelEn: String?,

    val deposit: OrderDeposit,

    val metalTotal: Double,
    val manufacturingTotal: Double,
    val taxTotal: Double,
    val total: Double,

    /** الإجمالي تقديري — طلبات «التسعير عند الاستلام» */
    val isEstimate: Boolean,

    val priceLockedUntil: String?,
    val priceLockExpired: Boolean,

    val branch: Branch?,
    val items: List<OrderLine>,

    val note: String?,
    val canCancel: Boolean,
    val createdAt: String?
) {
    fun statusLabel(arabic: Boolean): String =
        ((if (arabic) statusLabelAr else statusLabelEn) ?: statusValue)
}

data class OrderDeposit(
    val status: String,
    val labelAr: String?,
    val labelEn: String?,
    val amount: Double
) {
    fun label(arabic: Boolean): String =
        ((if (arabic) labelAr else labelEn) ?: status)

    val isPending: Boolean get() = status == "pending"
    val isPaid: Boolean get() = status == "paid"
    val exists: Boolean get() = status != "none"
}

data class OrderLine(
    val id: Long,
    val nameAr: String,
    val nameEn: String,
    val metal: String,
    val karat: Int,
    val weightGrams: Double,
    val quantity: Int,
    val gramPrice: Double,
    val lineTotal: Double
) {
    fun name(arabic: Boolean): String =
        (if (arabic) nameAr else nameEn).ifBlank { if (arabic) nameEn else nameAr }
}
