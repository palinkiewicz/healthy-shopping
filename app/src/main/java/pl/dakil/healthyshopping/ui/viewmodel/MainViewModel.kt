package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.data.repository.ProductRepository
import pl.dakil.healthyshopping.data.repository.SettingsRepository

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val product: ProductResponse) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class MainViewModel(
    private val repository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun getProduct(ean: String) {
        if (ean.isBlank()) {
            _uiState.value = ProductUiState.Error("Wpisz kod kreskowy")
            return
        }

        _uiState.value = ProductUiState.Loading

        viewModelScope.launch {
            val result = repository.getProduct(ean)
            result.fold(
                onSuccess = { product ->
                    _uiState.value = ProductUiState.Success(product)
                },
                onFailure = { error ->
                    _uiState.value = ProductUiState.Error(error.message ?: "Wystąpił nieznany błąd")
                }
            )
        }
    }

    fun addToHistory(product: ProductResponse) {
        val searchProduct = SearchProduct(
            ean = product.ean,
            name = product.name,
            score = product.score,
            harmfulLevel = product.harmfulLevel,
            image = product.image,
            nutrients = product.nutrientValues
        )
        settingsRepository.addToRecentlyViewed(searchProduct)
    }

    fun resetState() {
        _uiState.value = ProductUiState.Idle
    }
}
