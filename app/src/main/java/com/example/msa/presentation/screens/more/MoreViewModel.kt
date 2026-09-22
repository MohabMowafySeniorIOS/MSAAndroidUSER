package com.msa.android.presentation.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.repository.AuthRepositoryImpl
import com.msa.android.domain.repository.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoreState(
    val versionName: String = "1.0",
    val isLoggedIn: Boolean = false,
    val userName: String? = null
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val versionRepo: VersionRepository,
    private val authRepo: AuthRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(MoreState())
    val state: StateFlow<MoreState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            versionRepo.observeVersion().collect { info ->
                _state.update { it.copy(versionName = info.version) }
            }
        }
        viewModelScope.launch {
            authRepo.isLoggedIn.collect { logged ->
                _state.update { it.copy(isLoggedIn = logged) }
            }
        }
        viewModelScope.launch {
            authRepo.userName.collect { name ->
                _state.update { it.copy(userName = name) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}
