package pl.dakil.healthyshopping.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.dakil.healthyshopping.data.model.SearchProduct

data class NutrientSetting(
    val id: String,
    val name: String,
    val defaultColor: String
)

val AVAILABLE_NUTRIENTS = listOf(
    NutrientSetting("energy_value", "Kalorie", "#FFFFFF"),
    NutrientSetting("fat", "Tłuszcz", "#2196F3"),
    NutrientSetting("including_saturated_fatty_acids", "Kwasy nasycone", "#BBDEFB"),
    NutrientSetting("carbohydrates", "Węglowodany", "#FFEB3B"),
    NutrientSetting("including_sugars", "Cukry", "#FF9800"),
    NutrientSetting("fiber", "Błonnik", "#795548"),
    NutrientSetting("protein", "Białko", "#4CAF50"),
    NutrientSetting("salt", "Sól", "#F44336")
)

enum class ThemePreset {
    SYSTEM, DYNAMIC, LIGHT, DARK, OLED, SEPIA, FOREST
}

enum class DetailsSection(val id: String, val label: String) {
    SCORE_CARD("score_card", "Wynik zdrowotny"),
    PRODUCT_TAGS("product_tags", "Tagi produktu"),
    NUTRITION_TABLE("nutrition_table", "Tabela wartości odżywczych"),
    INGREDIENTS_LIST("ingredients_list", "Pełna lista składników"),
    HARMFUL_INGREDIENTS("harmful_ingredients", "Szkodliwość składników")
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

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

    private val _visibleNutrients = MutableStateFlow(
        prefs.getStringSet("visible_nutrients", emptySet()) ?: emptySet()
    )
    val visibleNutrients: StateFlow<Set<String>> = _visibleNutrients.asStateFlow()

    private val _detailsSectionOrder = MutableStateFlow(
        loadDetailsSectionOrder()
    )
    val detailsSectionOrder: StateFlow<List<String>> = _detailsSectionOrder.asStateFlow()

    private val _hiddenDetailsSections = MutableStateFlow(
        prefs.getStringSet("hidden_details_sections", emptySet()) ?: emptySet()
    )
    val hiddenDetailsSections: StateFlow<Set<String>> = _hiddenDetailsSections.asStateFlow()

    private val _nutrientColors = MutableStateFlow(
        AVAILABLE_NUTRIENTS.associate { it.id to prefs.getString("nutrient_color_${it.id}", it.defaultColor)!! }
    )
    val nutrientColors: StateFlow<Map<String, String>> = _nutrientColors.asStateFlow()

    private val _recentlyViewedLimit = MutableStateFlow(
        prefs.getInt("recently_viewed_limit", 5)
    )
    val recentlyViewedLimit: StateFlow<Int> = _recentlyViewedLimit.asStateFlow()

    private val _recentlyViewedItems = MutableStateFlow(
        loadRecentlyViewed()
    )
    val recentlyViewedItems: StateFlow<List<SearchProduct>> = _recentlyViewedItems.asStateFlow()

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

    fun setNutrientVisible(id: String, visible: Boolean) {
        val current = _visibleNutrients.value.toMutableSet()
        if (visible) current.add(id) else current.remove(id)
        prefs.edit().putStringSet("visible_nutrients", current).apply()
        _visibleNutrients.value = current
    }

    fun setNutrientColor(id: String, color: String) {
        prefs.edit().putString("nutrient_color_$id", color).apply()
        _nutrientColors.update { it.toMutableMap().apply { put(id, color) } }
    }

    fun setRecentlyViewedLimit(limit: Int) {
        prefs.edit().putInt("recently_viewed_limit", limit).apply()
        _recentlyViewedLimit.value = limit
    }

    fun setDetailsSectionOrder(order: List<String>) {
        val serialized = json.encodeToString(order)
        prefs.edit().putString("details_section_order", serialized).apply()
        _detailsSectionOrder.value = order
    }

    fun setDetailsSectionVisible(id: String, visible: Boolean) {
        val current = _hiddenDetailsSections.value.toMutableSet()
        if (visible) current.remove(id) else current.add(id)
        prefs.edit().putStringSet("hidden_details_sections", current).apply()
        _hiddenDetailsSections.value = current
    }

    private fun loadDetailsSectionOrder(): List<String> {
        val serialized = prefs.getString("details_section_order", null)
        val defaultOrder = DetailsSection.values().map { it.id }
        if (serialized == null) return defaultOrder
        
        return try {
            val loaded = json.decodeFromString<List<String>>(serialized)
            // Ensure all sections are present (in case of app updates adding new sections)
            val missing = defaultOrder.filter { it !in loaded }
            loaded + missing
        } catch (e: Exception) {
            defaultOrder
        }
    }

    fun addToRecentlyViewed(product: SearchProduct) {
        if (product.ean == null) return
        
        val current = _recentlyViewedItems.value.toMutableList()
        current.removeAll { it.ean == product.ean }
        current.add(0, product)
        
        val trimmed = current.take(10)
        saveRecentlyViewed(trimmed)
        _recentlyViewedItems.value = trimmed
    }

    fun clearRecentlyViewed() {
        saveRecentlyViewed(emptyList())
        _recentlyViewedItems.value = emptyList()
    }

    private fun loadRecentlyViewed(): List<SearchProduct> {
        val serialized = prefs.getString("recently_viewed_items", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<SearchProduct>>(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveRecentlyViewed(items: List<SearchProduct>) {
        val serialized = json.encodeToString(items)
        prefs.edit().putString("recently_viewed_items", serialized).apply()
    }
}
