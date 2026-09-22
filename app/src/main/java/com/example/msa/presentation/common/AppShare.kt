package com.msa.android.presentation.common

import android.content.Context
import android.content.Intent

/**
 * Opens the system "share" chooser with a localized message and the
 * Play-Store URL for this app. Called from every tab's header share button.
 *
 * The message is bilingual-friendly: Arabic line + URL.
 */
fun shareApp(context: Context) {
    val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"

    val message = buildString {
        append("تابع أسعار الذهب والفضة والدولار من تطبيق MSA")
        append("\n\n")
        append(playStoreUrl)
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(
        Intent.createChooser(intent, "مشاركة التطبيق").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
