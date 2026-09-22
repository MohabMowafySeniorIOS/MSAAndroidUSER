package com.msa.android.domain.model

import java.util.Date

// ─── Indicators screen ────────────────────────────────────────────────────────

/** Which metal the indicators screen is currently showing. */
enum class MetalType2(val displayAr: String, val displayEn: String) {
    GOLD("الذهب", "Gold"),
    SILVER("الفضة", "Silver")
}

/** Time range options for the indicators charts. */
enum class TimePeriod(val displayAr: String, val displayEn: String, val days: Int) {
    H24     ("24 ساعة",  "24 hours", 1),
    WEEK    ("أسبوع",    "Week",     7),
    MONTH   ("شهر",      "Month",    30),
    MONTHS3 ("3 شهور",   "3 Months", 90),
    MONTHS6 ("6 شهور",   "6 Months", 180),
    MONTHS9 ("9 شهور",   "9 Months", 270),
    YEAR1   ("سنة",      "Year",     365),
    YEARS2  ("سنتان",    "2 Years",  730),
    YEARS3  ("3 سنوات",  "3 Years",  1095)
}

/** A single (timestamp, value) sample on a chart. */
data class PricePoint(
    val date: Date,
    val value: Double
)

/** A titled series of points rendered as one chart. */
data class ChartDataSet(
    val title: String,
    val points: List<PricePoint>
) {
    val minValue: Double get() = points.minOfOrNull { it.value } ?: 0.0
    val maxValue: Double get() = points.maxOfOrNull { it.value } ?: 0.0
}

// ─── Technical Analysis screen ────────────────────────────────────────────────

/** Period for the التحليل الفني / الإرشادات الفنية screen. */
enum class TAPeriod(val displayAr: String, val displayEn: String) {
    YESTERDAY("أمس",   "Yesterday"),
    WEEK     ("أسبوع",  "Week"),
    MONTH    ("شهر",   "Month"),
    YEAR     ("سنة",    "Year")
}

/** Open/close pair, mirrors iOS PriceSnapshot. */
data class PriceSnapshot(
    val open: Double,
    val close: Double
) {
    val change: Double    get() = close - open
    val changePct: Double get() = if (open == 0.0) 0.0 else (close - open) / open * 100.0
    val isPositive: Boolean get() = change >= 0.0
}

/** Full payload for one TA screen render. */
data class TechnicalAnalysisData(
    val period: TAPeriod,
    val startDate: Date,
    val endDate: Date,

    val gold21:     PriceSnapshot,
    val gold24:     PriceSnapshot,
    val goldUSD:    PriceSnapshot,
    val jewelryUSD: PriceSnapshot,
    val silver:     PriceSnapshot,
    val bankUSD:    PriceSnapshot,
    val priceGap:   PriceSnapshot,

    val highPrice: Double,
    val lowPrice:  Double
) {
    val goldMarketUp:   Boolean get() = gold24.isPositive
    val dollarMarketUp: Boolean get() = bankUSD.isPositive
}
