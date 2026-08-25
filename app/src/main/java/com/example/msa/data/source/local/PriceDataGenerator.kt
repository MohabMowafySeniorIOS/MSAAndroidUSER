package com.msa.android.data.source.local

import com.msa.android.domain.model.ChartDataSet
import com.msa.android.domain.model.MetalType2
import com.msa.android.domain.model.PricePoint
import com.msa.android.domain.model.TimePeriod
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 1:1 port of iOS `PriceDataManager.generateMilestonePoints` strategy.
 *
 * For each chart we declare a few `(year, value)` anchor points based on real
 * historical prices, then linearly interpolate between them. A tiny seeded
 * pseudo-random noise term is layered on top so the resulting series looks
 * like a real time-series, not a straight line.
 *
 * The result is deterministic for a given (symbol, period) pair — good enough
 * to look like the eDahab charts without needing any backend.
 */
@Singleton
class PriceDataGenerator @Inject constructor() {

    /**
     * Returns all charts a screen needs for the given metal + period.
     *
     *   GOLD   → 4 charts: gold21 EGP, gold24 EGP, ounce USD, USD/EGP
     *   SILVER → 3 charts: silver 999 EGP, silver ounce USD, USD/EGP
     */
    fun chartsFor(metal: MetalType2, period: TimePeriod): List<ChartDataSet> = when (metal) {
        MetalType2.GOLD -> listOf(
            ChartDataSet("سعر جرام الذهب عيار 21 في مصر بالجنيه", goldEgpPoints(karat = 21, period = period)),
            ChartDataSet("سعر جرام الذهب عيار 24 في مصر بالجنيه", goldEgpPoints(karat = 24, period = period)),
            ChartDataSet("سعر الأونصة عيار 24 عالميا بالدولار",   goldUsdPoints(period)),
            ChartDataSet("سعر الدولار مقابل الجنيه",               usdEgpPoints(period))
        )
        MetalType2.SILVER -> listOf(
            ChartDataSet("سعر جرام الفضة عيار 999 في مصر بالجنيه", silverEgpPoints(period)),
            ChartDataSet("سعر أونصة الفضة عالميا بالدولار",         silverUsdPoints(period)),
            ChartDataSet("سعر الدولار مقابل الجنيه",                 usdEgpPoints(period))
        )
    }

    // ── Anchor data — matches iOS milestone tables exactly ──────────────────

    private fun goldEgpPoints(karat: Int, period: TimePeriod): List<PricePoint> {
        val m = if (karat == 24) 1.0 else 21.0 / 24.0
        return milestonePoints(
            anchors = listOf(
                2000 to (115.0  * m), 2005 to (180.0  * m), 2008 to (320.0  * m),
                2011 to (500.0  * m), 2013 to (420.0  * m), 2016 to (700.0  * m),
                2019 to (950.0  * m), 2020 to (1600.0 * m), 2021 to (1400.0 * m),
                2022 to (1800.0 * m), 2023 to (3200.0 * m), 2024 to (5500.0 * m),
                2025 to (7543.0 * m)
            ),
            period = period,
            noisePercent = 0.015
        )
    }

    private fun goldUsdPoints(period: TimePeriod): List<PricePoint> =
        milestonePoints(
            anchors = listOf(
                2000 to 273.0,  2003 to 420.0,  2006 to 620.0,
                2008 to 900.0,  2010 to 1400.0, 2011 to 1900.0,
                2013 to 1200.0, 2016 to 1150.0, 2019 to 1500.0,
                2020 to 2060.0, 2022 to 1800.0, 2023 to 2000.0,
                2024 to 2600.0, 2025 to 3350.0
            ),
            period = period,
            noisePercent = 0.012
        )

    private fun silverEgpPoints(period: TimePeriod): List<PricePoint> =
        milestonePoints(
            anchors = listOf(
                2000 to 1.5,   2005 to 3.0,   2008 to 10.0,
                2011 to 55.0,  2013 to 30.0,  2016 to 50.0,
                2019 to 70.0,  2020 to 110.0, 2022 to 100.0,
                2023 to 180.0, 2024 to 280.0, 2025 to 129.0
            ),
            period = period,
            noisePercent = 0.025
        )

