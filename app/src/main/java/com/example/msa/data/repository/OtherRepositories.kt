package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.firebase.FirestoreConstants
import com.msa.android.data.source.firebase.snapshotsAsFlow
import com.msa.android.domain.model.*
import com.msa.android.domain.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaqRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FaqRepository {

    companion object { private const val TAG = "FaqRepo" }

    /**
     * IMPORTANT — iOS uses a typo'd field name: "qestion_ar" / "qestion_en"
     * (missing the 'u'). We try the typo first, then fall back to "question_*".
     *
     * See iOS FAQViewModel.swift:
     *     var question = data["qestion_ar"] as? String : data["qestion_en"] as? String
     */
    override fun observeFaqs(lang: String): Flow<List<FaqItem>> =
        firestore.collection(FirestoreConstants.FAQ)
            .snapshotsAsFlow()
            .map { snap ->
                Log.d(TAG, "faq snapshot: ${snap.documents.size} docs")
                snap.documents.mapNotNull { doc ->
                    val question = doc.getString("qestion_$lang")          // iOS typo
                        ?: doc.getString("question_$lang")
                        ?: doc.getString("question")
                        ?: ""
                    val answer = doc.getString("answer_$lang")
                        ?: doc.getString("answer")
                        ?: ""
                    FaqItem(id = doc.id, question = question, answer = answer)
                }.filter { it.question.isNotBlank() }
            }
}

@Singleton
class PagesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PagesRepository {

    companion object { private const val TAG = "PagesRepo" }

    /**
     * iOS reads the entire `pages` collection and filters in-memory by
     * documentID matching one of: about / terms / privacy / refund.
     * We do the same — listen to one specific document.
     *
     * Field names match iOS verbatim (typo preserved!):
     *   • title_ar / title_en
     *   • describtion_ar / describtion_en  (yes, "describtion" with missing 'p')
     *
     * Falls back to "content_ar/en" if the typo'd field is missing.
     */
    override fun observePage(type: PageType): Flow<PageContent?> =
        firestore.collection(FirestoreConstants.PAGES)
            .document(type.docId)
            .snapshotsAsFlow()
            .map { doc ->
                android.util.Log.d(TAG, "page ${type.docId}: exists=${doc.exists()}")
                if (!doc.exists()) null
                else PageContent(
                    id = doc.id,
                    titleAr = doc.getString("title_ar") ?: "",
                    titleEn = doc.getString("title_en") ?: "",
                    // iOS:  data["describtion_ar"] as? String  (typo preserved)
                    contentAr = doc.getString("describtion_ar")
                        ?: doc.getString("description_ar")
                        ?: doc.getString("content_ar")
                        ?: "",
                    contentEn = doc.getString("describtion_en")
                        ?: doc.getString("description_en")
                        ?: doc.getString("content_en")
                        ?: ""
                )
            }
}

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContactRepository {

    override suspend fun send(message: ContactMessage): Result<Unit> = runCatching {
        firestore.collection(FirestoreConstants.CONTACT_US)
            .add(mapOf(
                "name" to message.name,
                "email" to message.email,
                "message" to message.message,
                "createdAt" to com.google.firebase.Timestamp.now()
            ))
            .await()
        Unit
    }
}

@Singleton
class OnBoardingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OnBoardingRepository {

    override fun observePages(): Flow<List<OnBoardingPage>> =
        firestore.collection(FirestoreConstants.ONBOARDING)
            .snapshotsAsFlow()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    OnBoardingPage(
                        id = doc.id,
                        // iOS Sliders model: { title, des, image }
                        imageUrl = doc.getString("image") ?: "",
                        titleAr = doc.getString("title_ar") ?: doc.getString("title") ?: "",
                        titleEn = doc.getString("title_en") ?: doc.getString("title") ?: "",
                        descAr = doc.getString("description_ar") ?: doc.getString("des") ?: "",
                        descEn = doc.getString("description_en") ?: doc.getString("des") ?: "",
                        order = (doc.get("order") as? Number)?.toInt() ?: 0
                    )
                }.sortedBy { it.order }
            }
}

@Singleton
class VersionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VersionRepository {

    // Real doc (see Firebase console): collection "appVersion", doc "appVersion",
    // fields { version: String, needMaintain: Boolean }. Watched live so a
    // remote change (e.g. flipping needMaintain) reaches the app without a restart.
    override fun observeVersion(): Flow<AppVersionInfo> =
        firestore.collection(FirestoreConstants.APP_VERSION)
            .document(FirestoreConstants.APP_VERSION)
            .snapshotsAsFlow()
            .map { doc ->
                AppVersionInfo(
                    version = doc.getString("version") ?: "1.0",
                    needMaintain = doc.getBoolean("needMaintain") ?: false
                )
            }

