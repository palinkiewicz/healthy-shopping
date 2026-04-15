package pl.dakil.healthyshopping.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemePreset {
    SYSTEM, DYNAMIC, LIGHT, DARK, OLED, SEPIA, FOREST
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _themePreset = MutableStateFlow(
        ThemePreset.valueOf(prefs.getString("theme_preset", ThemePreset.SYSTEM.name) ?: ThemePreset.SYSTEM.name)
    )
    val themePreset: StateFlow<ThemePreset> = _themePreset.asStateFlow()

    private val _showGroupedIngredients = MutableStateFlow(
        prefs.getBoolean("show_grouped_ingredients", false)
    )
    val showGroupedIngredients: StateFlow<Boolean> = _showGroupedIngredients.asStateFlow()

    private val _showNutritionProgressBars = MutableStateFlow(
        prefs.getBoolean("show_nutrition_progress_bars", true)
    )
    val showNutritionProgressBars: StateFlow<Boolean> = _showNutritionProgressBars.asStateFlow()

    private val _showHighlightedIngredients = MutableStateFlow(
        prefs.getBoolean("show_highlighted_ingredients", true)
    )
    val showHighlightedIngredients: StateFlow<Boolean> = _showHighlightedIngredients.asStateFlow()

    private val _showProductTags = MutableStateFlow(
        prefs.getBoolean("show_product_tags", true)
    )
    val showProductTags: StateFlow<Boolean> = _showProductTags.asStateFlow()

    private val _comparisonEans = MutableStateFlow(
        prefs.getStringSet("comparison_eans", emptySet()) ?: emptySet()
    )
    val comparisonEans: StateFlow<Set<String>> = _comparisonEans.asStateFlow()

    fun setThemePreset(preset: ThemePreset) {
        prefs.edit().putString("theme_preset", preset.name).apply()
        _themePreset.value = preset
    }

    fun setShowGroupedIngredients(enabled: Boolean) {
        prefs.edit().putBoolean("show_grouped_ingredients", enabled).apply()
        _showGroupedIngredients.value = enabled
    }

    fun setShowNutritionProgressBars(enabled: Boolean) {
        prefs.edit().putBoolean("show_nutrition_progress_bars", enabled).apply()
        _showNutritionProgressBars.value = enabled
    }

    fun setShowHighlightedIngredients(enabled: Boolean) {
        prefs.edit().putBoolean("show_highlighted_ingredients", enabled).apply()
        _showHighlightedIngredients.value = enabled
    }

    fun setShowProductTags(enabled: Boolean) {
        prefs.edit().putBoolean("show_product_tags", enabled).apply()
        _showProductTags.value = enabled
    }

    fun addToComparison(ean: String) {
        val current = _comparisonEans.value.toMutableSet()
        if (current.add(ean)) {
            prefs.edit().putStringSet("comparison_eans", current).apply()
            _comparisonEans.value = current
        }
    }

    fun removeFromComparison(ean: String) {
        val current = _comparisonEans.value.toMutableSet()
        if (current.remove(ean)) {
            prefs.edit().putStringSet("comparison_eans", current).apply()
            _comparisonEans.value = current
        }
    }

    fun clearComparison() {
        prefs.edit().remove("comparison_eans").apply()
        _comparisonEans.value = emptySet()
    }
}
