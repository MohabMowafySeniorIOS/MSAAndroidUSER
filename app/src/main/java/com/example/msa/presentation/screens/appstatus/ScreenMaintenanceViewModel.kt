package com.msa.android.presentation.screens.appstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.repository.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which individual screen a maintenance flag belongs to.
 *
 * Each entry maps to its own pair of Firestore fields on `appVersion/appVersion`
 * (see FirestoreConstants), so Home and the Dollar-prices tab are switched
 * independently of each other and of the app-wide `needMaintain`.
 */
enum class MaintainedScreen { HOME, DOLLAR }

/**
 * One screen's resolved state, already narrowed to the user's language.
 *
 * [title] and [message] are blank when Firestore carries no copy — the UI then
 * shows the bundled `R.string.maintenance_screen_*` strings, which Android
 * picks from values/ (EN) or values-ar/ (AR) on its own.
 */
data class ScreenMaintenanceUi(
    val isUnderMaintenance: Boolean = false,
    val title: String = "",
    val message: String = ""
)

/** Both gated screens, resolved for the currently selected language. */
data class ScreenMaintenanceState(
    val home: ScreenMaintenanceUi = ScreenMaintenanceUi(),
    val dollar: ScreenMaintenanceUi = ScreenMaintenanceUi()
) {
    fun forScreen(screen: MaintainedScreen): ScreenMaintenanceUi = when (screen) {
        MaintainedScreen.HOME -> home
        MaintainedScreen.DOLLAR -> dollar
    }
}

/**
 * Live listener on `appVersion/appVersion`, combined with the language the user
 * picked so the remote copy comes back already in the right language.
 *
 * Deliberately separate from [AppStatusViewModel]: that one blocks the whole
 * app (AppGate swaps the entire Main destination). This one only feeds
 * [ScreenMaintenanceGate], which covers a single tab's content and leaves the
 * bottom bar usable so the user can move to the other tabs.
 *
 * Because it's a snapshot listener, flipping a flag in the Firebase console
 * shows/hides the overlay while the user is already sitting on the screen —
 * no restart. Switching language re-resolves the text the same way.
 */
@HiltViewModel
class ScreenMaintenanceViewModel @Inject constructor(
    versionRepo: VersionRepository,
    languagePrefs: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenMaintenanceState())
    val state: StateFlow<ScreenMaintenanceState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                versionRepo.observeScreenMaintenance(),
                languagePrefs.languageFlow
            ) { remote, lang ->
                ScreenMaintenanceState(
                    home = ScreenMaintenanceUi(
                        isUnderMaintenance = remote.homeMaintain,
                        title = remote.homeText.title(lang),
                        message = remote.homeText.message(lang)
                    ),
                    dollar = ScreenMaintenanceUi(
                        isUnderMaintenance = remote.dollarMaintain,
                        title = remote.dollarText.title(lang),
                        message = remote.dollarText.message(lang)
                    )
                )
            }.collect { _state.value = it }
        }
    }
}
