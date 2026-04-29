package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import pl.dakil.healthyshopping.data.repository.SettingsRepository
import pl.dakil.healthyshopping.data.repository.ThemePreset
import pl.dakil.healthyshopping.data.model.SearchProduct

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val themePreset: StateFlow<ThemePreset> = repository.themePreset

    val showGroupedIngredients: StateFlow<Boolean> = repository.showGroupedIngredients

    val showNutritionProgressBars: StateFlow<Boolean> = repository.showNutritionProgressBars

    val showHighlightedIngredients: StateFlow<Boolean> = repository.showHighlightedIngredients

    val showProductTags: StateFlow<Boolean> = repository.showProductTags
    val comparisonEans: StateFlow<Set<String>> = repository.comparisonEans
    val visibleNutrients: StateFlow<Set<String>> = repository.visibleNutrients
    val nutrientColors: StateFlow<Map<String, String>> = repository.nutrientColors
    val showTemporaryNutrient: StateFlow<Boolean> = repository.showTemporaryNutrient

    val recentlyViewedLimit: StateFlow<Int> = repository.recentlyViewedLimit
    val recentlyViewedItems: StateFlow<List<SearchProduct>> = repository.recentlyViewedItems

    val detailsSectionOrder: StateFlow<List<String>> = repository.detailsSectionOrder
    val hiddenDetailsSections: StateFlow<Set<String>> = repository.hiddenDetailsSections

    fun setThemePreset(preset: ThemePreset) {
        repository.setThemePreset(preset)
    }

    fun setShowGroupedIngredients(enabled: Boolean) {
        repository.setShowGroupedIngredients(enabled)
    }

    fun setShowNutritionProgressBars(enabled: Boolean) {
        repository.setShowNutritionProgressBars(enabled)
    }

    fun setShowHighlightedIngredients(enabled: Boolean) {
        repository.setShowHighlightedIngredients(enabled)
    }

    fun setShowProductTags(enabled: Boolean) {
        repository.setShowProductTags(enabled)
    }

    fun setShowTemporaryNutrient(enabled: Boolean) {
        repository.setShowTemporaryNutrient(enabled)
    }

    fun moveDetailsSection(fromIndex: Int, toIndex: Int) {
        val currentOrder = detailsSectionOrder.value.toMutableList()
        if (fromIndex in currentOrder.indices && toIndex in currentOrder.indices) {
            val item = currentOrder.removeAt(fromIndex)
            currentOrder.add(toIndex, item)
            repository.setDetailsSectionOrder(currentOrder)
        }
    }

    fun setDetailsSectionVisible(id: String, visible: Boolean) {
        repository.setDetailsSectionVisible(id, visible)
    }

    fun addToComparison(ean: String) {
        repository.addToComparison(ean)
    }

    fun removeFromComparison(ean: String) {
        repository.removeFromComparison(ean)
    }

    fun clearComparison() {
        repository.clearComparison()
    }

    fun setNutrientVisible(id: String, visible: Boolean) {
        repository.setNutrientVisible(id, visible)
    }

    fun setNutrientColor(id: String, color: String) {
        repository.setNutrientColor(id, color)
    }

    fun setRecentlyViewedLimit(limit: Int) {
        repository.setRecentlyViewedLimit(limit)
    }

    fun addToRecentlyViewed(product: SearchProduct) {
        repository.addToRecentlyViewed(product)
    }

    fun clearRecentlyViewed() {
        repository.clearRecentlyViewed()
    }
}
