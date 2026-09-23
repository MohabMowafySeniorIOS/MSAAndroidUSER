package com.msa.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.network.ConnectivityObserver
import com.msa.android.data.source.network.ApiLoadingState
import com.msa.android.presentation.common.components.ApiLoadingOverlay
import com.msa.android.presentation.common.components.NoInternetOverlay
import com.msa.android.presentation.navigation.MSANavGraph
import com.msa.android.presentation.theme.MSATheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.transformLatest
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var languagePreferences: LanguagePreferences
    @Inject lateinit var connectivityObserver: ConnectivityObserver

    // POST_NOTIFICATIONS permission launcher (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result ignored */ }

    override fun attachBaseContext(newBase: Context) {
        // Apply saved locale before context creation
        val lang = LanguagePreferences.staticGetLanguage(newBase)
        val context = applyLocaleToContext(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the splash a tiny bit (gives Compose first frame time, matches iOS slash anim)
        var keep = true
        splash.setKeepOnScreenCondition { keep }
        window.decorView.postDelayed({ keep = false }, 350)

        // Ask for notification permission on Android 13+ (Tiramisu) — required to
        // actually post FCM notifications. On older API levels it's granted at install.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val direction = if (languagePreferences.languageFlow.collectAsState(initial = "ar").value == "ar") {
                LayoutDirection.Rtl
            } else LayoutDirection.Ltr

            // ── Debounced connectivity stream ────────────────────────────
            //
            // The raw `connectivityObserver.isConnected` flow can briefly
            // emit `false` during cold start while the OS is still wiring up
            // Wi-Fi (DHCP / DNS / captive-portal probe / NetworkCallback
            // delivery order all race). The user's network IS fine — Android
            // just hasn't finished saying so yet.
            //
            // `transformLatest` is the elegant fix:
            //   - When `connected = true` arrives, emit it immediately.
            //   - When `connected = false` arrives, wait 2.5 s before
            //     emitting. If `true` comes during that window, the inner
            //     coroutine is cancelled and we never emit `false` at all.
            //
            // Net effect: the overlay shows only when the device is REALLY
            // offline for at least 2.5 s, never for a startup blip. Reconnects
            // dismiss the overlay instantly (the `if (connected)` branch has
            // no delay).
            val debouncedConnectivity = remember(connectivityObserver) {
                connectivityObserver.isConnected.transformLatest { connected ->
                    if (connected) {
                        emit(true)
                    } else {
                        delay(2_500)
                        emit(false)
                    }
                }
            }
            // initial = true so the first frame is NEVER the offline overlay.
            // The debounced flow will correct this within ~2.5 s if needed.
            val isConnected by debouncedConnectivity.collectAsState(initial = true)
            val apiLoading by ApiLoadingState.isLoading.collectAsState()

            MSATheme {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            MSANavGraph()

                            // No-internet overlay — Activity-level so it
                            // covers EVERYTHING the NavHost can render. Uses
                            // AnimatedVisibility for fade-in/out, fillMaxSize +
                            // zIndex(100f) to guarantee it paints on top of
                            // whatever screen is currently active.
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isConnected,
                                enter = androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.fadeOut(),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(100f)
                            ) {
                                NoInternetOverlay()
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = apiLoading,
                                enter = androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.fadeOut(),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(110f)
                            ) {
                                ApiLoadingOverlay()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyLocaleToContext(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
}
