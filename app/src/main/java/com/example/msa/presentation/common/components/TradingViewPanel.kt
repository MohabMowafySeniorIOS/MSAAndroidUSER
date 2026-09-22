package com.msa.android.presentation.common.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color

/** نوع الأداة المعروضة */
enum class TVWidget(val key: String) {
    CHART("chart"),
    ANALYSIS("analysis")
}

/**
 * حاضنة widgets تريدنج فيو الرسمية.
 *
 * بتحمّل `assets/tradingview.html` وتحقن الإعدادات قبل التحميل، فالبيانات
 * بتيجي من مصادر السوق بتاعت تريدنج فيو مباشرة.
 *
 * الرمز `OANDA:XAUUSD` هو أونصة الذهب بالدولار — ده المعيار العالمي اللي
 * التحليل الفني بيتبني عليه. أسعار الجرام المصرية محلية ومفيهاش تاريخ
 * كافي لتحليل فني.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TradingViewPanel(
    widget: TVWidget,
    symbol: String,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    // نفس لون خلفية التطبيق عشان الأداة تندمج مع الشاشة
    val background = remember { Color(0xFF2E2020).toArgb() }
    val locale = if (isArabic) "ar_AE" else "en"

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(background)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // الأداة بتتحكم في مقاسها بنفسها، فمش عايزين تكبير يدوي
                    builtInZoomControls = false
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?, url: String?,
                        favicon: android.graphics.Bitmap?
                    ) {
                        // الإعدادات لازم توصل قبل ما سكربت الصفحة يشتغل
                        view?.evaluateJavascript(configScript(widget, symbol, locale), null)
                    }
                }

                loadUrl("file:///android_asset/tradingview.html")
            }
        },
        update = { view ->
            // تغيير المعدن أو اللغة بيعيد بناء الأداة بنفس الصفحة
            view.evaluateJavascript(configScript(widget, symbol, locale) + " render();", null)
        }
    )
}

private fun configScript(widget: TVWidget, symbol: String, locale: String): String =
    """window.MSA_CONFIG = {
        widget: '${widget.key}',
        symbol: '$symbol',
        locale: '$locale'
    };"""

/** رموز المعادن على تريدنج فيو */
object TVSymbols {
    const val GOLD = "OANDA:XAUUSD"
    const val SILVER = "OANDA:XAGUSD"
}
