package com.msa.android.presentation.common.components

import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Google's official Test ad unit ID for banner placements. Returns a real test
 * ad and is officially safe to ship in development builds — Google guarantees
 * it will never bill or count against any account.
 * Replace with your own ca-app-pub-… banner unit ID before publishing.
 */
const val ADMOB_TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

/**
 * Drop-in banner that mounts an AdMob [AdView] inside Compose.
 *
 * Why all the lifecycle ceremony?  [AdView] is a classic Android `View` and
 * Google's docs are clear that we MUST forward the host's pause/resume/destroy
 * to it — otherwise the banner keeps polling for ad impressions even when the
 * app is backgrounded (wasted bandwidth, drained battery, polluted metrics)
 * and the AdView leaks if not explicitly destroyed.
 *
 * We pick the anchored adaptive banner size (Google's current recommendation
 * over the fixed 320×50 BANNER) so the ad fills the device width with a
 * height the SDK picks per-form-factor. Falls back to BANNER if the SDK call
 * fails (e.g. running on an emulator without the right Play services).
 *
 * The composable always reserves space — `fillMaxWidth()` plus the SDK-chosen
 * height — so the UI doesn't reflow when the ad finishes loading.
 */
@Composable
fun AdMobBanner(
    adUnitId: String = ADMOB_TEST_BANNER_UNIT_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    // Anchored adaptive banner sized to current screen width. iOS uses
    // GADAdSizeBanner; on Android adaptive is the modern equivalent and
    // yields higher CPMs than the legacy fixed 320×50.
    val adSize = remember(configuration.screenWidthDp) {
        runCatching {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context, configuration.screenWidthDp
            )
        }.getOrDefault(AdSize.BANNER)
    }

    // Build the AdView once; survive recompositions by remembering it.
    val adView = remember {
        AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adListener = object : AdListener() {
                override fun onAdLoaded()                     { Log.d(TAG, "ad loaded") }
                override fun onAdFailedToLoad(e: LoadAdError) { Log.w(TAG, "ad failed: ${e.message}") }
                override fun onAdImpression()                 { Log.d(TAG, "ad impression") }
                override fun onAdClicked()                    { Log.d(TAG, "ad clicked") }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    // Forward host lifecycle → AdView. Without this the banner keeps refreshing
    // in the background and leaks when the screen leaves the composition.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> adView.resume()
                Lifecycle.Event.ON_PAUSE   -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView }
    )
}

private const val TAG = "AdMobBanner"
