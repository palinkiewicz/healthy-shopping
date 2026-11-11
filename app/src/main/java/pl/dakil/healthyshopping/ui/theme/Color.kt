package pl.dakil.healthyshopping.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt

/**
 * A container color with its readable content color. Health tones are semantic (good / caution /
 * bad), so they deliberately stay the same across color presets; they still follow the app's own
 * dark mode through [LocalDarkTheme], never the system setting.
 */
@Immutable
data class Tone(val container: Color, val content: Color)

enum class HealthTone { GOOD, MILD, CAUTION, BAD }

private val LightTones = mapOf(
    HealthTone.GOOD to Tone(Color(0xFFCCF9D9), Color(0xFF00210C)),
    HealthTone.MILD to Tone(Color(0xFFEBF3CC), Color(0xFF151D00)),
    HealthTone.CAUTION to Tone(Color(0xFFFEEDD9), Color(0xFF291800)),
    HealthTone.BAD to Tone(Color(0xFFFFDAD6), Color(0xFF410002)),
)

private val DarkTones = mapOf(
    HealthTone.GOOD to Tone(Color(0xFF005327), Color(0xFFCCF9D9)),
    HealthTone.MILD to Tone(Color(0xFF3C4B00), Color(0xFFEBF3CC)),
    HealthTone.CAUTION to Tone(Color(0xFF5A3E00), Color(0xFFFEEDD9)),
    HealthTone.BAD to Tone(Color(0xFF93000A), Color(0xFFFFDAD6)),
)

/** Strong variants for marks on a surface (progress bars, small dots). */
private val LightAccents = mapOf(
    HealthTone.GOOD to Color(0xFF1E8F4B),
    HealthTone.MILD to Color(0xFF6B7A00),
    HealthTone.CAUTION to Color(0xFFB26A00),
    HealthTone.BAD to Color(0xFFBA1A1A),
)

private val DarkAccents = mapOf(
    HealthTone.GOOD to Color(0xFF7DDE9C),
    HealthTone.MILD to Color(0xFFC6D67A),
    HealthTone.CAUTION to Color(0xFFFFB95C),
    HealthTone.BAD to Color(0xFFFFB4AB),
)

@Composable
@ReadOnlyComposable
fun HealthTone.tone(): Tone = (if (LocalDarkTheme.current) DarkTones else LightTones).getValue(this)

@Composable
@ReadOnlyComposable
fun HealthTone.accent(): Color = (if (LocalDarkTheme.current) DarkAccents else LightAccents).getValue(this)

/** The API's ingredient highlight backgrounds mapped to the app's tones. */
@Composable
@ReadOnlyComposable
fun phraseTone(apiHex: String?): Tone {
    val tone = when (apiHex?.uppercase()) {
        "#D9F6E4" -> HealthTone.GOOD
        "#EBF3CC" -> HealthTone.MILD
        "#FEEDD9" -> HealthTone.CAUTION
        "#FFF3C4" -> HealthTone.BAD
        else -> null
    }
    if (tone != null) return tone.tone()
    val parsed = parseHexColor(apiHex) ?: Color.Transparent
    return if (LocalDarkTheme.current) Tone(parsed.copy(alpha = 0.3f), Color.White)
    else Tone(parsed.copy(alpha = 0.9f), Color.Black)
}

/** Parses an API "#RRGGBB" color, or null when it is missing or malformed. */
fun parseHexColor(hex: String?): Color? = hex?.let {
    runCatching { Color(it.toColorInt()) }.getOrNull()
}

/** Black or white, whichever reads better on [background]. */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.45f) Color(0xFF1A1C19) else Color.White
