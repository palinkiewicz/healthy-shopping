package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.data.repository.ProductRepository

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val products: List<SearchProduct>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

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

    private suspend fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading
        val result = repository.searchProducts(query)
        result.fold(
            onSuccess = { response ->
                _uiState.value = SearchUiState.Success(response.products)
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
