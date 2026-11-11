package pl.dakil.healthyshopping.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.data.repository.ProgressStyle
import pl.dakil.healthyshopping.ui.theme.LocalMotionPreferences

/**
 * Whether progress bars are drawn wavy (the default) rather than straight, per the Feel settings.
 * Custom wavy bars honour it by flattening their amplitude and stopping their wave.
 */
val wavyProgress: Boolean
    @Composable @ReadOnlyComposable get() = LocalMotionPreferences.current.progressStyle == ProgressStyle.WAVY

/**
 * A usage meter: an expressive wavy bar that springs to each new [fraction] instead of jumping.
 * The wave stays still (amplitude only, no travel), so a live value doesn't animate continuously.
 * With the straight progress style chosen in the Feel settings it is a flat bar, still springing.
 */
@Composable
fun UsageBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "usage",
    )
    if (!wavyProgress) {
        LinearProgressIndicator(
            progress = { animated },
            modifier = modifier.fillMaxWidth(),
            color = color,
            trackColor = trackColor,
        )
        return
    }
    LinearWavyProgressIndicator(
        progress = { animated },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = trackColor,
        amplitude = { WavyProgressIndicatorDefaults.indicatorAmplitude(it) },
        wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
        waveSpeed = 0.dp,
    )
}

/**
 * A linear progress bar in the style chosen in the Feel settings, wavy or straight: determinate
 * with a [progress], indeterminate without one.
 */
@Composable
fun AppLinearProgress(modifier: Modifier = Modifier, progress: (() -> Float)? = null) {
    val straight = !wavyProgress
    when {
        straight && progress != null -> LinearProgressIndicator(progress = progress, modifier = modifier)
        straight -> LinearProgressIndicator(modifier = modifier)
        progress != null -> LinearWavyProgressIndicator(progress = progress, modifier = modifier)
        else -> LinearWavyProgressIndicator(modifier = modifier)
    }
}