    // Same doc, per-screen keys. Android reads homeMaintain / dollarMaintain;
    // iOS reads its own ioshomeMaintain / iosdollarMaintain, so either platform
    // can be taken down on its own. Titles/messages are optional per-language
    // overrides — when absent the UI falls back to R.string.maintenance_screen_*.
    override fun observeScreenMaintenance(): Flow<ScreenMaintenance> =
        firestore.collection(FirestoreConstants.APP_VERSION)
            .document(FirestoreConstants.APP_VERSION)
            .snapshotsAsFlow()
            .map { doc ->
                ScreenMaintenance(
                    homeMaintain = doc.getBoolean(FirestoreConstants.FIELD_HOME_MAINTAIN) ?: false,
                    homeText = doc.maintenanceText(
                        FirestoreConstants.FIELD_HOME_MAINTAIN_TITLE,
                        FirestoreConstants.FIELD_HOME_MAINTAIN_MESSAGE
                    ),
                    dollarMaintain = doc.getBoolean(FirestoreConstants.FIELD_DOLLAR_MAINTAIN) ?: false,
                    dollarText = doc.maintenanceText(
                        FirestoreConstants.FIELD_DOLLAR_MAINTAIN_TITLE,
                        FirestoreConstants.FIELD_DOLLAR_MAINTAIN_MESSAGE
                    )
                )
            }

    /**
     * Reads `<key>_ar` / `<key>_en`, falling back to the un-suffixed `<key>`
     * so a document written before the copy was split per language still works.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.maintenanceText(
        titleKey: String,
        messageKey: String
    ): MaintenanceText {
        fun read(key: String, suffix: String): String =
            getString(key + suffix).orEmpty().ifBlank { getString(key).orEmpty() }

        return MaintenanceText(
            titleAr = read(titleKey, FirestoreConstants.SUFFIX_AR),
            titleEn = read(titleKey, FirestoreConstants.SUFFIX_EN),
            messageAr = read(messageKey, FirestoreConstants.SUFFIX_AR),
            messageEn = read(messageKey, FirestoreConstants.SUFFIX_EN)
        )
    }
}

/**
 * 1:1 port of iOS bullionsScreenView Firestore reads.
 *
 *   Billions/{metalId}                                       ← name_ar, name_en
 *   Billions/{metalId}/{metalId}/{anyDoc}                    ← CompanyName: [String]
 *   Billions/{metalId}/{metalId}/{metalId}/{company}/{anyDoc}
 *      ← gram: [{name_ar, name_en, count, cashBack, Manufacturing}, ...]
 */
@Singleton
class BullionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BullionRepository {

    companion object { private const val TAG = "BullionRepo" }

    override fun observeMetals(): Flow<List<BullionMetalType>> =
        firestore.collection("Billions")
            .snapshotsAsFlow()
            .map { snap ->
                android.util.Log.d(TAG, "metals snapshot: ${snap.documents.size} docs")
                snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    BullionMetalType(
                        id = doc.id,
                        name = (data["name_ar"] as? String)
                            ?: (data["name_en"] as? String) ?: "",
                        type = doc.id
                    )
                }
            }

    override fun observeCompanies(metalId: String): Flow<List<String>> =
        firestore.collection("Billions")
            .document(metalId)
            .collection(metalId)
            .snapshotsAsFlow()
            .map { snap ->
                android.util.Log.d(TAG, "companies($metalId): ${snap.documents.size} docs")
                snap.documents.flatMap { doc ->
                    (doc.data?.get("CompanyName") as? List<*>)
                        ?.filterIsInstance<String>()
                        .orEmpty()
                }
            }

