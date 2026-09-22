package com.msa.android.presentation.common

import androidx.lifecycle.ViewModel
import com.msa.android.data.source.network.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Tiny ViewModel that exposes the connectivity StateFlow from ConnectivityObserver.
 * Lives for the whole app session — one network callback for the entire lifecycle.
 */
@HiltViewModel
class NetworkStatusViewModel @Inject constructor(
    observer: ConnectivityObserver
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = observer.isConnected
}
