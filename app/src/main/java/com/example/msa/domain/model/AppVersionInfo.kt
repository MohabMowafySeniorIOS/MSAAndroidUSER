package com.msa.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Mirrors the real Firestore doc `appVersion/appVersion`:
 *   { version: "1.3", needMaintain: false }
 *
 * `version` is compared against BuildConfig.VERSION_NAME (see VersionComparator)
 * to decide whether to show the mandatory update screen; `needMaintain` gates
 * the maintenance screen. Both are watched live (snapshot listener) so either
 * one can be flipped remotely without needing an app restart.
 */
@Parcelize
data class AppVersionInfo(
    val version: String = "1.0",
    val needMaintain: Boolean = false
) : Parcelable

/**
 * Remote copy for one maintenance screen, in both languages.
 *
 * Follows the `*_ar` / `*_en` convention already used across the app's
 * Firestore documents (title_ar, name_en, …). Every field is optional: an empty
 * result tells the UI to fall back to the bundled `R.string.maintenance_screen_*`
 * resources, which are themselves translated via values/ and values-ar/.
 */
@Parcelize
data class MaintenanceText(
    val titleAr: String = "",
    val titleEn: String = "",
    val messageAr: String = "",
    val messageEn: String = ""
) : Parcelable {

    /** Title for [lang] ("ar"/"en"), falling back to the other language. */
    fun title(lang: String): String =
        if (lang == "en") titleEn.ifBlank { titleAr } else titleAr.ifBlank { titleEn }

    /** Message for [lang] ("ar"/"en"), falling back to the other language. */
    fun message(lang: String): String =
        if (lang == "en") messageEn.ifBlank { messageAr } else messageAr.ifBlank { messageEn }
}

/**
 * Per-screen maintenance, read from the SAME doc `appVersion/appVersion`.
 *
 * `homeMaintain` and `dollarMaintain` are Android-only (iOS reads its own
 * `ioshomeMaintain` / `iosdollarMaintain`), so those two screens can be taken
 * down on one platform without touching the other. The rest are shared:
 *
 *   homeMaintain     : Boolean  → blocks the Home tab only
 *   dollarMaintain   : Boolean  → blocks the Dollar-prices (banks) tab only
 *   FedralliMaintain : Boolean  → blocks the Fed-meetings screen
 *   NewsMaintain     : Boolean  → blocks the News tab only
 *   BullionsMaintain : Boolean  → blocks the Bullions tab only
 *
 * `FedralliMaintain`, `NewsMaintain` and `BullionsMaintain` are the exceptions
 * to the per-platform rule above: they have no "ios" twin in the console, so
 * both apps read the same field and the screen goes down everywhere at once.
 *
 * Copy is remote-editable per language so the wording can change from the
 * Firebase console without shipping a build:
 *
 *   homeMaintainTitle_ar   / homeMaintainTitle_en
 *   homeMaintainMessage_ar / homeMaintainMessage_en
 *   dollarMaintainTitle_ar   / dollarMaintainTitle_en
 *   dollarMaintainMessage_ar / dollarMaintainMessage_en
 *   FedralliMaintainTitle_ar   / FedralliMaintainTitle_en
 *   FedralliMaintainMessage_ar / FedralliMaintainMessage_en
 *   NewsMaintainTitle_ar       / NewsMaintainTitle_en
 *   NewsMaintainMessage_ar     / NewsMaintainMessage_en
 *   BullionsMaintainTitle_ar   / BullionsMaintainTitle_en
 *   BullionsMaintainMessage_ar / BullionsMaintainMessage_en
 *
 * This is independent of the app-wide `needMaintain`, which still blocks
 * everything through AppGate.
 */
@Parcelize
data class ScreenMaintenance(
    val homeMaintain: Boolean = false,
    val homeText: MaintenanceText = MaintenanceText(),
    val dollarMaintain: Boolean = false,
    val dollarText: MaintenanceText = MaintenanceText(),
    val federalMaintain: Boolean = false,
    val federalText: MaintenanceText = MaintenanceText(),
    val newsMaintain: Boolean = false,
    val newsText: MaintenanceText = MaintenanceText(),
    val bullionsMaintain: Boolean = false,
    val bullionsText: MaintenanceText = MaintenanceText()
) : Parcelable

/** Compares dotted version strings ("1.10" vs "1.9") numerically, part by part. */
object VersionComparator {
    /** True if [current] is strictly older than [latest]. */
    fun isOutdated(current: String, latest: String): Boolean {
        val currentParts = current.trim().split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.trim().split(".").map { it.toIntOrNull() ?: 0 }
        val size = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until size) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (c != l) return c < l
        }
        return false
    }
}

@Parcelize
data class ContactMessage(
    val name: String,
    val email: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
