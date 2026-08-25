package com.msa.android.data.source.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live network status as a hot StateFlow that any consumer can collect.
 *
 * ─ What changed (June 2026) ─
 * We used to require `NET_CAPABILITY_VALIDATED` on top of `NET_CAPABILITY_INTERNET`.
 * `VALIDATED` is set only AFTER Android pings a Google captive-portal endpoint
 * to confirm the network actually reaches the public internet — a probe that
 * routinely takes ~1–3 seconds on cold-start Wi-Fi connections. During those
 * seconds the network has INTERNET but not VALIDATED, and we were (incorrectly)
 * reporting "offline". That's why users saw "لا يوجد اتصال" flash up on launch
 * even though their Wi-Fi was perfectly fine.
 *
 * New rule: trust `NET_CAPABILITY_INTERNET`. If the OS says this network claims
 * to provide internet, treat it as online. Two consequences worth knowing:
 *
 *   1. On a captive-portal Wi-Fi (e.g. airport, hotel) we may briefly report
 *      online before the user signs in. That's acceptable — our HTTP calls
 *      will fail and the "جاري التحميل" placeholders will stay put. Far less
 *      jarring than a full-screen overlay.
 *
 *   2. The initial state defaults to TRUE (optimistic). Otherwise the very
 *      first frame, before the NetworkCallback has had a chance to fire,
 *      would show the overlay even when everything is fine.
 *
 * The callback still fires on real connectivity changes and the safety-net
 * poll runs every 3 seconds to catch ROM-specific cases where callbacks are
 * delivered late or not at all (Xiaomi, Huawei, certain Samsung One-UI versions).
 */
@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Optimistic default — assume online until a callback or poll proves otherwise.
    // The previous default of `checkNow()` race-conditioned with the OS captive-
    // portal validation step and produced false-positive overlays on cold start.
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "[cb] onAvailable")
            // Trust the system — a network just came up; whether it's
            // validated yet is irrelevant for our use case.
            _isConnected.value = true
        }
        override fun onLost(network: Network) {
            Log.d(TAG, "[cb] onLost → false")
            // Lost is unambiguous: the active route is gone. Flip immediately.
            _isConnected.value = checkNow()
        }
        override fun onUnavailable() {
            Log.d(TAG, "[cb] onUnavailable → false")
            _isConnected.value = false
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            // INTERNET capability is the signal we care about. VALIDATED gets
            // set asynchronously by the OS and can lag by several seconds, so
            // we deliberately don't gate on it (see class-level note above).
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d(TAG, "[cb] caps changed → online=$hasInternet")
            _isConnected.value = hasInternet
        }
        override fun onLosing(network: Network, maxMsToLive: Int) {
            Log.d(TAG, "[cb] onLosing → recheck")
            _isConnected.value = checkNow()
        }
    }

    init {
        Log.d(TAG, "init: optimistic seed=true")
        runCatching { cm.registerDefaultNetworkCallback(callback) }
            .onFailure { Log.e(TAG, "register failed", it) }

        scope.launch {
            // Give the OS a brief moment before the first real check — registering
            // the callback above will normally fire `onAvailable` within tens of
            // ms, which is plenty to set the right value. Without this delay the
            // poll could correct from `true` (optimistic) → `false` (no active
            // network YET) → `true` (callback finally fires), flashing the overlay.
            delay(750)
            val initial = checkNow()
            if (_isConnected.value != initial) {
                Log.d(TAG, "[init] first probe corrected to $initial")
                _isConnected.value = initial
            }

            // Safety-net poll, catches ROM bugs where callbacks don't fire.
            while (isActive) {
                delay(3_000)
                val now = checkNow()
                if (_isConnected.value != now) {
                    Log.d(TAG, "[poll] correction: ${_isConnected.value} → $now")
                    _isConnected.value = now
                }
            }
        }
    }

    /**
     * Snapshot check — true when there's an active network that claims internet.
     * We don't require NET_CAPABILITY_VALIDATED because that capability is
     * latched asynchronously by Android's connectivity probe and can lag a
     * functional network by several seconds.
     */
    private fun checkNow(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object { private const val TAG = "Connectivity" }
}
