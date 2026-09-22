package com.msa.android.data.source.firebase

/** Collection / document keys mirroring iOS app's Firestore structure. */
object FirestoreConstants {
    // Collections
    const val METALS         = "metals"
    const val METALS_OUNCE   = "metalsOunce"
    const val BANKS          = "banks"
    const val FAQ            = "FAQ"
    const val PAGES          = "pages"
    const val ONBOARDING     = "on_boarding"
    const val CONTACT_US     = "ContactUs"
    const val BULLIONS       = "bullions"

    // Documents
    const val APP_VERSION    = "appVersion"
    const val OUNCE_DOC      = "ounce"
    const val APP_SETTINGS   = "app_settings"

    // Field keys
    const val FIELD_TYPE      = "type"
    const val FIELD_NAME      = "name"
    const val FIELD_BUY       = "buyPrice"
    const val FIELD_SELL      = "salePrice"
    const val FIELD_UPDATED   = "updatedAt"
    const val FIELD_ORDER     = "order"
    const val FIELD_KARAT     = "karat"

    // ── Per-screen maintenance (doc: appVersion/appVersion) ──────────────────
    // Android-only flags. iOS reads "ioshomeMaintain" / "iosdollarMaintain" from
    // the same doc, so a screen can be taken down per platform.
    const val FIELD_HOME_MAINTAIN   = "homeMaintain"
    const val FIELD_DOLLAR_MAINTAIN = "dollarMaintain"

    /**
     * Fed-meetings screen.
     *
     * Spelled exactly as it is in the Firebase console — "Fedralli", capital F.
     * It is a typo, but the field already exists there and renaming it would
     * silently switch the screen back on for every installed build that still
     * reads the old name. The console is the source of truth; the code matches
     * it.
     *
     * Unlike home/dollar there is no "ios" twin: BOTH platforms read this one
     * field, so flipping it takes the screen down everywhere at once. Add
     * "iosFedralliMaintain" and split it here if per-platform control is ever
     * needed.
     */
    const val FIELD_FEDERAL_MAINTAIN = "FedralliMaintain"

    /**
     * الأخبار والسبائك.
     *
     * زي الفيدرالي: مفتاح واحد للمنصتين، مفيش نسخة `ios` في اللوحة.
     * الأسماء بحروفها الكبيرة زي ما هي في Firebase بالظبط —
     * `NewsMaintain` و`BullionsMaintain` (بحرف s).
     */
    const val FIELD_NEWS_MAINTAIN     = "NewsMaintain"
    const val FIELD_BULLIONS_MAINTAIN = "BullionsMaintain"

    // Copy is shared between platforms and split per language, matching the
    // "*_ar" / "*_en" convention used by the other documents. All optional —
    // missing values fall back to R.string.maintenance_screen_*.
    const val FIELD_HOME_MAINTAIN_TITLE     = "homeMaintainTitle"
    const val FIELD_HOME_MAINTAIN_MESSAGE   = "homeMaintainMessage"
    const val FIELD_DOLLAR_MAINTAIN_TITLE   = "dollarMaintainTitle"
    const val FIELD_DOLLAR_MAINTAIN_MESSAGE = "dollarMaintainMessage"
    const val FIELD_FEDERAL_MAINTAIN_TITLE   = "FedralliMaintainTitle"
    const val FIELD_FEDERAL_MAINTAIN_MESSAGE = "FedralliMaintainMessage"
    const val FIELD_NEWS_MAINTAIN_TITLE      = "NewsMaintainTitle"
    const val FIELD_NEWS_MAINTAIN_MESSAGE    = "NewsMaintainMessage"
    const val FIELD_BULLIONS_MAINTAIN_TITLE   = "BullionsMaintainTitle"
    const val FIELD_BULLIONS_MAINTAIN_MESSAGE = "BullionsMaintainMessage"

    const val SUFFIX_AR = "_ar"
    const val SUFFIX_EN = "_en"

    // Page docs (iOS uses these exact ids)
    const val PAGE_ABOUT      = "about_us"
    const val PAGE_PRIVACY    = "privacy_policy"
    const val PAGE_USAGE      = "usage_policy"
    const val PAGE_REFUND     = "refund_policy"

    // Central bank id (for the dollar calc)
    const val CENTRAL_BANK_ID = "central_bank"
}
