package pl.dakil.healthyshopping.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.ErrorType
import pl.dakil.healthyshopping.data.model.Score
import pl.dakil.healthyshopping.ui.designsystem.EmptyState
import pl.dakil.healthyshopping.ui.designsystem.MorphShape
import pl.dakil.healthyshopping.ui.designsystem.PolygonShape
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.theme.LocalMotionPreferences
import pl.dakil.healthyshopping.ui.theme.contentColorOn
import pl.dakil.healthyshopping.ui.theme.parseHexColor
import pl.dakil.healthyshopping.ui.theme.tabular
import kotlin.math.roundToInt

private val badgeShape by lazy { PolygonShape(MaterialShapes.Cookie9Sided) }
private val heroMorph by lazy { Morph(MaterialShapes.Circle, MaterialShapes.Cookie12Sided) }

/**
 * The product's health score on a cookie in the API's score color. The [hero] variant blooms from a
 * circle into a cookie with a settling turn (decorative) and counts up to its value (animated numbers).
 */
@Composable
fun ScoreBadge(
    score: Score?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    hero: Boolean = false,
) {
    val parsed = parseHexColor(score?.color)
    val container = parsed ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val content = if (parsed != null) contentColorOn(parsed) else MaterialTheme.colorScheme.onSurfaceVariant
    val value = score?.value
    val style = when {
        size >= 88.dp -> MaterialTheme.typography.displayMediumEmphasized
        size >= 64.dp -> MaterialTheme.typography.headlineMediumEmphasized
        size >= 48.dp -> MaterialTheme.typography.titleLargeEmphasized
        else -> MaterialTheme.typography.titleSmallEmphasized
    }.tabular()
    val description = stringResource(R.string.cd_score, value?.toString() ?: "?")

    if (!hero) {
        Box(
            modifier = modifier
                .size(size)
                .background(container, badgeShape)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Text(value?.toString() ?: "?", style = style, color = content, maxLines = 1)
        }
        return
    }

    val decorative = decorativeMotion
    val bloom = remember { Animatable(if (decorative) 0f else 1f) }
    val bloomSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { bloom.animateTo(1f, bloomSpec) }

    // Counts up from zero the first time the badge appears, unless animated numbers are off.
    val animatedNumbers = LocalMotionPreferences.current.animatedNumbers
    val target = (value ?: 0).toFloat()
    val shown = remember { Animatable(if (animatedNumbers) 0f else target) }
    val countSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    LaunchedEffect(target, animatedNumbers) {
        if (animatedNumbers) shown.animateTo(target, countSpec) else shown.snapTo(target)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = (1f - bloom.value) * -90f }
            .clip(MorphShape(heroMorph, bloom.value))
            .background(container)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (value == null) "?" else shown.value.roundToInt().toString(),
            style = style,
            color = content,
            maxLines = 1,
            // Counter-rotate so the number stays upright while the badge turns.
            modifier = Modifier.graphicsLayer { rotationZ = (1f - bloom.value) * 90f },
        )
    }
}

/**
 * A product photo on a white card (packshots have white backgrounds, so they sit naturally on it in
 * both themes), with a symbol while it is missing or failed to load.
 */
@Composable
fun ProductImage(
    url: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentDescription: String? = null,
) {
    var failed by remember(url) { mutableStateOf(url.isNullOrBlank()) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (failed) MaterialTheme.colorScheme.surfaceContainerHighest else Color.White),
        contentAlignment = Alignment.Center,
    ) {
        if (failed) {
            AppIcon(
                AppSymbols.ImageNotSupported,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                onState = { if (it is AsyncImagePainter.State.Error) failed = true },
                modifier = Modifier.fillMaxSize().padding(4.dp),
            )
        }
    }
}

/** A failed request: an error-tinted empty state explaining what went wrong, with an optional retry. */
@Composable
fun ErrorState(
    errorType: ErrorType,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val (symbol, title, body) = when (errorType) {
        ErrorType.CONNECTION -> Triple(AppSymbols.CloudOff, R.string.error_connection_title, R.string.error_connection_body)
        ErrorType.NOT_FOUND -> Triple(AppSymbols.SearchOff, R.string.error_not_found_title, R.string.error_not_found_body)
        ErrorType.UNKNOWN -> Triple(AppSymbols.Error, R.string.error_unknown_title, R.string.error_unknown_body)
    }
    EmptyState(
        symbol = symbol,
        title = stringResource(title),
        body = stringResource(body),
        polygon = MaterialShapes.Cookie9Sided,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier,
        actions = onRetry?.let {
            {
                Button(onClick = it, shapes = ButtonDefaults.shapes()) {
                    AppIcon(AppSymbols.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(stringResource(R.string.action_retry), modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
                }
            }
        },
    )
}
