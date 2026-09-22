package com.msa.android.data.source.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PortfolioItemDto(
    val id: Long,
    val metal: String,
    val karat: String,
    @Json(name = "item_type") val itemType: String,
    @Json(name = "purchase_date") val purchaseDate: String? = null,
    val weight: Double = 0.0,
    @Json(name = "gram_price") val gramPrice: Double = 0.0,
    @Json(name = "manufacturing_per_gram") val manufacturingPerGram: Double = 0.0,
    @Json(name = "cashback_per_gram") val cashbackPerGram: Double = 0.0,
    @Json(name = "total_paid") val totalPaid: Double = 0.0,
    val note: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "current_value") val currentValue: Double = 0.0,
    @Json(name = "shop_sell_value") val shopSellValue: Double = 0.0,
    val profit: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class MetalBreakdownDto(
    val metal: String,
    @Json(name = "current_value") val currentValue: Double = 0.0,
    @Json(name = "total_paid") val totalPaid: Double = 0.0,
    @Json(name = "total_weight") val totalWeight: Double = 0.0,
    @Json(name = "items_count") val itemsCount: Int = 0,
    val profit: Double = 0.0,
    @Json(name = "profit_percent") val profitPercent: Double = 0.0,
    @Json(name = "avg_buy_price") val avgBuyPrice: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class PortfolioSummaryDto(
    @Json(name = "current_value") val currentValue: Double = 0.0,
    @Json(name = "total_paid") val totalPaid: Double = 0.0,
    @Json(name = "total_weight") val totalWeight: Double = 0.0,
    @Json(name = "cashback_total") val cashbackTotal: Double = 0.0,
    @Json(name = "shop_sell_value") val shopSellValue: Double = 0.0,
    val profit: Double = 0.0,
    @Json(name = "profit_percent") val profitPercent: Double = 0.0,
    @Json(name = "items_count") val itemsCount: Int = 0,
    @Json(name = "by_metal") val byMetal: List<MetalBreakdownDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PortfolioPayloadDto(
    val summary: PortfolioSummaryDto,
    val items: List<PortfolioItemDto> = emptyList()
)

/* ── البروفايل ─────────────────────────────────────────────────── */

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val locale: String? = null
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)
