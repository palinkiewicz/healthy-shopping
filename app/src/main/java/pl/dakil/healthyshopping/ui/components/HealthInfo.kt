package pl.dakil.healthyshopping.ui.components

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.Nutrient
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbol
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.theme.HealthTone
import pl.dakil.healthyshopping.ui.theme.Tone
import pl.dakil.healthyshopping.ui.theme.tone

// ---------------------------------------------------------------------------------------------
// Ingredient harm levels (1 = beneficial … 5 = very harmful)
// ---------------------------------------------------------------------------------------------

data class HarmLevel(
    /** Plural group title ("Szkodliwe"). */
    @param:StringRes val groupLabel: Int,
    /** Sentence for a single ingredient ("Szkodliwy składnik"). */
    @param:StringRes val description: Int,
    val symbol: AppSymbol,
    val tone: HealthTone?,
)

fun harmLevel(level: Int?): HarmLevel = when (level) {
    5 -> HarmLevel(R.string.harm_group_5, R.string.harm_desc_5, AppSymbols.Dangerous, HealthTone.BAD)
    4 -> HarmLevel(R.string.harm_group_4, R.string.harm_desc_4, AppSymbols.Warning, HealthTone.BAD)
    3 -> HarmLevel(R.string.harm_group_3, R.string.harm_desc_3, AppSymbols.Help, HealthTone.CAUTION)
    2 -> HarmLevel(R.string.harm_group_2, R.string.harm_desc_2, AppSymbols.CheckCircle, HealthTone.MILD)
    1 -> HarmLevel(R.string.harm_group_1, R.string.harm_desc_1, AppSymbols.Eco, HealthTone.GOOD)
    else -> HarmLevel(R.string.harm_group_unknown, R.string.harm_desc_unknown, AppSymbols.Help, null)
}

/** The level's tone, or a neutral surface tone for unknown levels. */
@Composable
@ReadOnlyComposable
fun HarmLevel.toneOrNeutral(): Tone = tone?.tone() ?: Tone(
    MaterialTheme.colorScheme.surfaceContainerHighest,
    MaterialTheme.colorScheme.onSurfaceVariant,
)

// ---------------------------------------------------------------------------------------------
// Nutrients
// ---------------------------------------------------------------------------------------------

enum class NutrientKind(
    @param:StringRes val label: Int,
    /** Reference intake for an average adult (2000 kcal). */
    val dailyReference: Double,
    /** Which way is better when comparing products; null when neither is. */
    val lowerIsBetter: Boolean?,
) {
    ENERGY(R.string.nutrient_energy, 2000.0, true),
    FAT(R.string.nutrient_fat, 70.0, true),
    SATURATED(R.string.nutrient_saturated_short, 20.0, true),
    CARBS(R.string.nutrient_carbs, 260.0, null),
    SUGARS(R.string.nutrient_sugars, 90.0, true),
    FIBER(R.string.nutrient_fiber, 25.0, false),
    PROTEIN(R.string.nutrient_protein, 50.0, false),
    SALT(R.string.nutrient_salt, 6.0, true),
}

/** Recognises a nutrient by its Polish API name. More specific names are checked first. */
fun nutrientKindOf(name: String?): NutrientKind? {
    val n = name?.lowercase() ?: return null
    return when {
        "nasycone" in n -> NutrientKind.SATURATED
        "cukry" in n -> NutrientKind.SUGARS
        "energ" in n || "kalorie" in n -> NutrientKind.ENERGY
        "tłuszcz" in n -> NutrientKind.FAT
        "węglowodany" in n -> NutrientKind.CARBS
        "błonnik" in n -> NutrientKind.FIBER
        "białko" in n -> NutrientKind.PROTEIN
        "sól" in n -> NutrientKind.SALT
        else -> null
    }
}

fun List<Nutrient>.find(kind: NutrientKind): Nutrient? = firstOrNull { nutrientKindOf(it.name) == kind }

/** "12,5" / "<0.5" → 12.5 / 0.5; null when there is no number. */
fun parseNutrientNumber(value: String?): Double? =
    value?.replace(',', '.')?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull()

fun Nutrient.displayValue(): String? {
    val value = details?.value?.takeIf { it.isNotBlank() } ?: return null
    return listOfNotNull(value, details.unit?.takeIf { it.isNotBlank() }).joinToString(" ")
}

/** Share of the daily reference intake ([0, 1]) and how worrying it is (null: neutral). */
data class DailyShare(val fraction: Float, val tone: HealthTone?)

fun dailyShare(kind: NutrientKind?, value: Double?): DailyShare? {
    if (kind == null || value == null) return null
    val fraction = (value / kind.dailyReference).coerceIn(0.0, 1.0).toFloat()
    val tone = when (kind.lowerIsBetter) {
        false -> HealthTone.GOOD
        null -> null
        true -> if (kind == NutrientKind.ENERGY) null else when {
            fraction < 0.33f -> HealthTone.GOOD
            fraction < 0.66f -> HealthTone.CAUTION
            else -> HealthTone.BAD
        }
    }
    return DailyShare(fraction, tone)
}

/** Plain-text nutrition table for the clipboard. */
fun formatNutritionForCopy(product: ProductResponse, res: Resources): String {
    val nutrients = product.nutrientValues?.nutrients.orEmpty()
    val unit = product.nutrientValues?.per100Unit ?: "g"
    val missing = res.getString(R.string.value_missing_short)
    val lines = listOf(
        R.string.copy_energy to NutrientKind.ENERGY,
        R.string.copy_fat to NutrientKind.FAT,
        R.string.copy_saturated to NutrientKind.SATURATED,
        R.string.copy_carbs to NutrientKind.CARBS,
        R.string.copy_sugars to NutrientKind.SUGARS,
        R.string.copy_fiber to NutrientKind.FIBER,
        R.string.copy_protein to NutrientKind.PROTEIN,
        R.string.copy_salt to NutrientKind.SALT,
    ).map { (label, kind) -> res.getString(label, nutrients.find(kind)?.displayValue() ?: missing) }
    return (listOf(res.getString(R.string.nutrition_per_100, unit)) + lines).joinToString("\n")
}
