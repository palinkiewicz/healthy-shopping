package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.data.repository.ProductRepository
import pl.dakil.healthyshopping.data.repository.SettingsRepository

sealed interface ComparisonUiState {
    data object Idle : ComparisonUiState
    data object Loading : ComparisonUiState
    data class Success(val products: List<ProductResponse>) : ComparisonUiState
    data class Error(val message: String) : ComparisonUiState
}

class ComparisonViewModel(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ComparisonUiState>(ComparisonUiState.Idle)
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.comparisonEans.collect { eans ->
                if (eans.isEmpty()) {
                    _uiState.value = ComparisonUiState.Success(emptyList())
                } else {
                    refreshComparison(eans)
                }
            }
        }
    }

    fun refreshComparison(eans: Set<String>? = null) {
        val targetEans = eans ?: settingsRepository.comparisonEans.value
        if (targetEans.isEmpty()) {
            _uiState.value = ComparisonUiState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = ComparisonUiState.Loading
            try {
                val deferredResults = targetEans.map { ean ->
                    async { productRepository.getProduct(ean) }
                }
                val results = deferredResults.awaitAll()
                
                val failed = results.filter { it.isFailure }
                if (failed.isNotEmpty()) {
                    val errorMessage = failed.first().exceptionOrNull()?.localizedMessage ?: "Nieznany błąd"
                    _uiState.value = ComparisonUiState.Error("Wystąpił błąd podczas pobierania danych produktów: $errorMessage")
                    return@launch
                }

                val products = results.map { it.getOrThrow() }
                _uiState.value = ComparisonUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = ComparisonUiState.Error("Wystąpił błąd systemowy: ${e.localizedMessage}")
            }
        }
    }

    fun clearComparison() {
        settingsRepository.clearComparison()
    }
}
