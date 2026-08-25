package com.msa.android.presentation.common

import android.content.Context
import com.msa.android.R

/** Mirrors iOS "منذ ١ دقيقة" / "1 minute ago" string format. */
object RelativeTimeFormatter {
    fun format(ctx: Context, millis: Long?): String {
        if (millis == null) return ""
        val diff = (System.currentTimeMillis() - millis).coerceAtLeast(0L) / 1000L
        return when {
            diff < 60 -> ctx.getString(R.string.now)
            diff < 3600 -> {
                val mins = diff / 60
                ctx.resources.getQuantityString(R.plurals.minutes_ago, mins.toInt(), mins.toInt())
            }
            diff < 86400 -> {
                val hours = diff / 3600
                ctx.resources.getQuantityString(R.plurals.hours_ago, hours.toInt(), hours.toInt())
            }
            else -> {
                val days = diff / 86400
                ctx.resources.getQuantityString(R.plurals.days_ago, days.toInt(), days.toInt())
            }
        }
    }
}