    private fun silverUsdPoints(period: TimePeriod): List<PricePoint> =
        milestonePoints(
            anchors = listOf(
                2000 to 5.0,  2004 to 6.5,  2007 to 13.0,
                2011 to 49.0, 2013 to 20.0, 2016 to 17.0,
                2019 to 18.0, 2020 to 29.0, 2022 to 22.0,
                2023 to 25.0, 2024 to 32.0, 2025 to 33.0
            ),
            period = period,
            noisePercent = 0.022
        )

    private fun usdEgpPoints(period: TimePeriod): List<PricePoint> =
        milestonePoints(
            anchors = listOf(
                2000 to 3.5,   2003 to 6.2,   2005 to 5.8,
                2010 to 5.6,   2013 to 7.0,   2016 to 8.8,
                2017 to 17.5,  2019 to 16.8,  2020 to 16.0,
                2022 to 18.0,  2023 to 30.9,  2024 to 48.0,
                2025 to 51.99
            ),
            period = period,
            noisePercent = 0.003
        )

    // ── Interpolation + step sizing ─────────────────────────────────────────

    private fun milestonePoints(
        anchors: List<Pair<Int, Double>>,
        period: TimePeriod,
        noisePercent: Double
    ): List<PricePoint> {
        val cal = Calendar.getInstance()
        val end = Date()
        val startCal = (cal.clone() as Calendar).apply {
            time = end
            add(Calendar.DAY_OF_YEAR, -period.days)
        }
        val start = startCal.time

        // Anchor → Date pairs, plus a final anchor pinned at `end` using the latest value.
        val anchorDates: List<Pair<Date, Double>> = buildList {
            anchors.forEach { (year, value) ->
                val c = Calendar.getInstance().apply {
                    clear(); set(year, Calendar.JANUARY, 1)
                }
                add(c.time to value)
            }
            add(end to anchors.last().second)
        }

        val stepHours = when (period) {
            TimePeriod.H24     -> 1
            TimePeriod.WEEK    -> 4
            TimePeriod.MONTH   -> 12
            TimePeriod.MONTHS3 -> 24
            TimePeriod.MONTHS6 -> 48
            TimePeriod.MONTHS9 -> 72
            TimePeriod.YEAR1   -> 168    // weekly
            TimePeriod.YEARS2  -> 336
            TimePeriod.YEARS3  -> 504
        }

        val result = mutableListOf<PricePoint>()
        var seed = 42L
        var c = (cal.clone() as Calendar).apply { time = start }
        while (!c.time.after(end)) {
            val base = interpolate(anchorDates, c.time)
            val r = pseudoRandom(seed).also { seed = it.first }.second
            val noise = base * noisePercent * r
            val v = (base + noise).coerceAtLeast(0.01)
            result.add(PricePoint(date = c.time, value = roundTo2(v)))
            c.add(Calendar.HOUR_OF_DAY, stepHours)
        }
        return result
    }

    private fun interpolate(anchors: List<Pair<Date, Double>>, at: Date): Double {
        if (anchors.size < 2) return anchors.firstOrNull()?.second ?: 0.0
        for (i in 0 until anchors.size - 1) {
            val (aDate, aVal) = anchors[i]
            val (bDate, bVal) = anchors[i + 1]
            if (!at.before(aDate) && !at.after(bDate)) {
                val total = (bDate.time - aDate.time).toDouble()
                val elapsed = (at.time - aDate.time).toDouble()
                val t = if (total > 0) elapsed / total else 0.0
                return aVal + (bVal - aVal) * t
            }
        }
        return anchors.last().second
    }

    /**
     * Linear-congruential pseudo-random: same algorithm as iOS port.
     * Returns (newSeed, value in -1.0..1.0).
     */
    private fun pseudoRandom(seed: Long): Pair<Long, Double> {
        val newSeed = seed * 6364136223846793005L + 1442695040888963407L
        val v = ((newSeed ushr 33).toDouble()) / UInt.MAX_VALUE.toLong().toDouble()
        return newSeed to (v - 0.5) * 2.0
    }

    private fun roundTo2(v: Double): Double = kotlin.math.round(v * 100.0) / 100.0
}
