package com.msa.android.domain.model

/**
 * Mirrors iOS `QR` struct in QRRepository. Stored in Firestore under the
 * `qr_codes` collection, doc id == the value embedded in the QR.
 */
data class QRDocument(
    val id: String,
    val isUsed: Boolean = false,
    val createdAt: Long? = null,
    val usedAt: Long? = null
)
