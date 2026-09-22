package com.msa.android.presentation.screens.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.ContactMessage
import com.msa.android.data.repository.ContactInfoRepository
import com.msa.android.data.source.network.dto.ContactDto
import com.msa.android.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HelpState(
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null,
    /** بيانات التواصل من لوحة التحكم — كانت مكتوبة ثابتة في الشاشة */
    val contact: ContactDto? = null
)

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val repo: ContactRepository,
    private val contactInfo: ContactInfoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HelpState())
    val state: StateFlow<HelpState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            contactInfo.observe().collect { info ->
                _state.update { it.copy(contact = info) }
            }
        }
    }

    fun setName(v: String) { _state.update { it.copy(name = v) } }
    fun setEmail(v: String) { _state.update { it.copy(email = v) } }
    fun setMessage(v: String) { _state.update { it.copy(message = v) } }

    fun send() {
        val s = _state.value
        if (s.name.isBlank() || s.email.isBlank() || s.message.isBlank()) {
            _state.update { it.copy(error = "fields_required") }
            return
        }
        _state.update { it.copy(sending = true, error = null) }
        viewModelScope.launch {
            val res = repo.send(ContactMessage(s.name, s.email, s.message))
            _state.update {
                if (res.isSuccess) it.copy(sending = false, sent = true,
                    name = "", email = "", message = "")
                else it.copy(sending = false, error = res.exceptionOrNull()?.message)
            }
        }
    }

    fun clearSent() = _state.update { it.copy(sent = false) }
}
