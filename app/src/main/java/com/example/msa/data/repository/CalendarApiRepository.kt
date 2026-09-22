package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.CalendarEventDto
import com.msa.android.domain.model.CalendarCurrency
import com.msa.android.domain.model.CalendarDay
import com.msa.android.domain.model.EconomicEvent
import com.msa.android.domain.model.EventDetails
import com.msa.android.domain.model.EventHistory
import com.msa.android.domain.model.EventImpact
import com.msa.android.domain.model.EventPoint
import com.msa.android.domain.repository.CalendarRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * التقويم الاقتصادي من الـ API.
 *
 * التطبيق عمره ما بيوصل للمصدر الخارجي — بيقرا من السيرفر، والسيرفر
 * هو اللي بيزامن كل ساعة. نفس مبدأ [FomcApiRepository] بالظبط.
 *
 * ## الكاش دقيقتين بس
 *
 * `FomcApiRepository` بيكاش ١٥ دقيقة لأن مواعيد الاجتماعات مبتتغيّرش
 * غير كل كام أسبوع. هنا العكس: يوم صدور رقم مهم، خانة «الفعلي» بتتملي
 * في ثواني — وكاش ربع ساعة معناه إن المستخدم يفتح الشاشة بعد الخبر
 * ويلاقيها لسه فاضية.
 */
