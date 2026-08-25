package com.msa.android.data.source.local

import com.msa.android.domain.model.PriceSnapshot
import com.msa.android.domain.model.TAPeriod
import com.msa.android.domain.model.TechnicalAnalysisData
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Returns the hard-coded analysis data for a given period — exact numbers
 * come from iOS `TechnicalAnalysisProvider.buildData(for:)`.
 *
 * Same numbers, same screen layouts, so the Android version matches iOS
 * pixel-by-pixel in terms of values.
 */
@Singleton
class TechnicalAnalysisDataSource @Inject constructor() {

    fun load(period: TAPeriod): TechnicalAnalysisData {
        val now = Date()
        val cal = Calendar.getInstance()
        val (start, end) = when (period) {
            TAPeriod.YESTERDAY -> {
                val y = (cal.clone() as Calendar).apply {
                    time = now; add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val s = y.time
                val e = (y.clone() as Calendar).apply { add(Calendar.SECOND, 86399) }.time
                s to e
            }
            TAPeriod.WEEK  -> (cal.clone() as Calendar).apply { time = now; add(Calendar.DAY_OF_YEAR, -7) }.time to now
            TAPeriod.MONTH -> (cal.clone() as Calendar).apply { time = now; add(Calendar.MONTH, -1) }.time to now
            TAPeriod.YEAR  -> (cal.clone() as Calendar).apply { time = now; add(Calendar.YEAR, -1) }.time to now
        }

        return when (period) {
            TAPeriod.YESTERDAY -> TechnicalAnalysisData(
                period = period, startDate = start, endDate = end,
                gold21     = PriceSnapshot(6655.0,   6600.0),
                gold24     = PriceSnapshot(7606.0,   7543.0),
                goldUSD    = PriceSnapshot(4488.0,   4431.8),
                jewelryUSD = PriceSnapshot(52.71,    52.93),
                silver     = PriceSnapshot(131.0,    129.0),
                bankUSD    = PriceSnapshot(51.83,    51.88),
                priceGap   = PriceSnapshot(126.48,   150.02),
                highPrice = 7606.0, lowPrice = 7537.0
            )
            TAPeriod.WEEK -> TechnicalAnalysisData(
                period = period, startDate = start, endDate = end,
                gold21     = PriceSnapshot(6710.0,   6610.0),
                gold24     = PriceSnapshot(7669.0,   7554.0),
                goldUSD    = PriceSnapshot(4393.6,   4470.6),
                jewelryUSD = PriceSnapshot(54.28,    52.55),
                silver     = PriceSnapshot(132.0,    129.0),
                bankUSD    = PriceSnapshot(52.23,    51.88),
                priceGap   = PriceSnapshot(290.29,    96.29),
                highPrice = 7766.0, lowPrice = 7537.0
            )
            TAPeriod.MONTH -> TechnicalAnalysisData(
                period = period, startDate = start, endDate = end,
                gold21     = PriceSnapshot(6905.0,   6610.0),
                gold24     = PriceSnapshot(7891.0,   7554.0),
                goldUSD    = PriceSnapshot(4556.6,   4470.6),
                jewelryUSD = PriceSnapshot(53.86,    52.55),
                silver     = PriceSnapshot(127.0,    129.0),
                bankUSD    = PriceSnapshot(53.75,    51.88),
                priceGap   = PriceSnapshot(15.85,     96.29),
                highPrice = 8057.0, lowPrice = 7537.0
            )
            TAPeriod.YEAR -> TechnicalAnalysisData(
                period = period, startDate = start, endDate = end,
                gold21     = PriceSnapshot(4705.0,   6615.0),
                gold24     = PriceSnapshot(5377.0,   7560.0),
                goldUSD    = PriceSnapshot(3363.0,   4470.6),
                jewelryUSD = PriceSnapshot(49.72,    52.59),
                silver     = PriceSnapshot(58.1,     129.0),
                bankUSD    = PriceSnapshot(49.73,    51.94),
                priceGap   = PriceSnapshot(-0.56,     93.67),
                highPrice = 8686.0, lowPrice = 5160.0
            )
        }
    }
}
