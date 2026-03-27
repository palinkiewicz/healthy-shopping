package pl.dakil.healthyshopping.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val OledColorScheme = darkColorScheme(
    primary = md_theme_oled_primary,
    onPrimary = md_theme_oled_onPrimary,
    primaryContainer = md_theme_oled_primaryContainer,
    onPrimaryContainer = md_theme_oled_onPrimaryContainer,
    secondary = md_theme_oled_secondary,
    onSecondary = md_theme_oled_onSecondary,
    secondaryContainer = md_theme_oled_secondaryContainer,
    onSecondaryContainer = md_theme_oled_onSecondaryContainer,
    tertiary = md_theme_oled_tertiary,
    onTertiary = md_theme_oled_onTertiary,
    tertiaryContainer = md_theme_oled_tertiaryContainer,
    onTertiaryContainer = md_theme_oled_onTertiaryContainer,
    error = md_theme_oled_error,
    errorContainer = md_theme_oled_errorContainer,
    onError = md_theme_oled_onError,
    onErrorContainer = md_theme_oled_onErrorContainer,
    background = md_theme_oled_background,
    onBackground = md_theme_oled_onBackground,
    surface = md_theme_oled_surface,
    onSurface = md_theme_oled_onSurface,
)

private val SepiaColorScheme = lightColorScheme(
    primary = md_theme_sepia_primary,
    onPrimary = md_theme_sepia_onPrimary,
    primaryContainer = md_theme_sepia_primaryContainer,
    onPrimaryContainer = md_theme_sepia_onPrimaryContainer,
    secondary = md_theme_sepia_secondary,
    onSecondary = md_theme_sepia_onSecondary,
    secondaryContainer = md_theme_sepia_secondaryContainer,
    onSecondaryContainer = md_theme_sepia_onSecondaryContainer,
    tertiary = md_theme_sepia_tertiary,
    onTertiary = md_theme_sepia_onTertiary,
    tertiaryContainer = md_theme_sepia_tertiaryContainer,
    onTertiaryContainer = md_theme_sepia_onTertiaryContainer,
    error = md_theme_sepia_error,
    errorContainer = md_theme_sepia_errorContainer,
    onError = md_theme_sepia_onError,
    onErrorContainer = md_theme_sepia_onErrorContainer,
    background = md_theme_sepia_background,
    onBackground = md_theme_sepia_onBackground,
    surface = md_theme_sepia_surface,
    onSurface = md_theme_sepia_onSurface,
)

private val ForestColorScheme = darkColorScheme(
    primary = md_theme_forest_primary,
    onPrimary = md_theme_forest_onPrimary,
    primaryContainer = md_theme_forest_primaryContainer,
    onPrimaryContainer = md_theme_forest_onPrimaryContainer,
    secondary = md_theme_forest_secondary,
    onSecondary = md_theme_forest_onSecondary,
    secondaryContainer = md_theme_forest_secondaryContainer,
    onSecondaryContainer = md_theme_forest_onSecondaryContainer,
    tertiary = md_theme_forest_tertiary,
    onTertiary = md_theme_forest_onTertiary,
    tertiaryContainer = md_theme_forest_tertiaryContainer,
    onTertiaryContainer = md_theme_forest_onTertiaryContainer,
    error = md_theme_forest_error,
    errorContainer = md_theme_forest_errorContainer,
    onError = md_theme_forest_onError,
    onErrorContainer = md_theme_forest_onErrorContainer,
    background = md_theme_forest_background,
    onBackground = md_theme_forest_onBackground,
    surface = md_theme_forest_surface,
    onSurface = md_theme_forest_onSurface,
)

@Composable
fun HealthyShoppingTheme(
    themePreset: pl.dakil.healthyshopping.data.repository.ThemePreset = pl.dakil.healthyshopping.data.repository.ThemePreset.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = when (themePreset) {
        pl.dakil.healthyshopping.data.repository.ThemePreset.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isSystemDark) DarkColorScheme else LightColorScheme
            }
        }
        pl.dakil.healthyshopping.data.repository.ThemePreset.SYSTEM -> {
            if (isSystemDark) DarkColorScheme else LightColorScheme
        }
        pl.dakil.healthyshopping.data.repository.ThemePreset.LIGHT -> LightColorScheme
        pl.dakil.healthyshopping.data.repository.ThemePreset.DARK -> DarkColorScheme
        pl.dakil.healthyshopping.data.repository.ThemePreset.OLED -> OledColorScheme
        pl.dakil.healthyshopping.data.repository.ThemePreset.SEPIA -> SepiaColorScheme
        pl.dakil.healthyshopping.data.repository.ThemePreset.FOREST -> ForestColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}