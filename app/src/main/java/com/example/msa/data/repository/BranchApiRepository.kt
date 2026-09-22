package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.BranchDto
import com.msa.android.domain.model.Branch
import com.msa.android.domain.repository.BranchRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * الفروع من الـ API.
 *
 * الكاش مفتاحه الموقع مقرّباً لأقرب ~١١٠ متر: لو خزّنا بالإحداثيات
 * الكاملة كان كل تحديث بسيط لموقع المستخدم (وهو واقف مكانه) بيدّي
 * مفتاح جديد وطلب شبكة جديد للا شيء. وفي نفس الوقت لو المستخدم اتحرك
 * مسافة حقيقية، المفتاح بيتغيّر والترتيب بيتحدّث.
 */
@Singleton
class BranchApiRepository @Inject constructor(
    private val api: MsaApi
) : BranchRepository {

    private companion object {
        const val TAG = "BranchRepo"
        const val CACHE_TTL_MS = 10 * 60 * 1000L

        /** 3 خانات عشرية ≈ ١١٠ متر */
        fun keyFor(lat: Double?, lng: Double?): String =
            if (lat == null || lng == null) "no-location"
            else "%.3f,%.3f".format(lat, lng)
    }

    private val mutex = Mutex()
    private var cachedKey: String? = null
    private var cached: List<Branch>? = null
    private var cachedAtMs = 0L

    override suspend fun branches(lat: Double?, lng: Double?): Result<List<Branch>> {
        val key = keyFor(lat, lng)

        mutex.withLock {
            if (cachedKey == key
                && cached != null
                && System.currentTimeMillis() - cachedAtMs < CACHE_TTL_MS
            ) {
                return Result.success(cached!!)
            }
        }

        return runCatching {
            api.branches(lat = lat, lng = lng).data.map { it.toDomain() }
        }.onSuccess { list ->
            mutex.withLock {
                cachedKey = key
                cached = list
                cachedAtMs = System.currentTimeMillis()
            }
        }.onFailure { e ->
            Log.w(TAG, "branches failed", e)
        }
    }

    override suspend fun branch(id: Long): Result<Branch?> {
        // غالباً موجود في الكاش من شاشة القايمة، فالانتقال للتفاصيل
        // بيبقى فوري من غير طلب جديد
        mutex.withLock {
            cached?.firstOrNull { it.id == id }?.let { return Result.success(it) }
        }

        return runCatching { api.branch(id).data.toDomain() }
            .onFailure { e -> Log.w(TAG, "branch $id failed", e) }
    }

    override suspend fun invalidate() = mutex.withLock {
        cachedKey = null
        cached = null
        cachedAtMs = 0L
    }
}

private fun BranchDto.toDomain(): Branch = Branch(
    id = id,
    // الاسم مطلوب في السيرفر، بس بنحمي نفسنا من رد ناقص بدل ما
    // الشاشة تعرض كارت بعنوان فاضي
    nameAr = nameAr.orEmpty(),
    nameEn = nameEn.orEmpty(),
    addressAr = addressAr,
    addressEn = addressEn,
    cityAr = cityAr,
    cityEn = cityEn,
    latitude = latitude,
    longitude = longitude,
    phone = phone?.takeIf { it.isNotBlank() },
    whatsapp = whatsapp?.takeIf { it.isNotBlank() },
    workingHoursAr = workingHoursAr,
    workingHoursEn = workingHoursEn,
    imageUrl = imageUrl?.takeIf { it.isNotBlank() },
    directionsUrl = directionsUrl?.takeIf { it.isNotBlank() },
    distanceKm = distanceKm
)
