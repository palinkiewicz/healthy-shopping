package pl.dakil.healthyshopping.ui.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import pl.dakil.healthyshopping.ui.theme.LocalMotionPreferences
import pl.dakil.healthyshopping.ui.theme.tabular
import java.util.Locale
import kotlin.math.roundToLong

private val NumberPattern = Regex("""(\d+)(?:([.,])(\d+))?""")

/**
 * Shows [text], counting its first number up from zero the first time it appears ("12,5 g" runs
 * 0,0 → 12,5 keeping its decimals and separator). With animated numbers off it simply shows [text].
 * Figures are tabular so the value doesn't jitter sideways while it counts.
 */
@Composable
fun CountUpText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    val tabularStyle = style.tabular()
    val match = NumberPattern.find(text)
    if (!LocalMotionPreferences.current.animatedNumbers || match == null) {
        Text(text, modifier = modifier, style = tabularStyle, color = color, fontWeight = fontWeight, maxLines = 1)
        return
    }
    val whole = match.groupValues[1]
    val separator = match.groupValues[2].ifEmpty { "." }
    val fraction = match.groupValues[3]
    val target = "$whole.${fraction.ifEmpty { "0" }}".toDoubleOrNull() ?: 0.0

    val progress = remember(text) { Animatable(0f) }
    val spec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    LaunchedEffect(text) { progress.animateTo(1f, spec) }

    val current = target * progress.value
    val formatted = if (fraction.isEmpty()) {
        current.roundToLong().toString()
    } else {
        String.format(Locale.ROOT, "%.${fraction.length}f", current).replace(".", separator)
    }
    Text(
        text.replaceRange(match.range, formatted),
        modifier = modifier,
        style = tabularStyle,
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
    )
}
