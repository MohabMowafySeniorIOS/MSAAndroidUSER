package com.msa.android.data.repository

import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.ContactDto
import com.msa.android.data.source.network.dto.ContentBundleDto
import com.msa.android.domain.model.FaqItem
import com.msa.android.domain.model.PageContent
import com.msa.android.domain.model.PageType
import com.msa.android.domain.repository.FaqRepository
import com.msa.android.domain.repository.PagesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * محتوى شاشة «المزيد» من لوحة التحكم بدل Firestore.
 *
 * السيرفر بيرجّع كل المحتوى في طلب واحد (`/content`)، فبنجيبه مرة
 * ونخزّنه في الذاكرة. المستخدم بيفتح «من نحن» ثم «الأسئلة» ثم
 * «السياسات» ورا بعض — من غير التخزين ده كان هيتبعت طلب لكل شاشة.
 */
@Singleton
class ContentCache @Inject constructor(
    private val api: MsaApi,
    private val language: LanguagePreferences
) {
    private val mutex = Mutex()
    private var cached: ContentBundleDto? = null

    suspend fun bundle(): ContentBundleDto? = mutex.withLock {
        cached ?: runCatching { api.content().data }.getOrNull()?.also { cached = it }
    }

    /** بيتنادى لو المستخدم عمل سحب للتحديث */
    suspend fun invalidate() = mutex.withLock { cached = null }

    suspend fun isArabic(): Boolean = language.languageFlow.first() == "ar"
}

/**
 * معرّفات الصفحات في التطبيق مختلفة عن الـ slugs في السيرفر،
 * فالربط صريح هنا بدل ما نغيّر الـ enum ونكسر أي كود بيستخدمه.
 */
private fun PageType.slug(): String = when (this) {
    PageType.ABOUT_US -> "about_us"
    PageType.PRIVACY  -> "privacy_policy"
    PageType.TERMS    -> "usage_policy"
    PageType.REFUND   -> "refund_policy"
}

@Singleton
class PagesApiRepository @Inject constructor(
    private val cache: ContentCache,
    private val api: MsaApi
) : PagesRepository {

    override fun observePage(type: PageType): Flow<PageContent?> = flow {
        // كل شاشة تقرأ endpoint الصفحة مباشرة؛ فشل بيانات جانبية في /content
        // لا يمنع صفحات «من نحن» والسياسات من الظهور.
        val remote = runCatching { api.page(type.slug()).data }.getOrNull()
        val page = remote ?: cache.bundle()?.pages?.get(type.slug())
        emit(
            page?.let {
                PageContent(
                    id = it.slug,
                    titleAr = it.titleAr.orEmpty(),
                    titleEn = it.titleEn.orEmpty(),
                    contentAr = it.bodyAr.orEmpty(),
                    contentEn = it.bodyEn.orEmpty()
                )
            }
        )
    }
}

@Singleton
class FaqApiRepository @Inject constructor(
    private val cache: ContentCache,
    private val api: MsaApi
) : FaqRepository {

    /**
     * السيرفر بيرجّع اللغتين مع بعض، فالاختيار بيحصل هنا —
     * تغيير اللغة مبيحتاجش طلب جديد للشبكة.
     */
    override fun observeFaqs(lang: String): Flow<List<FaqItem>> = flow {
        val arabic = lang == "ar"
        val remote = runCatching { api.faqs().data }.getOrNull()
        val faqs = remote ?: cache.bundle()?.faqs.orEmpty()

        emit(faqs.map {
                FaqItem(
                    id = it.id.toString(),
                    question = (if (arabic) it.questionAr else it.questionEn).orEmpty(),
                    answer = (if (arabic) it.answerAr else it.answerEn).orEmpty()
                )
            })
    }
}

/** بيانات التواصل — كانت مكتوبة ثابتة في الكود، بقت من اللوحة. */
@Singleton
class ContactInfoRepository @Inject constructor(
    private val cache: ContentCache
) {
    fun observe(): Flow<ContactDto?> = flow { emit(cache.bundle()?.contact) }
}
