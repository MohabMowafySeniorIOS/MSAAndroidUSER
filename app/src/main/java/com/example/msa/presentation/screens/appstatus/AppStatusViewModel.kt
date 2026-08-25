package com.msa.android.presentation.screens.appstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.BuildConfig
import com.msa.android.domain.model.VersionComparator
import com.msa.android.domain.repository.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppStatus {
    /** Still waiting on the first Firestore snapshot — keep showing content as-is. */
    data object Loading : AppStatus
    data object Ok : AppStatus
    data object NeedsUpdate : AppStatus
    data object Maintenance : AppStatus
}

/**
 * Watches `appVersion/appVersion` live (Firestore snapshot listener, not a
 * one-off fetch) and decides whether the app should be blocked:
 *
 *   • needMaintain == true                       → AppStatus.Maintenance
 *   • BuildConfig.VERSION_NAME < doc.version      → AppStatus.NeedsUpdate
 *   • otherwise                                   → AppStatus.Ok
 *
 * Maintenance takes priority over the update check. Because the listener is
 * live, flipping either flag in Firestore reaches anyone already on the Home
 * screen immediately — no app restart needed.
 */
@HiltViewModel
class AppStatusViewModel @Inject constructor(
    versionRepo: VersionRepository
) : ViewModel() {

    private val _status = MutableStateFlow<AppStatus>(AppStatus.Loading)
    val status: StateFlow<AppStatus> = _status.asStateFlow()

    init {
        viewModelScope.launch {
            versionRepo.observeVersion().collect { info ->
                _status.update {
                    when {
                        info.needMaintain -> AppStatus.Maintenance
                        VersionComparator.isOutdated(BuildConfig.VERSION_NAME, info.version) -> AppStatus.NeedsUpdate
                        else -> AppStatus.Ok
                    }
                }
            }
        }
    }
}
