package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.data.repository.ProductRepository
import pl.dakil.healthyshopping.data.repository.SettingsRepository

enum class SortType {
    SCORE, NUTRIENT
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

data class SearchSort(
    val type: SortType = SortType.SCORE,
    val order: SortOrder = SortOrder.DESCENDING,
    val nutrientId: String? = null
)

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val products: List<SearchProduct>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sort = MutableStateFlow(SearchSort())
    val sort: StateFlow<SearchSort> = _sort.asStateFlow()

    private var allProducts: List<SearchProduct> = emptyList()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { query ->
                    val trimmed = query.trim()
                    if (trimmed.length < 3) {
                        _uiState.value = SearchUiState.Idle
                    } else {
                        performSearch(trimmed)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSort(sort: SearchSort) {
        _sort.value = sort
        applyCurrentSort()
    }

    private fun applyCurrentSort() {
        val currentState = _uiState.value
        if (currentState is SearchUiState.Success) {
            _uiState.value = SearchUiState.Success(sortProducts(allProducts, _sort.value))
        }
    }

    private fun sortProducts(products: List<SearchProduct>, sort: SearchSort): List<SearchProduct> {
        return when (sort.type) {
            SortType.SCORE -> {
                if (sort.order == SortOrder.ASCENDING) {
                    products.sortedBy { it.score?.value ?: Int.MAX_VALUE }
                } else {
                    products.sortedByDescending { it.score?.value ?: Int.MIN_VALUE }
                }
            }
            SortType.NUTRIENT -> {
                val nutrientId = sort.nutrientId ?: return products
                products.sortedWith { p1, p2 ->
                    val v1 = p1.nutrients?.nutrients?.find { it.id == nutrientId }?.details?.value?.replace(",", ".")?.toDoubleOrNull() 
                        ?: if (sort.order == SortOrder.ASCENDING) Double.MAX_VALUE else Double.MIN_VALUE
                    val v2 = p2.nutrients?.nutrients?.find { it.id == nutrientId }?.details?.value?.replace(",", ".")?.toDoubleOrNull() 
                        ?: if (sort.order == SortOrder.ASCENDING) Double.MAX_VALUE else Double.MIN_VALUE
                    if (sort.order == SortOrder.ASCENDING) v1.compareTo(v2) else v2.compareTo(v1)
                }
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading
        val result = productRepository.searchProducts(query)
        result.fold(
            onSuccess = { response ->
                allProducts = response.products
                _uiState.value = SearchUiState.Success(sortProducts(allProducts, _sort.value))
            },
            onFailure = { error ->
                _uiState.value = SearchUiState.Error(error.message ?: "Wystąpił błąd")
            }
        )
    }

    fun resetState() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Idle
    }
}
