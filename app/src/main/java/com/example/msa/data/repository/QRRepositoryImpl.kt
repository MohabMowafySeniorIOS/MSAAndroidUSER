package com.msa.android.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.msa.android.domain.model.QRDocument
import com.msa.android.domain.repository.QRRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * QR verification + marking — straight port of iOS `QRRepository`:
 *
 *   Firestore.firestore()
 *     .collection("qr_codes")
 *     .document(id)
 *     .getDocument(...)
 *
 * • `verifyQR` returns null if the doc doesn't exist (= invalid QR).
 * • `markUsed` flips isUsed=true and stamps usedAt with server time.
 *
 * Collection name is `qr_codes`; if your Firestore uses a different one,
 * change the constant below.
 */
@Singleton
class QRRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QRRepository {

    override suspend fun verifyQR(id: String): QRDocument? {
        if (id.isBlank()) {
            Log.w(TAG, "verifyQR: blank id, returning null")
            return null
        }
        return try {
            // Log the EXACT Firestore path we're about to hit. If this doesn't
            // match what the user sees in the Firebase Console, they know to
            // change the COLLECTION constant in QRRepository.
            val path = "${QRRepository.COLLECTION}/$id"
            Log.d(TAG, "verifyQR: GET firestore path=$path")

            val snap = firestore
                .collection(QRRepository.COLLECTION)
                .document(id)
                .get()
                .await()

            if (!snap.exists()) {
                Log.w(TAG, "verifyQR: doc DOES NOT EXIST at $path")
                return null
            }
            val isUsed = snap.getBoolean("isUsed") ?: false
            val createdAt = snap.getTimestamp("createdAt")?.toDate()?.time
            val usedAt = snap.getTimestamp("usedAt")?.toDate()?.time
            val raw = snap.data
            Log.d(TAG, "verifyQR: doc EXISTS, full data=$raw")
            QRDocument(id = id, isUsed = isUsed, createdAt = createdAt, usedAt = usedAt)
        } catch (e: Exception) {
            Log.e(TAG, "verifyQR($id) FAILED with ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
    }

    override suspend fun markUsed(id: String) {
        if (id.isBlank()) return
        try {
            firestore.collection(QRRepository.COLLECTION).document(id)
                .update(
                    mapOf(
                        "isUsed" to true,
                        "usedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Log.d(TAG, "markUsed($id) ✓ — isUsed=true, usedAt=now()")
        } catch (e: Exception) {
            Log.e(TAG, "markUsed($id) FAILED: ${e.message}", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "QRRepo"
    }
}
