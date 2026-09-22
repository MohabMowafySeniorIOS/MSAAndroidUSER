package com.msa.android.data.source.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 1:1 port of iOS GoldPriceScraper.swift / SilverPriceScraper.swift.
 *
 * iOS approach:
 *   • Create a hidden WKWebView with an iPhone User-Agent.
 *   • Load https://www.investing.com/currencies/xau-usd  (or  xag-usd  for silver).
 *   • Wait ~3 s for the JS bundle to render the price.
 *   • Evaluate `document.querySelector('[data-test="instrument-price-last"]').innerText`.
 *
 * Android port:
 *   • Same idea using android.webkit.WebView (must be created on Main thread).
 *   • Wait for `onPageFinished`, then poll the JS selector every 500 ms for up to
 *     10 s — the iOS hard-coded 3 s wait is unreliable on slower networks.
 *   • Returns the parsed Double or 0.0 on any failure.
 *
 * NOTE: WebView must run on the Android Main thread. The whole scrape coroutine
 *       is wrapped in `withContext(Dispatchers.Main)`.
 */
@Singleton
class InvestingScraper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "InvestingScraper"
        private const val USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1"
        private const val PAGE_LOAD_TIMEOUT_MS = 20_000L   // wait up to 20s for page to load
        private const val INITIAL_RENDER_WAIT_MS = 3_000L  // iOS waits 3 s
        private const val POLL_INTERVAL_MS = 500L          // re-query the DOM every 500 ms
        private const val POLL_MAX_ATTEMPTS = 14           // ≈7 s of polling after initial wait

        private const val PRICE_JS = """
            (function() {
                var el = document.querySelector('[data-test="instrument-price-last"]');
                return el ? el.innerText : null;
            })();
        """
    }

    suspend fun fetchGoldOuncePrice(): Double = scrape("xau-usd", label = "GOLD")
    suspend fun fetchSilverOuncePrice(): Double = scrape("xag-usd", label = "SILVER")

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun scrape(pair: String, label: String): Double =
        withContext(Dispatchers.Main) {
            val url = "https://www.investing.com/currencies/$pair"
            Log.d(TAG, "[$label] loading $url")

            val webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = USER_AGENT
                settings.loadsImagesAutomatically = false   // tiny speed-up: skip images
            }

            val pageDone = CompletableDeferred<Boolean>()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d(TAG, "[$label] onPageFinished: $url")
                    if (!pageDone.isCompleted) pageDone.complete(true)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    val failingUrl = request?.url?.toString() ?: ""
                    val mainFrame  = request?.isForMainFrame == true
                    // Sub-resource errors (ads, trackers …) fire constantly on investing.com
                    // and don't mean the page failed. Only fail on main-frame errors.
                    if (mainFrame) {
                        Log.e(TAG, "[$label] main-frame error: $failingUrl → ${error?.description}")
                        if (!pageDone.isCompleted) pageDone.complete(false)
                    }
                }
            }

            webView.loadUrl(url)

            val price = try {
                val ok = withTimeoutOrNull(PAGE_LOAD_TIMEOUT_MS) { pageDone.await() } ?: false
                if (!ok) {
                    Log.w(TAG, "[$label] page didn't finish loading — returning 0")
                    0.0
                } else {
                    // mimic iOS 3-second wait, then poll
                    delay(INITIAL_RENDER_WAIT_MS)
                    extractPriceWithPolling(webView, label)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[$label] scrape failed: ${e.message}", e)
                0.0
            } finally {
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.loadUrl("about:blank")
                webView.destroy()
            }

            Log.d(TAG, "[$label] ✅ scraped price = $price USD")
            price
        }

    /**
     * After the initial wait, queries the price selector. If it returns null
     * (the JS bundle is still rendering), retries every 500 ms up to ~7 s.
     */
    private suspend fun extractPriceWithPolling(webView: WebView, label: String): Double {
        repeat(POLL_MAX_ATTEMPTS) { attempt ->
            val raw = evaluateJs(webView)
            // evaluateJavascript returns JSON-encoded: "null"  or  "\"2,384.50\""
            if (raw != null && raw != "null") {
                val cleaned = raw.trim('"').replace(",", "").trim()
                val price = cleaned.toDoubleOrNull()
                if (price != null && price > 0.0) {
                    Log.d(TAG, "[$label] price found on attempt ${attempt + 1}: $price")
                    return price
                }
            }
            delay(POLL_INTERVAL_MS)
        }
        Log.w(TAG, "[$label] price selector never resolved — returning 0")
        return 0.0
    }

    private suspend fun evaluateJs(webView: WebView): String? =
        suspendCancellableCoroutine { cont ->
            webView.evaluateJavascript(PRICE_JS) { result ->
                if (cont.isActive) cont.resume(result)
            }
        }
}
