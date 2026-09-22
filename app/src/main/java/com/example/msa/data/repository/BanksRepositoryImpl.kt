package com.msa.android.data.repository

import android.util.Log
import com.msa.android.data.source.firebase.snapshotsAsFlow
import com.msa.android.domain.model.BankRate
import com.msa.android.domain.model.Trend
import com.msa.android.domain.repository.BanksRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bank rates — mirrors iOS `DollarPricesOnBankVC.fetchCurrency()` exactly,
 * including its `bankUpdatedAt`-based ordering:
 *
 *   Firestore.collection("currencies")
 *            .document(selectedCurrency)
 *            .collection("banks")
 *            .order(by: "bankUpdatedAt", descending: true)
 *   // then, client-side: same calendar day → sort by buy descending;
 *   // otherwise keep the bankUpdatedAt order.
 *
 * Each doc has fields: `bank`, `currency`, `buy: Double`, `sell: Double`,
 * `logo`, `trend: String`, `bankUrl`, `bankUpdatedAt: Timestamp`.
 *
 * Default currency is "USD" — same default as iOS.
 */
@Singleton
class BanksRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BanksRepository {

    companion object { private const val TAG = "BanksRepo" }

    override fun observeBanks(currency: String): Flow<List<BankRate>> =
        firestore.collection("currencies")
            .document(currency)
            .collection("banks")
            .orderBy("bankUpdatedAt", Query.Direction.DESCENDING)
            .snapshotsAsFlow()
            .map { snap ->
                Log.d(TAG, "banks/${currency} snapshot: ${snap.documents.size} docs")

                val rates = snap.documents.mapNotNull { doc ->
                    // iOS reads `bank` field (CurrencyBankModel.bank), not `name`.
                    // Fall back to `name` for older docs written by the legacy structure.
                    val bankName = (doc.getString("bank") ?: doc.getString("name"))
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null

                    BankRate(
                        id    = doc.id,
                        name  = bankName,
                        buy   = doc.get("buy")?.toString()  ?: "0",
                        sell  = doc.get("sell")?.toString() ?: "0",
                        logo  = doc.getString("logo") ?: "",
                        date  = doc.getString("date") ?: "",
                        trend = Trend.fromString(doc.getString("trend")),
                        bankUrl = doc.getString("bankUrl") ?: "",
                        currency = doc.getString("currency") ?: currency,
                        updatedAtMillis = doc.getTimestamp("bankUpdatedAt")?.toDate()?.time
                    )
                }

                // نفس منطق iOS بالظبط: لو اتنين بنوك اتحدّثوا في نفس اليوم،
                // رتّبهم بسعر الشراء الأعلى الأول؛ غير كده سيب ترتيب آخر
                // تحديث (Firestore orderBy فوق) زي ما هو.
                rates.sortedWith(Comparator { a, b ->
                    val aMillis = a.updatedAtMillis
                    val bMillis = b.updatedAtMillis
                    if (aMillis == null || bMillis == null) return@Comparator 0

                    if (isSameCalendarDay(aMillis, bMillis)) {
                        val aBuy = a.buy.toDoubleOrNull() ?: 0.0
                        val bBuy = b.buy.toDoubleOrNull() ?: 0.0
                        bBuy.compareTo(aBuy)
                    } else {
                        bMillis.compareTo(aMillis)
                    }
                })
            }

    /**
     * Home-screen USD price. Mirrors iOS `HomeVC.getPrices()` EXACTLY —
     * including which Firestore collection it reads from, which is NOT the
     * same one the dollar-prices-on-banks screen uses:
     *
     *   Firestore.collection("banks")   // flat, top-level — NOT currencies/USD/banks
     *
     * Those docs use `name` (not `bank`), plus `buy`, `sell`, `trend`,
     * `updatedAt`, and `bankUpdatedAt` (the Timestamp used below to pick the
     * freshest of the three candidate banks).
     *
     * Not necessarily the central bank specifically — it's whichever of
     * these three has the most recent `bankUpdatedAt`:
     *   المركزي (Central Bank) / بنك مصر (Banque Misr) / البنك الأهلي المصري (NBE)
     *
     * Skips cache-only snapshots on purpose: Firestore's local persistence
     * cache can hold stale prices from a previous session, and its listener
     * fires with that stale cached snapshot FIRST before the real one syncs
     * from the server a moment later — which is exactly the "shows a wrong
     * price for a second, then corrects itself" symptom. Waiting for a
     * server-confirmed snapshot means the very first value shown is always
     * the real one; the UI just shows its loading state a little longer
     * instead of a wrong number.
     *
     * Emits 0.0 immediately on subscribe (before any Firestore data arrives)
     * purely so downstream `combine()` calls can tick — HomeScreen already
     * renders 0.0 as "جاري التحميل" (see priceOrLoading3), so this never
     * shows a wrong number, just a brief loading state if truly offline.
     */
    override fun observeCentralBankDollar(): Flow<Double> =
        firestore.collection("banks")
            .snapshotsAsFlow()
            .filter { snap -> !snap.metadata.isFromCache }
            .map { snap ->
                val candidates = snap.documents.filter { doc ->
                    val name = doc.getString("name") ?: doc.getString("bank") ?: ""
                    name.contains("المركزى") || name.contains("المركزي") || name.contains("Central") ||
                        name.contains("بنك مصر") ||
                        name.contains("الأهلي المصري")
                }

                val latest = candidates.maxByOrNull { doc ->
                    doc.getTimestamp("bankUpdatedAt")?.seconds ?: Long.MIN_VALUE
                }

                val sell = latest?.let {
                    (it.get("sell") as? Number)?.toDouble()
                        ?: it.getString("sell")?.replace(",", "")?.toDoubleOrNull()
                        ?: 0.0
                } ?: 0.0

                Log.d(
                    TAG,
                    "home dollar price = $sell (from ${latest?.getString("name") ?: latest?.getString("bank")})"
                )
                sell
            }
            .onStart { emit(0.0) }

    private fun isSameCalendarDay(millisA: Long, millisB: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = millisA }
        val calB = Calendar.getInstance().apply { timeInMillis = millisB }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }
}
