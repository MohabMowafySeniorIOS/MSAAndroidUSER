package com.msa.android.presentation.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.AssetType
import com.msa.android.domain.model.PortfolioItem
import com.msa.android.domain.repository.PortfolioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddItemState(
    val type: AssetType = AssetType.GOLD_21K,
    val quantityText: String = "",
    val totalPriceText: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val quantity: Double   get() = quantityText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val totalPrice: Double get() = totalPriceText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val isValid: Boolean   get() = quantity > 0 && totalPrice > 0
}

@HiltViewModel
class AddPortfolioItemViewModel @Inject constructor(
    private val repo: PortfolioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddItemState())
    val state: StateFlow<AddItemState> = _state.asStateFlow()

    fun setType(type: AssetType)        = _state.update { it.copy(type = type) }
    fun setQuantity(text: String)       = _state.update { it.copy(quantityText = sanitize(text)) }
    fun setTotalPrice(text: String)     = _state.update { it.copy(totalPriceText = sanitize(text)) }
    fun setPurchaseDate(epoch: Long)    = _state.update { it.copy(purchaseDate = epoch) }
    fun setNotes(text: String)          = _state.update { it.copy(notes = text) }

    fun save() {
        val s = _state.value
        if (!s.isValid || s.saving) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                repo.addItem(
                    PortfolioItem(
                        id = UUID.randomUUID().toString(),
                        type = s.type,
                        quantity = s.quantity,
                        purchaseTotalPrice = s.totalPrice,
                        purchaseDate = s.purchaseDate,
                        notes = s.notes.takeIf { it.isNotBlank() }
                    )
                )
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, error = e.message) }
            }
        }
    }

    private fun sanitize(text: String): String {
        val cleaned = text.filter { it.isDigit() || it == '.' || it == ',' }
        val first = cleaned.indexOfFirst { it == '.' || it == ',' }
        return if (first < 0) cleaned
        else cleaned.substring(0, first + 1) +
             cleaned.substring(first + 1).filter { it.isDigit() }
    }
}
