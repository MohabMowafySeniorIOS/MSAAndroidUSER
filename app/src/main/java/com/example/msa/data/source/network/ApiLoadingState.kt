package com.msa.android.data.source.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicInteger

object ApiLoadingState {
    private val activeRequests = AtomicInteger(0)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun begin() { _isLoading.value = activeRequests.incrementAndGet() > 0 }

    fun end() {
        val remaining = activeRequests.updateAndGet { (it - 1).coerceAtLeast(0) }
        _isLoading.value = remaining > 0
    }
}

class ApiLoadingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        ApiLoadingState.begin()
        return try { chain.proceed(chain.request()) } finally { ApiLoadingState.end() }
    }
}
