package com.msa.android.domain.model

/** نوع القطعة — نفس قيم الباك اند */
enum class ItemType(val apiValue: String) {
    BULLION("bullion"),
    COIN("coin"),
    JEWELLERY("jewellery"),
    SCRAP("scrap");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.apiValue == value } ?: BULLION
    }
}

enum class PortfolioMetal(val apiValue: String) {
    GOLD("gold"),
    SILVER("silver");

    /** العيارات المتاحة لكل معدن — الفضة بمقياس الألف مش بالعيار */
    val karats: List<String>
        get() = if (this == GOLD) listOf("24", "22", "21", "18", "14")
                else listOf("999", "925", "800")

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.apiValue == value } ?: GOLD
    }
}

/**
 * قطعة في المحفظة.
 *
 * المصنعية والكاش باك مخزّنين **لكل جرام** مش للقطعة كلها — كده تعديل
 * الوزن بعدين بيدي أرقام صح من غير ما المستخدم يعيد الحساب بنفسه.
 */
data class PortfolioEntry(
    val id: Long,
    val metal: PortfolioMetal,
    val karat: String,
    val type: ItemType,
    val purchaseDate: String?,
    val weight: Double,
    val gramPrice: Double,
    val manufacturingPerGram: Double,
    val cashbackPerGram: Double,
    val totalPaid: Double,
    val note: String?,
    val imageUrl: String?,
    // محسوبة من السيرفر بالسعر الحالي
    val currentValue: Double,
    val shopSellValue: Double,
    val profit: Double
) {
    val profitPercent: Double
        get() = if (totalPaid > 0) profit / totalPaid * 100.0 else 0.0

    val isProfit: Boolean get() = profit >= 0
}

/** ملخّص لمعدن واحد */
data class MetalBreakdown(
    val metal: PortfolioMetal,
    val currentValue: Double,
    val totalPaid: Double,
    val totalWeight: Double,
    val itemsCount: Int,
    val profit: Double,
    val profitPercent: Double,
    val avgBuyPrice: Double
)

data class PortfolioTotals(
    val currentValue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalWeight: Double = 0.0,
    val cashbackTotal: Double = 0.0,
    val shopSellValue: Double = 0.0,
    val profit: Double = 0.0,
    val profitPercent: Double = 0.0,
    val itemsCount: Int = 0,
    val byMetal: List<MetalBreakdown> = emptyList()
) {
    val isProfit: Boolean get() = profit >= 0

    fun forMetal(metal: PortfolioMetal): MetalBreakdown? =
        byMetal.firstOrNull { it.metal == metal }
}
