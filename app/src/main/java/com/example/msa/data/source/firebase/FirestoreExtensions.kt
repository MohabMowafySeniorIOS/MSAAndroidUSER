package com.msa.android.data.source.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "FirestoreFlow"

/**
 * Realtime listener for Firestore collection queries as Flow.
 * Logs errors so a missing index or permission denial is visible in Logcat
 * instead of silently swallowing it.
 */
fun Query.snapshotsAsFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snap, err ->
        if (err != null) {
            Log.e(TAG, "Query error: ${err.message}", err)
            close(err)
            return@addSnapshotListener
        }
        if (snap != null) trySend(snap)
    }
    awaitClose { registration.remove() }
}

/** Realtime listener for Firestore document references as Flow. */
fun DocumentReference.snapshotsAsFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snap, err ->
        if (err != null) {
            Log.e(TAG, "Document error (${this@snapshotsAsFlow.path}): ${err.message}", err)
            close(err)
            return@addSnapshotListener
        }
        if (snap != null) trySend(snap)
    }
    awaitClose { registration.remove() }
}
