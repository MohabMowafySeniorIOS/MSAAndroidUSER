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
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which individual screen a maintenance flag belongs to.
 *
 * Each entry maps to its own Firestore fields on `appVersion/appVersion`
 * (see FirestoreConstants), so Home, the Dollar-prices tab and the
 * Fed-meetings screen are switched independently of each other and of the
 * app-wide `needMaintain`.
 */
enum class MaintainedScreen { HOME, DOLLAR, FEDERAL, NEWS, BULLIONS }

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
    val dollar: ScreenMaintenanceUi = ScreenMaintenanceUi(),
    val federal: ScreenMaintenanceUi = ScreenMaintenanceUi(),
    val news: ScreenMaintenanceUi = ScreenMaintenanceUi(),
    val bullions: ScreenMaintenanceUi = ScreenMaintenanceUi()
) {
    fun forScreen(screen: MaintainedScreen): ScreenMaintenanceUi = when (screen) {
        MaintainedScreen.HOME -> home
        MaintainedScreen.DOLLAR -> dollar
        MaintainedScreen.FEDERAL -> federal
        MaintainedScreen.NEWS -> news
        MaintainedScreen.BULLIONS -> bullions
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

    private companion object {
        /** مهلة إعادة الاشتراك بعد فشل مراقب Firestore */
        const val RETRY_DELAY_MS = 5_000L
    }

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
                    ),
                    federal = ScreenMaintenanceUi(
                        isUnderMaintenance = remote.federalMaintain,
                        title = remote.federalText.title(lang),
                        message = remote.federalText.message(lang)
                    ),
                    news = ScreenMaintenanceUi(
                        isUnderMaintenance = remote.newsMaintain,
                        title = remote.newsText.title(lang),
                        message = remote.newsText.message(lang)
                    ),
                    bullions = ScreenMaintenanceUi(
                        isUnderMaintenance = remote.bullionsMaintain,
                        title = remote.bullionsText.title(lang),
                        message = remote.bullionsText.message(lang)
                    )
                )
            }
                /*
                 * مراقب Firestore بيقفل بـ`close(err)` لو حصل خطأ
                 * (صلاحيات · شبكة · المستند اتمسح)، والـ`callbackFlow`
                 * بيرمي. من غير معالجة كان بيطلع من `viewModelScope`
                 * من غير أي معالج = التطبيق بيقفل.
                 *
                 * `retryWhen` بيعيد الاشتراك كل ٥ ثواني. `catch` لوحده
                 * ما كانش بيكفي: هو بينهي التدفّق، فآخر قيمة وصلت
                 * بتفضل مجمّدة للأبد — لو كانت «صيانة = نعم» المستخدم
                 * كان هيفضل على كارت الصيانة حتى بعد ما المشرف يقفلها.
                 */
                .retryWhen { error, _ ->
                    android.util.Log.w(
                        "ScreenMaintenance",
                        "تعذّر قراءة أعلام الصيانة — هنعيد المحاولة",
                        error
                    )
                    delay(RETRY_DELAY_MS)
                    true
                }
                .collect { _state.value = it }
        }
    }
}