    override fun observeGrams(metalId: String, company: String): Flow<List<BullionGramItem>> =
        firestore.collection("Billions")
            .document(metalId)
            .collection(metalId)
            .document(metalId)
            .collection(company)
            .snapshotsAsFlow()
            .map { snap ->
                android.util.Log.d(TAG, "grams($metalId/$company): ${snap.documents.size} docs")
                val out = mutableListOf<BullionGramItem>()
                for (doc in snap.documents) {
                    val gramList = doc.data?.get("gram") as? List<*> ?: continue
                    for (item in gramList) {
                        val m = item as? Map<*, *> ?: continue
                        val name = (m["name_ar"] as? String)
                            ?: (m["name_en"] as? String) ?: ""
                        val count         = (m["count"]         as? Number)?.toDouble() ?: 0.0
                        val cashBack      = (m["cashBack"]      as? Number)?.toDouble() ?: 0.0
                        val manufacturing = (m["Manufacturing"] as? Number)?.toDouble() ?: 0.0
                        out += BullionGramItem(
                            name = name,
                            count = count,
                            cashBack = cashBack,
                            manufacturing = manufacturing,
                            manufacturingPrice = manufacturing * count
                        )
                    }
                }
                out
            }
}

/**
 * News implementation matching iOS AllNewsVC.getNews() exactly:
 *   Firestore.firestore()
 *     .collection("news")
 *     .order(by: "publishedAt", descending: true)
 *     .addSnapshotListener { ... }
 *
 * iOS reads Firestore documents with these EXACT field names:
 *   data["title"]       — String
 *   data["description"] — String
 *   data["url"]         — String
 *   data["imageUrl"]    — String   (NOT "urlToImage"! the struct field is `urlToImage`
 *                                    but the Firestore key iOS reads is `imageUrl`)
 *   data["publishedAt"] — String   (ISO8601)
 *   data["source"]      — String   (iOS wraps as Source(name: ...))
 */
@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val msaApi: com.msa.android.data.source.network.MsaApi,
    private val languagePreferences: com.msa.android.data.source.local.LanguagePreferences
) : NewsRepository {

    companion object { private const val TAG = "NewsRepo" }

    override fun observeNews(): Flow<List<NewsItem>> =
        firestore.collection("news")
            .orderBy("publishedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshotsAsFlow()
            .map { snap ->
                android.util.Log.d(TAG, "news snapshot: ${snap.documents.size} docs")
                snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val item = NewsItem(
                        id          = doc.id,
                        title       = data["title"]       as? String,
                        description = data["description"] as? String,
                        url         = data["url"]         as? String,
                        // iOS reads "imageUrl" — try it first, then fall back to common alternatives
                        urlToImage  = (data["imageUrl"]   as? String)
                            ?: (data["urlToImage"]        as? String)
                            ?: (data["image"]             as? String)
                            ?: (data["image_url"]         as? String),
                        publishedAt = data["publishedAt"] as? String,
                        // iOS:  Source(name: data["source"] as? String)  — source itself is a String
                        sourceName  = (data["source"]     as? String)
                            ?: ((data["source"] as? Map<*, *>)?.get("name") as? String)
                    )
                    android.util.Log.d(TAG, "news ${doc.id}: title=${item.title?.take(40)} img=${item.urlToImage}")
                    item
                }
            }

    /**
     * MSA-curated news — comes from the REST API at
     * `https://msagold.com/api/v1/news` (iOS uses Alamofire with the same
     * base URL via `APIClient.shared`).
     *
     * Each NewsData → NewsItem mapping mirrors iOS:
     *
     *     func map() -> NewsItem {
     *         if currentLang == "ar" {
     *             return NewsItem(title: title?.ar ?? "", description: description?.ar,
     *                             url: thumb_url, urlToImage: thumb_url, ...)
     *         } else {
     *             return NewsItem(title: title?.en ?? "", description: description?.en, ...)
     *         }
     *     }
     *
     * Note that both `url` and `urlToImage` get the same `thumb_url` value —
     * MSA posts are image-first, so the iOS tap handler opens the image URL
     * in a browser. Our Android side opens it in the fullscreen ImageViewer
     * for a nicer UX, but the underlying URL is the same.
     */
    override fun observeMsaNews(): Flow<List<NewsItem>> = flow {
        try {
            val lang = languagePreferences.currentLanguage()
            android.util.Log.d(TAG, "MSANews: GET https://msagold.com/api/v1/news (lang=$lang)")
            val response = msaApi.getNews()
            val items = response.data.orEmpty().map { dto ->
                NewsItem(
                    id          = (dto.id ?: 0).toString(),
                    title       = dto.title?.forLanguage(lang),
                    description = dto.description?.forLanguage(lang),
                    url         = dto.thumb_url,
                    urlToImage  = dto.thumb_url,
                    publishedAt = dto.created_at,
                    sourceName  = "MSA"
                )
            }
            android.util.Log.d(TAG, "MSANews API: ${items.size} items")
            emit(items)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "MSANews API failed: ${e.message}", e)
            emit(emptyList())
        }
    }
}
