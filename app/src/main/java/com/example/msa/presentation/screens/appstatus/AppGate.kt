package com.msa.android.presentation.screens.appstatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Wraps the Home screen's content. Watches AppStatusViewModel (live Firestore
 * listener on appVersion/appVersion) and swaps in MaintenanceScreen or
 * ForceUpdateScreen instead of [content] the moment either condition is true
 * — including while the user is already sitting on Home, no restart needed.
 *
 * While the very first snapshot hasn't arrived yet (AppStatus.Loading) we
 * just show [content] as normal, so there's no flash/blocking spinner on a
 * cold start with good connectivity.
 */
@Composable
fun AppGate(
    vm: AppStatusViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val status by vm.status.collectAsStateWithLifecycle()

    when (status) {
        AppStatus.Maintenance -> MaintenanceScreen()
        AppStatus.NeedsUpdate -> ForceUpdateScreen()
        AppStatus.Loading, AppStatus.Ok -> content()
    }
}
