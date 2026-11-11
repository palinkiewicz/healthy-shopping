package pl.dakil.healthyshopping.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.repository.AppColorTheme
import pl.dakil.healthyshopping.data.repository.DarkThemeOption
import pl.dakil.healthyshopping.data.repository.DetailsSection
import pl.dakil.healthyshopping.data.repository.ProgressStyle
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbol
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.theme.HealthTone

// UI-side labels for values the data layer defines. The data layer keeps its own ids; everything
// shown to the user comes from string resources here.

@Composable
fun AppColorTheme.label(): String = stringResource(
    when (this) {
        AppColorTheme.ZDROWSKLAD -> R.string.theme_zdrowsklad
        AppColorTheme.DYNAMIC -> R.string.theme_dynamic
        AppColorTheme.OCEAN -> R.string.theme_ocean
        AppColorTheme.LAVENDER -> R.string.theme_lavender
        AppColorTheme.SUNSET -> R.string.theme_sunset
        AppColorTheme.ROSE -> R.string.theme_rose
        AppColorTheme.TEAL -> R.string.theme_teal
    }
)

@Composable
fun DarkThemeOption.label(): String = stringResource(
    when (this) {
        DarkThemeOption.FOLLOW_SYSTEM -> R.string.dark_mode_system
        DarkThemeOption.LIGHT -> R.string.dark_mode_light
        DarkThemeOption.DARK -> R.string.dark_mode_dark
    }
)

@Composable
fun SearchAutoFocusOption.label(): String = stringResource(
    when (this) {
        SearchAutoFocusOption.NEVER -> R.string.auto_focus_never
        SearchAutoFocusOption.EMPTY_FIELD -> R.string.auto_focus_empty
        SearchAutoFocusOption.ALWAYS -> R.string.auto_focus_always
    }
)

@Composable
fun ProgressStyle.label(): String = stringResource(
    when (this) {
        ProgressStyle.WAVY -> R.string.settings_progress_style_wavy
        ProgressStyle.STRAIGHT -> R.string.settings_progress_style_straight
    }
)

@StringRes
fun DetailsSection.labelRes(): Int = when (this) {
    DetailsSection.SCORE_CARD -> R.string.section_score
    DetailsSection.PRODUCT_TAGS -> R.string.section_tags
    DetailsSection.NUTRITION_TABLE -> R.string.section_nutrition
    DetailsSection.INGREDIENTS_LIST -> R.string.section_ingredients
    DetailsSection.HARMFUL_INGREDIENTS -> R.string.section_harmful
}

val DetailsSection.symbol: AppSymbol
    get() = when (this) {
        DetailsSection.SCORE_CARD -> AppSymbols.HealthAndSafety
        DetailsSection.PRODUCT_TAGS -> AppSymbols.Sell
        DetailsSection.NUTRITION_TABLE -> AppSymbols.Nutrition
        DetailsSection.INGREDIENTS_LIST -> AppSymbols.ReceiptLong
        DetailsSection.HARMFUL_INGREDIENTS -> AppSymbols.Science
    }

fun detailsSectionFor(id: String): DetailsSection? = DetailsSection.entries.firstOrNull { it.id == id }

/** Name of one of the data layer's AVAILABLE_NUTRIENTS, by id. */
@StringRes
fun nutrientNameRes(id: String): Int = when (id) {
    "energy_value" -> R.string.nutrient_energy
    "fat" -> R.string.nutrient_fat
    "including_saturated_fatty_acids" -> R.string.nutrient_saturated
    "carbohydrates" -> R.string.nutrient_carbs
    "including_sugars" -> R.string.nutrient_sugars
    "fiber" -> R.string.nutrient_fiber
    "protein" -> R.string.nutrient_protein
    "salt" -> R.string.nutrient_salt
    else -> R.string.nutrient_unknown
}

/** A product tag from the API: its label and whether it is good or bad news (null: unknown tag). */
data class TagInfo(@param:StringRes val label: Int?, val tone: HealthTone?)

fun tagInfo(tag: String): TagInfo = when (tag) {
    "no_sugar_added" -> TagInfo(R.string.tag_no_sugar_added, HealthTone.GOOD)
    "high_fiber" -> TagInfo(R.string.tag_high_fiber, HealthTone.GOOD)
    "high_protein" -> TagInfo(R.string.tag_high_protein, HealthTone.GOOD)
    "not_recommended_for_overweight_people" -> TagInfo(R.string.tag_overweight, HealthTone.BAD)
    "not_recommended_for_people_with_high_cholesterol" -> TagInfo(R.string.tag_cholesterol, HealthTone.BAD)
    "not_recommended_for_people_with_diabetes" -> TagInfo(R.string.tag_diabetes, HealthTone.BAD)
    "not_recommended_for_people_with_hypertension" -> TagInfo(R.string.tag_hypertension, HealthTone.BAD)
    else -> TagInfo(null, null)
}
