package pl.dakil.healthyshopping.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import pl.dakil.healthyshopping.data.repository.SettingsRepository
import pl.dakil.healthyshopping.data.repository.ThemePreset

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val themePreset: StateFlow<ThemePreset> = repository.themePreset

    val showGroupedIngredients: StateFlow<Boolean> = repository.showGroupedIngredients

    val showNutritionProgressBars: StateFlow<Boolean> = repository.showNutritionProgressBars

    val showHighlightedIngredients: StateFlow<Boolean> = repository.showHighlightedIngredients

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
}
