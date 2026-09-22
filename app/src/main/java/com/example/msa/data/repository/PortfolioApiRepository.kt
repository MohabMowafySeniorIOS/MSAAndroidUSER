package com.msa.android.data.repository

import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.ApiError
import com.msa.android.data.source.network.dto.MetalBreakdownDto
import com.msa.android.data.source.network.dto.PortfolioItemDto
import com.msa.android.data.source.network.dto.PortfolioSummaryDto
import com.msa.android.domain.model.ItemType
import com.msa.android.domain.model.MetalBreakdown
import com.msa.android.domain.model.PortfolioEntry
import com.msa.android.domain.model.PortfolioMetal
import com.msa.android.domain.model.PortfolioTotals
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** بيانات القطعة وقت الإضافة أو التعديل */
data class PortfolioDraft(
    val metal: PortfolioMetal,
    val karat: String,
    val type: ItemType,
    val purchaseDate: String?,
    val weight: Double,
    val gramPrice: Double,
    val manufacturingPerGram: Double,
    val cashbackPerGram: Double,
    val totalPaid: Double?,
    val note: String?,
    val imageFile: File?
)

sealed interface PortfolioResult {
    data class Success(val totals: PortfolioTotals, val items: List<PortfolioEntry>) : PortfolioResult
    data class Failure(val message: String) : PortfolioResult
}

@Singleton
class PortfolioApiRepository @Inject constructor(
    private val api: MsaApi,
    private val moshi: Moshi
) {

    suspend fun load(gold21: Double, silver999: Double): PortfolioResult = safely {
        val payload = api.portfolio(gold21, silver999).bodyOrThrow().data
        PortfolioResult.Success(payload.summary.toDomain(), payload.items.map { it.toDomain() })
    }

    suspend fun add(draft: PortfolioDraft): String? = mutate {
        api.addPortfolioItem(draft.fields(), draft.imagePart())
    }

    suspend fun update(id: Long, draft: PortfolioDraft): String? = mutate {
        api.updatePortfolioItem(id, draft.fields(), draft.imagePart())
    }

    suspend fun delete(id: Long): String? = try {
        api.deletePortfolioItem(id).unitOrThrow(); null
    } catch (e: ApiException) {
        e.error.firstMessage() ?: "حصل خطأ"
    } catch (e: Exception) {
        NETWORK_ERROR
    }

    // ── تحويل الطلب ────────────────────────────────────────────────

    private fun PortfolioDraft.fields(): Map<String, RequestBody> {
        val text = "text/plain".toMediaType()
        val map = linkedMapOf(
            "metal" to metal.apiValue,
            "karat" to karat,
            "item_type" to type.apiValue,
            "weight" to weight.toString(),
            "gram_price" to gramPrice.toString(),
            "manufacturing_per_gram" to manufacturingPerGram.toString(),
            "cashback_per_gram" to cashbackPerGram.toString()
        )
        // الحقول الاختيارية بتتبعت بس لو ليها قيمة — إرسالها فاضية
        // بيخلّي تحقق لارافيل يرفضها.
        purchaseDate?.takeIf { it.isNotBlank() }?.let { map["purchase_date"] = it }
        totalPaid?.let { map["total_paid"] = it.toString() }
        note?.takeIf { it.isNotBlank() }?.let { map["note"] = it }

        return map.mapValues { (_, v) -> v.toRequestBody(text) }
    }

    private fun PortfolioDraft.imagePart(): MultipartBody.Part? {
        val file = imageFile ?: return null
        val body = file.asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData("image", file.name, body)
    }

    // ── تحويل الرد ─────────────────────────────────────────────────

    private fun PortfolioItemDto.toDomain() = PortfolioEntry(
        id = id,
        metal = PortfolioMetal.from(metal),
        karat = karat,
        type = ItemType.from(itemType),
        purchaseDate = purchaseDate,
        weight = weight,
        gramPrice = gramPrice,
        manufacturingPerGram = manufacturingPerGram,
        cashbackPerGram = cashbackPerGram,
        totalPaid = totalPaid,
        note = note,
        imageUrl = imageUrl,
        currentValue = currentValue,
        shopSellValue = shopSellValue,
        profit = profit
    )

    private fun MetalBreakdownDto.toDomain() = MetalBreakdown(
        metal = PortfolioMetal.from(metal),
        currentValue = currentValue,
        totalPaid = totalPaid,
        totalWeight = totalWeight,
        itemsCount = itemsCount,
        profit = profit,
        profitPercent = profitPercent,
        avgBuyPrice = avgBuyPrice
    )

    private fun PortfolioSummaryDto.toDomain() = PortfolioTotals(
        currentValue = currentValue,
        totalPaid = totalPaid,
        totalWeight = totalWeight,
        cashbackTotal = cashbackTotal,
        shopSellValue = shopSellValue,
        profit = profit,
        profitPercent = profitPercent,
        itemsCount = itemsCount,
        byMetal = byMetal.map { it.toDomain() }
    )

    // ── معالجة الأخطاء ─────────────────────────────────────────────

    private inline fun safely(block: () -> PortfolioResult): PortfolioResult = try {
        block()
    } catch (e: ApiException) {
        PortfolioResult.Failure(e.error.firstMessage() ?: "حصل خطأ")
    } catch (e: Exception) {
        PortfolioResult.Failure(NETWORK_ERROR)
    }

    /** بترجع رسالة الخطأ، أو null لو العملية نجحت */
    private inline fun <T> mutate(block: () -> Response<T>): String? = try {
        block().unitOrThrow(); null
    } catch (e: ApiException) {
        e.error.firstMessage() ?: "حصل خطأ"
    } catch (e: Exception) {
        NETWORK_ERROR
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (!isSuccessful) throw ApiException(parseError(this))
        return body() ?: throw ApiException(ApiError(message = "رد فاضي من السيرفر"))
    }

    private fun <T> Response<T>.unitOrThrow() {
        if (!isSuccessful) throw ApiException(parseError(this))
    }

    private fun <T> parseError(response: Response<T>): ApiError {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        val parsed = raw?.let {
            runCatching { moshi.adapter(ApiError::class.java).fromJson(it) }.getOrNull()
        }
        return parsed ?: ApiError(
            message = when (response.code()) {
                401 -> "لازم تسجّل الدخول"
                403 -> "مش من حقك تعدّل القطعة دي"
                in 500..599 -> "في مشكلة في السيرفر، جرّب كمان شوية"
                else -> "حصل خطأ غير متوقع"
            }
        )
    }

    private class ApiException(val error: ApiError) : Exception(error.message)

    private companion object {
        const val NETWORK_ERROR = "تأكد من اتصالك بالإنترنت وجرّب تاني"
    }
}
