package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.FomcEventDto
import com.msa.android.domain.model.FomcEvent
import com.msa.android.domain.repository.FomcRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * اجتماعات الفيدرالي من الـ API.
 *
 * التطبيق عمره ما بيوصل لمصدر البيانات الخارجي (FRED / موقع الفيدرالي)
 * — بيقرا من السيرفر بس، والسيرفر هو اللي بيزامن. كده لو المصدر
 * الخارجي وقع، التطبيق بيفضل شغال على آخر بيانات صحيحة.
 *
 * فيه كاش في الذاكرة لمدة [CACHE_TTL_MS]: المستخدم بيفتح الشاشة، يدخل
 * على تفاصيل اجتماع، ويرجع — من غير الكاش ده كان هيتبعت طلب شبكة كل
 * مرة لبيانات مبتتغيّرش غير مرة كل كام أسبوع.
 */
@Singleton
class FomcApiRepository @Inject constructor(
    private val api: MsaApi
) : FomcRepository {

    private companion object {
        const val TAG = "FomcRepo"

        /** ١٥ دقيقة — نفس `FOMC_CACHE_TTL` الافتراضي في لارافيل */
        const val CACHE_TTL_MS = 15 * 60 * 1000L
    }

    private val mutex = Mutex()
    private var cachedEvents: List<FomcEvent>? = null
    private var cachedAtMs: Long = 0L

    private fun isFresh(): Boolean =
        cachedEvents != null && System.currentTimeMillis() - cachedAtMs < CACHE_TTL_MS

    /**
     * كل الاجتماعات (القادم والسابق) في طلب واحد.
     *
     * بنجيب القايمة كاملة مرة واحدة بدل طلب للقادم وطلب للسابق: العدد
     * ٨ اجتماعات في السنة، فالرد صغير والتقسيم في التطبيق أرخص من
     * طلبين.
     */
    override suspend fun events(forceRefresh: Boolean): Result<List<FomcEvent>> {
        if (!forceRefresh) {
            mutex.withLock { if (isFresh()) return Result.success(cachedEvents!!) }
        }

        return runCatching {
            // ١٠٠ هو أقصى `limit` مسموح به في لارافيل — يغطي أكتر من
            // ١٢ سنة اجتماعات، فمفيش داعي لترقيم صفحات هنا.
            api.fomcEvents(period = "all", limit = 100).data
                .mapNotNull { it.toDomain() }
                .sortedByDescending { it.meetingStartAt ?: Instant.EPOCH }
        }.onSuccess { list ->
            mutex.withLock {
                cachedEvents = list
                cachedAtMs = System.currentTimeMillis()
            }
        }.onFailure { e ->
            Log.w(TAG, "fomc events failed", e)
        }
    }

    /**
     * الاجتماع القادم — للعدّاد التنازلي في الرئيسية.
     *
     * لو الكاش لسه صالح بنحسبه منه بدل طلب جديد؛ الفرق إن الكاش عندنا
     * فيه كل الاجتماعات فاختيار القادم منها حساب محلي.
     */
    override suspend fun next(): Result<FomcEvent?> {
        mutex.withLock {
            if (isFresh()) {
                val now = Instant.now()

                return Result.success(
                    cachedEvents!!
                        .filter { (it.countdownTarget ?: Instant.EPOCH).isAfter(now) }
                        .minByOrNull { it.countdownTarget ?: Instant.MAX }
                )
            }
        }

        return runCatching { api.fomcNext().data?.toDomain() }
            .onFailure { e -> Log.w(TAG, "fomc next failed", e) }
    }

    override suspend fun event(id: Long): Result<FomcEvent?> {
        mutex.withLock {
            if (isFresh()) {
                cachedEvents!!.firstOrNull { it.id == id }?.let { return Result.success(it) }
            }
        }

        return runCatching { api.fomcEvent(id).data.toDomain() }
            .onFailure { e -> Log.w(TAG, "fomc event $id failed", e) }
    }

    override suspend fun invalidate() = mutex.withLock {
        cachedEvents = null
        cachedAtMs = 0L
    }
}

/**
 * تحويل الرد لموديل التطبيق.
 *
 * بيرجّع `null` لو الاجتماع مالوش تاريخ بداية أصلاً — سجل زي ده مش
 * قابل للعرض ولا للترتيب، فبنتجاهله بدل ما نعرض كارت فاضي.
 */
private fun FomcEventDto.toDomain(): FomcEvent? {
    val start = meetingStartAt.toInstantOrNull() ?: return null

    return FomcEvent(
        id = id,
        externalId = externalId,
        title = title,
        meetingStartAt = start,
        meetingEndAt = meetingEndAt.toInstantOrNull(),
        decisionAt = decisionAt.toInstantOrNull(),
        pressConferenceAt = pressConferenceAt.toInstantOrNull(),
        minutesAt = minutesAt.toInstantOrNull(),
        federalFundsRate = federalFundsRate,
        rateLowerBound = rateLowerBound,
        rateUpperBound = rateUpperBound,
        previousRate = previousRate,
        rateChange = rateChange,
        hasPressConference = hasPressConference,
        hasProjections = hasProjections,
        statementUrl = statementUrl,
        minutesUrl = minutesUrl,
        sourceUrl = sourceUrl,
        officialTimezone = timezone,
        serverStatus = status
    )
}

/**
 * ISO-8601 بتوقيت UTC (`2026-09-16T18:00:00Z`).
 *
 * تاريخ بايظ بيرجّع `null` بدل ما يرمي استثناء — حقل واحد غلط في
 * اجتماع واحد المفروض ما يوقّعش الشاشة كلها.
 */
private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null

    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        Log.w("FomcRepo", "unparsable date: $this")
        null
    }
}
