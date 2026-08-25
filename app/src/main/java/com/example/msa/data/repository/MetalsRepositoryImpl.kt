package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.firebase.FirestoreConstants
import com.msa.android.data.source.firebase.snapshotsAsFlow
import com.msa.android.domain.model.Metal
import com.msa.android.domain.model.OuncePrice
import com.msa.android.domain.repository.MetalsRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS HomeVC behavior:
 *   • metals collection — doc.id = type, "name_ar" = name, etc.
 *   • Ounce price ("الشاشة العالمية") — controlled entirely from Firestore
 *     now (`metalsOunce/ounce`, written by the Node.js backend). The old
 *     on-device investing.com scraping fallback has been removed on purpose
 *     (product decision): the app no longer scrapes anything itself for the
 *     Home screen — if Firestore doesn't have a value, the UI just shows
 *     "جاري التحميل" until it does.
 */
@Singleton
class MetalsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MetalsRepository {

    companion object {
        private const val TAG = "MetalsRepo"
    }

    override fun observeMetals(): Flow<List<Metal>> =
        firestore.collection(FirestoreConstants.METALS)
            .snapshotsAsFlow()
            .map { snap ->
                Log.d(TAG, "metals snapshot: ${snap.documents.size} docs")
                snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    // ounce lives in its own "metalsOunce" collection (see
                    // observeOuncePrice below), so this filter is just a safety
                    // net in case an old "ounce_price"/"ounce" doc still exists
                    // under "metals" from before.
                    if (doc.id == FirestoreConstants.OUNCE_DOC) return@mapNotNull null
                    val metal = Metal(
                        type = doc.id,
                        name = data["name_ar"] as? String
                            ?: data["name"] as? String,
                        buyPrice  = (data["buyPrice"]  as? String) ?: (data["buyPrice"]  as? Number)?.toString(),
                        salePrice = (data["salePrice"] as? String) ?: (data["salePrice"] as? Number)?.toString(),
                        updatedAt = (data["updatedAt"] as? Timestamp)?.toDate()?.time
                    )
                    Log.d(TAG, "metal doc id=${doc.id} buy=${metal.buyPrice} sell=${metal.salePrice}")
                    metal
                }
            }

    /**
     * Ounce price — Firestore `metalsOunce/ounce` only, live listener. Shape:
     *   { gold: { price: "2350.50" }, silver: { price: "29.30" } }
     * (price is a String, matching the Node.js backend and iOS's
     * listenToOuncePrice() exactly.)
     *
     * No on-device scraping fallback anymore — if this doc is missing or a
     * price field is invalid, the corresponding value stays 0.0 and the UI
     * shows its "جاري التحميل" placeholder until Firestore has a real value.
     */
    override fun observeOuncePrice(): Flow<OuncePrice> = callbackFlow {
        val reg = firestore.collection(FirestoreConstants.METALS_OUNCE)
            .document(FirestoreConstants.OUNCE_DOC)
            .addSnapshotListener { doc, err ->
                if (err != null) {
                    Log.e(TAG, "ounce listener error: ${err.message}")
                    return@addSnapshotListener
                }
                if (doc?.exists() == true) {
                    val goldPriceStr = (doc.get("gold") as? Map<*, *>)?.get("price")
                    val silverPriceStr = (doc.get("silver") as? Map<*, *>)?.get("price")

                    val gold = when (goldPriceStr) {
                        is String -> goldPriceStr.toDoubleOrNull() ?: 0.0
                        is Number -> goldPriceStr.toDouble()
                        else -> 0.0
                    }
                    val silver = when (silverPriceStr) {
                        is String -> silverPriceStr.toDoubleOrNull() ?: 0.0
                        is Number -> silverPriceStr.toDouble()
                        else -> 0.0
                    }

                    Log.d(TAG, "ounce from Firestore: gold=$gold silver=$silver")
                    trySend(OuncePrice(goldPrice = gold, silverPrice = silver))
                } else {
                    Log.w(TAG, "Firestore metalsOunce/ounce doc missing")
                    trySend(OuncePrice())
                }
            }
        awaitClose { reg.remove() }
    }

    override fun observeLastUpdated(): Flow<Long?> =
        firestore.collection(FirestoreConstants.METALS)
            .snapshotsAsFlow()
            .map { snap ->
                snap.documents.mapNotNull {
                    (it.get("updatedAt") as? Timestamp)?.toDate()?.time
                }.maxOrNull()
            }
}
