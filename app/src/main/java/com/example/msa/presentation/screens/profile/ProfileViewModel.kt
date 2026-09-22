package com.msa.android.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.repository.AuthRepositoryImpl
import com.msa.android.data.repository.AuthResult
import com.msa.android.data.repository.ProfileApiRepository
import com.msa.android.data.repository.ProfileResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: String? = null,
    val savedMessage: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    /** بيتحوّل true بعد الخروج أو حذف الحساب عشان الشاشة تقفل */
    val signedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileApiRepository,
    private val authRepo: AuthRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = profileRepo.load()) {
                is ProfileResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        name = result.user.name,
                        email = result.user.email.orEmpty(),
                        phone = result.user.phone
                    )
                }
                is ProfileResult.Failure -> _state.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null, savedMessage = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null, savedMessage = null) }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, savedMessage = null) }
            when (val result = profileRepo.update(s.name.trim(), s.email.trim())) {
                is ProfileResult.Success -> _state.update {
                    it.copy(busy = false, savedMessage = "saved", name = result.user.name,
                            email = result.user.email.orEmpty())
                }
                is ProfileResult.Failure -> _state.update {
                    it.copy(busy = false, error = result.message)
                }
            }
        }
    }

    fun changePassword(current: String, new: String, confirm: String, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            val message = profileRepo.changePassword(current, new, confirm)
            _state.update { it.copy(busy = false) }
            onDone(message)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _state.update { it.copy(signedOut = true) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            when (val result = authRepo.deleteAccount()) {
                is AuthResult.Success -> _state.update { it.copy(busy = false, signedOut = true) }
                is AuthResult.Failure -> _state.update { it.copy(busy = false, error = result.message) }
            }
        }
    }
}
