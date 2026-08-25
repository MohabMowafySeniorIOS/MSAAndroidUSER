package com.msa.android.domain.repository

import com.msa.android.domain.model.*
import kotlinx.coroutines.flow.Flow

/** Realtime gold + silver prices from `metals` collection. */
interface MetalsRepository {
    fun observeMetals(): Flow<List<Metal>>
    fun observeOuncePrice(): Flow<OuncePrice>
    fun observeLastUpdated(): Flow<Long?>
}

/** Realtime bank rates collection. */
interface BanksRepository {
    /**
     * Observe bank rates for a given currency. iOS layout:
     *   `currencies/{currency}/banks` — each doc has buy/sell/logo/trend.
     * Default `USD` matches iOS `selectedCurrency = "USD"`.
     */
    fun observeBanks(currency: String = "USD"): Flow<List<BankRate>>
    /** Central bank dollar value used by Home calculations. */
    fun observeCentralBankDollar(): Flow<Double>
}

interface FaqRepository {
    fun observeFaqs(lang: String): Flow<List<FaqItem>>
}

interface PagesRepository {
    fun observePage(type: PageType): Flow<PageContent?>
}

interface ContactRepository {
    suspend fun send(message: ContactMessage): Result<Unit>
}

interface OnBoardingRepository {
    fun observePages(): Flow<List<OnBoardingPage>>
}

interface VersionRepository {
    fun observeVersion(): Flow<AppVersionInfo>

    /**
     * Live per-screen maintenance flags from the same `appVersion/appVersion`
     * doc. Separate from [observeVersion] so a screen can be taken down without
     * blocking the whole app.
     */
    fun observeScreenMaintenance(): Flow<ScreenMaintenance>
}

/**
 * 1:1 port of iOS bullionsScreenView Firestore calls.
 *
 *   Billions/{metalId}                                       ← name_ar, name_en
 *   Billions/{metalId}/{metalId}/{anyDoc}                    ← CompanyName: [String]
 *   Billions/{metalId}/{metalId}/{metalId}/{company}/{anyDoc}
 *      ← gram: [{name_ar, name_en, count, cashBack, Manufacturing}, ...]
 */
interface BullionRepository {
    fun observeMetals(): Flow<List<BullionMetalType>>
    fun observeCompanies(metalId: String): Flow<List<String>>
    fun observeGrams(metalId: String, company: String): Flow<List<BullionGramItem>>
}

/**
 * News articles. Mirrors iOS AllNewsVC.getNews():
 *   Firestore.firestore()
 *     .collection("news")
 *     .order(by: "publishedAt", descending: true)
 *     .addSnapshotListener { ... }
 */
interface NewsRepository {
    fun observeNews(): Flow<List<NewsItem>>
    fun observeMsaNews(): Flow<List<NewsItem>>
}

/**
 * QR code verification — mirrors iOS QRRepository.
 *   • verifyQR(id):  reads Firestore.collection("qr_codes").document(id) and
 *                    returns the parsed QRDocument, or null when the doc
 *                    doesn't exist (= invalid QR).
 *   • markUsed(id):  sets isUsed=true and usedAt=server timestamp.
 */
interface QRRepository {
    suspend fun verifyQR(id: String): com.msa.android.domain.model.QRDocument?
    suspend fun markUsed(id: String)

    companion object {
        /**
         * Firestore collection where QR documents live. Each QR's value is
         * used as the document ID (e.g. `qr_codes/ABC123`).
         * Exposed publicly so the diagnostic dialog can show the user where
         * the verification is looking — makes Firebase mismatches obvious.
         */
        const val COLLECTION = "qr_codes"
    }
}

/**
 * The user's portfolio of physical-asset holdings (gold bars, pounds, silver).
 * Persisted locally — no server backing yet — so all CRUD is suspending against
 * a DataStore-backed store. Live updates flow through [observeItems].
 */
interface PortfolioRepository {
    fun observeItems(): kotlinx.coroutines.flow.Flow<List<com.msa.android.domain.model.PortfolioItem>>
    suspend fun addItem(item: com.msa.android.domain.model.PortfolioItem)
    suspend fun updateItem(item: com.msa.android.domain.model.PortfolioItem)
    suspend fun removeItem(id: String)
}
