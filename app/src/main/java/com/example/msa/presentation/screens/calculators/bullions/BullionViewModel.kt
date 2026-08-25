package com.msa.android.presentation.screens.calculators.bullions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.model.BullionGramItem
import com.msa.android.domain.model.BullionMetalType
import com.msa.android.domain.repository.BullionRepository
import com.msa.android.domain.repository.MetalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

data class BullionState(
    val metals: List<BullionMetalType> = emptyList(),
    val companies: List<String> = emptyList(),
    val grams: List<BullionGramItem> = emptyList(),

    val selectedMetal: BullionMetalType? = null,
    val selectedCompany: String = "",
    val selectedGram: BullionGramItem? = null,

    // iOS: imageName starts "MSA", becomes company name on company select
    val imageName: String = "MSA",

    // Latest gold/silver prices from the main `metals` collection
    val goldBuy: Double = 0.0,
    val goldSell: Double = 0.0,
    val silverBuy: Double = 0.0,
    val silverSell: Double = 0.0,

    val metalPickerVisible: Boolean = false,
    val companyPickerVisible: Boolean = false,
    val gramPickerVisible: Boolean = false
)

@HiltViewModel
class BullionViewModel @Inject constructor(
    private val bullionRepo: BullionRepository,
    private val metalsRepo: MetalsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BullionState())
    val state: StateFlow<BullionState> = _state.asStateFlow()

    private var companiesJob: Job? = null
    private var gramsJob: Job? = null

    init {
        // iOS .onAppear { getMetal() }
        viewModelScope.launch {
            bullionRepo.observeMetals().collect { metals ->
                _state.update { it.copy(metals = metals) }
            }
        }
        // Gold/silver base prices for handleGramPrice()
        viewModelScope.launch {
            metalsRepo.observeMetals().collect { list ->
                val gold = list.firstOrNull { it.type == "gold" }
                val silver = list.firstOrNull { it.type == "silver" }
                _state.update {
                    it.copy(
                        goldBuy    = gold?.buyPrice?.toDoubleOrNull()    ?: 0.0,
                        goldSell   = gold?.salePrice?.toDoubleOrNull()   ?: 0.0,
                        silverBuy  = silver?.buyPrice?.toDoubleOrNull()  ?: 0.0,
                        silverSell = silver?.salePrice?.toDoubleOrNull() ?: 0.0
                    )
                }
            }
        }
    }

    fun showMetalPicker() = _state.update { it.copy(metalPickerVisible = true) }
    fun showCompanyPicker() =
        _state.update { if (it.selectedMetal != null) it.copy(companyPickerVisible = true) else it }
    fun showGramPicker() =
        _state.update { if (it.selectedCompany.isNotBlank()) it.copy(gramPickerVisible = true) else it }
    fun dismissPickers() = _state.update {
        it.copy(metalPickerVisible = false, companyPickerVisible = false, gramPickerVisible = false)
    }

    /** iOS Button(metal.name) { selectedMetal = metal; selectedCompany = ""; imageName = "MSA"; selectedGrame = nil; getCompanies(metalId) } */
    fun selectMetal(metal: BullionMetalType) {
        _state.update {
            it.copy(
                selectedMetal = metal,
                selectedCompany = "",
                selectedGram = null,
                imageName = "MSA",
                companies = emptyList(),
                grams = emptyList(),
                metalPickerVisible = false
            )
        }
        companiesJob?.cancel()
        gramsJob?.cancel()
        companiesJob = viewModelScope.launch {
            bullionRepo.observeCompanies(metal.id).collect { companies ->
                _state.update { it.copy(companies = companies) }
            }
        }
    }

    /** iOS Button(company) { selectedCompany = company; imageName = company; getgrames(...) } */
    fun selectCompany(company: String) {
        val metal = _state.value.selectedMetal ?: return
        _state.update {
            it.copy(
                selectedCompany = company,
                imageName = company,
                selectedGram = null,
                grams = emptyList(),
                companyPickerVisible = false
            )
        }
        gramsJob?.cancel()
        gramsJob = viewModelScope.launch {
            bullionRepo.observeGrams(metal.id, company).collect { grams ->
                _state.update { it.copy(grams = grams) }
            }
        }
    }

    fun selectGram(gram: BullionGramItem) {
        _state.update { it.copy(selectedGram = gram, gramPickerVisible = false) }
    }

    /**
     * 1:1 port of iOS handleGramePrice():
     *   var gramSalePrice = goldPrice.salePrice
     *   var gramBuyPrice  = goldPrice.buyPrice
     *   if selectedMetal.type == "Karat24" { sale *= 24/21; buy *= 24/21 }
     *   else if selectedMetal.type == "Silver" { sale = silverSell; buy = silverBuy }
     *   return (round(sale), round(buy))
     */
    fun handleGramPrice(): Pair<Double, Double> {
        val s = _state.value
        var sale = s.goldSell
        var buy = s.goldBuy
        when (s.selectedMetal?.type) {
            "Karat24" -> {
                sale = sale * 24.0 / 21.0
                buy = buy * 24.0 / 21.0
            }
            "Silver" -> {
                sale = s.silverSell
                buy = s.silverBuy
            }
        }
        return Pair(round(sale), round(buy))
    }
}