@Singleton
class CalendarApiRepository @Inject constructor(
    private val api: MsaApi,
    private val language: com.msa.android.data.source.local.LanguagePreferences
) : CalendarRepository {

    private companion object {
        const val TAG = "CalendarRepo"

        /** دقيقتين — نفس `CALENDAR_CACHE_TTL` الافتراضي في لارافيل */
        const val CACHE_TTL_MS = 2 * 60 * 1000L

        val DATE_PARAM: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val mutex = Mutex()

    /** مفتاح الكاش = بصمة الفلاتر، عشان تغيير الفلتر ما يرجّعش نتيجة قديمة */
    private var cacheKey: String? = null
    private var cachedDays: List<CalendarDay>? = null
    private var cachedAtMs = 0L

    private var cachedCurrencies: List<CalendarCurrency>? = null

    /**
     * لغة أسماء المؤشرات.
     *
     * المصدر بيبعتها بالإنجليزي بس والسيرفر بيعرّبها، فلازم نقول له
     * اللغة. بناخدها من إعدادات **التطبيق** مش من النظام: المستخدم
     * ممكن يكون مختار عربي جوه التطبيق وجهازه إنجليزي.
     */
    private suspend fun lang(): String =
        if (language.currentLanguage().startsWith("ar")) "ar" else "en"

    override suspend fun days(
        from: LocalDate,
        to: LocalDate,
        impacts: Set<EventImpact>,
        currencies: Set<String>,
        forceRefresh: Boolean
    ): Result<List<CalendarDay>> {

        val lang = lang()

        /*
         * اللغة جزء من مفتاح الكاش.
         *
         * من غيرها، تبديل لغة التطبيق كان بيسيب الأسماء القديمة
         * معروضة لحد ما الكاش يقدم — يعني شاشة عربية فيها أسماء
         * إنجليزية لدقيقتين.
         */
        val key = listOf(
            from, to,
            impacts.map { it.key }.sorted().joinToString(","),
            currencies.sorted().joinToString(","),
            lang
        ).joinToString("|")

        if (!forceRefresh) {
            mutex.withLock {
                if (cacheKey == key &&
                    cachedDays != null &&
                    System.currentTimeMillis() - cachedAtMs < CACHE_TTL_MS
                ) {
                    return Result.success(cachedDays!!)
                }
            }
        }

        return runCatching {
            val dto = api.calendarEvents(
                from = DATE_PARAM.format(from),
                to = DATE_PARAM.format(to),
                // فاضي = مفيش فلتر. بنبعت null مش نص فاضي عشان
                // البارامتر ما يتحطش في الرابط أصلاً.
                impact = impacts.takeIf { it.isNotEmpty() }
                    ?.joinToString(",") { it.key },
                currencies = currencies.takeIf { it.isNotEmpty() }
                    ?.joinToString(","),
                lang = lang
            ).data

            dto.days.mapNotNull { day ->
                val date = day.date.toLocalDateOrNull() ?: return@mapNotNull null

                CalendarDay(
                    date = date,
                    events = day.events.mapNotNull { it.toDomain() }
                )
            }.filter { it.events.isNotEmpty() }

        }.onSuccess { list ->
            mutex.withLock {
                cacheKey = key
                cachedDays = list
                cachedAtMs = System.currentTimeMillis()
            }
        }.onFailure { e ->
            Log.w(TAG, "calendar days failed", e)
        }
    }

    override suspend fun details(id: Long): Result<EventDetails?> = runCatching {
        val dto = api.calendarEvent(id, lang = lang()).data
        val event = dto.event.toDomain() ?: return@runCatching null

        EventDetails(event = event, next = dto.next?.toDomain())
    }.onFailure { e ->
        Log.w(TAG, "calendar event $id failed", e)
    }

    override suspend fun history(id: Long, limit: Int): Result<EventHistory> = runCatching {
        val dto = api.calendarHistory(id, limit, lang = lang()).data

        EventHistory(
            eventKey = dto.eventKey,
            title = dto.title,
            unit = dto.unit,
            points = dto.points.mapNotNull { p ->
                val at = p.occursAt.toInstantOrNull() ?: return@mapNotNull null
                EventPoint(occursAt = at, label = p.actual, value = p.value, forecast = p.forecast)
            },
            releases = dto.releases.mapNotNull { it.toDomain() }
        )
    }.onFailure { e ->
        Log.w(TAG, "calendar history $id failed", e)
    }

    /**
     * قايمة العملات للفلتر.
     *
     * بتتكاش لحد ما التطبيق يتقفل: العملات الموجودة مبتتغيّرش من
     * ساعة للتانية، وفتح شاشة الفلتر ما يستاهلش طلب شبكة كل مرة.
     */
    override suspend fun currencies(): Result<List<CalendarCurrency>> {
        mutex.withLock { cachedCurrencies?.let { return Result.success(it) } }

        return runCatching {
            api.calendarFilters().data.currencies.mapNotNull { c ->
                val code = c.code?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                CalendarCurrency(code = code, countryCode = c.countryCode?.trim())
            }
        }.onSuccess { list ->
            mutex.withLock { cachedCurrencies = list }
        }.onFailure { e ->
            Log.w(TAG, "calendar filters failed", e)
        }
    }

    override suspend fun invalidate() = mutex.withLock {
        cacheKey = null
        cachedDays = null
        cachedAtMs = 0L
    }
}

/**
 * تحويل الرد لموديل التطبيق.
 *
 * بيرجّع `null` لو الحدث مالوش عنوان أو وقت — صف زي ده مش قابل للعرض
 * ولا للترتيب، فبنتجاهله بدل ما نعرض خانة فاضية في نص القايمة.
 */
private fun CalendarEventDto.toDomain(): EconomicEvent? {
    val at = occursAt.toInstantOrNull() ?: return null
    val name = title?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return EconomicEvent(
        id = id,
        title = name,
        eventKey = eventKey,
        currency = currency?.trim()?.takeIf { it.isNotEmpty() },
        countryCode = countryCode?.trim()?.takeIf { it.isNotEmpty() },
        occursAt = at,
        allDay = allDay,
        impact = EventImpact.from(impact),
        actual = actual?.trim()?.takeIf { it.isNotEmpty() },
        forecast = forecast?.trim()?.takeIf { it.isNotEmpty() },
        previous = previous?.trim()?.takeIf { it.isNotEmpty() },
        actualValue = actualValue,
        forecastValue = forecastValue,
        previousValue = previousValue,
        unit = unit?.trim()?.takeIf { it.isNotEmpty() },
        category = category?.trim()?.takeIf { it.isNotEmpty() },
        description = description?.trim()?.takeIf { it.isNotEmpty() }
    )
}

/** ISO-8601 بتوقيت UTC. تاريخ بايظ = `null` بدل استثناء يوقّع الشاشة. */
private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null

    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        Log.w("CalendarRepo", "unparsable date: $this")
        null
    }
}

/**
 * `YYYY-MM-DD` من تجميع السيرفر.
 *
 * ⚠️ السيرفر بيجمّع بتوقيت **UTC**. بنستخدم التاريخ ده كمفتاح ترتيب
 * بس، والعنوان اللي المستخدم بيقراه بيتحسب من وقت أول حدث بتوقيت
 * الجهاز — عشان مستخدم في طوكيو ما يشوفش «الأربع» فوق أحداث بتحصل
 * عنده الخميس.
 */
private fun String?.toLocalDateOrNull(): LocalDate? {
    if (this.isNullOrBlank()) return null

    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}

/** اليوم اللي الحدث واقع فيه بتوقيت الجهاز */
internal fun EconomicEvent.localDate(): LocalDate =
    occursAt.atZone(ZoneId.systemDefault()).toLocalDate()
