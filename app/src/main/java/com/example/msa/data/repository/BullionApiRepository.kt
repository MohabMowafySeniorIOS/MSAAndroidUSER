package com.msa.android.data.repository

import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.BullionMetalDto
import com.msa.android.domain.model.BullionGramItem
import com.msa.android.domain.model.BullionMetalType
import com.msa.android.domain.repository.BullionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * السبائك من الـ API بدل Firestore.
 *
 * السيرفر بيرجّع الشجرة كاملة في طلب واحد، فبنجيبها مرة ونخزّنها في
 * الذاكرة. الشاشة بتنده على الثلاث دوال ورا بعض وهي بتملا القوايم،
 * ومن غير التخزين ده كان هيتبعت ثلاث طلبات لنفس البيانات.
 */
@Singleton
class BullionApiRepository @Inject constructor(
    private val api: MsaApi,
    private val language: LanguagePreferences
) : BullionRepository {

    private val mutex = Mutex()
    private var cached: List<BullionMetalDto>? = null

    private suspend fun tree(): List<BullionMetalDto> = mutex.withLock {
        cached ?: runCatching { api.bullions().data }
            .getOrDefault(emptyList())
            .also { cached = it.ifEmpty { null } }
    }

    /** بيتنادى لو المستخدم عمل سحب للتحديث */
    suspend fun refresh() = mutex.withLock { cached = null }

    private suspend fun isArabic(): Boolean = language.languageFlow.first() == "ar"

    override fun observeMetals(): Flow<List<BullionMetalType>> = flow {
        val ar = isArabic()
        emit(
            tree().map {
                BullionMetalType(
                    id = it.slug,
                    name = (if (ar) it.nameAr else it.nameEn) ?: it.slug,
                    type = it.slug
                )
            }
        )
    }

    override fun observeCompanies(metalId: String): Flow<List<String>> = flow {
        emit(tree().firstOrNull { it.slug == metalId }?.companies?.map { it.name } ?: emptyList())
    }

    override fun observeGrams(metalId: String, company: String): Flow<List<BullionGramItem>> = flow {
        val ar = isArabic()
        val products = tree()
            .firstOrNull { it.slug == metalId }
            ?.companies?.firstOrNull { it.name == company }
            ?.products ?: emptyList()

        emit(
            products.map { p ->
                BullionGramItem(
                    name = (if (ar) p.nameAr else p.nameEn).orEmpty(),
                    // `count` في نموذج التطبيق هو وزن السبيكة بالجرام
                    count = p.weight,
                    cashBack = p.cashBack,
                    manufacturing = p.manufacturing,
                    manufacturingPrice = p.manufacturing * p.weight
                )
            }
        )
    }

    /** رابط شعار الشركة لو اترفع من اللوحة، وإلا null ونرجع للصورة المدمجة. */
    suspend fun companyImageUrl(metalId: String, company: String): String? =
        tree().firstOrNull { it.slug == metalId }
            ?.companies?.firstOrNull { it.name == company }
            ?.imageUrl
}
