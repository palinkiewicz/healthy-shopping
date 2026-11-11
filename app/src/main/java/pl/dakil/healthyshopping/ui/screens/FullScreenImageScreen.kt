package pl.dakil.healthyshopping.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.theme.Spacing

private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2.5f

/** The product photo on black: pinch to zoom and pan, double-tap to zoom in or back out. */
@Composable
fun FullScreenImageScreen(
    imageUrl: String,
    onBackClicked: () -> Unit,
) {
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val spring = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val offsetSpring = MaterialTheme.motionScheme.defaultSpatialSpec<Offset>()

    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val transformState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        scope.launch {
            val newScale = (scale.value * zoomChange).coerceIn(1f, MAX_ZOOM)
            val zoom = newScale / scale.value
            // Keep the point under the fingers in place while zooming (the layer scales about its centre).
            val fromCenter = centroid - Offset(viewport.width / 2f, viewport.height / 2f)
            val newOffset = fromCenter * (1f - zoom) + offset.value * zoom + panChange
            scale.snapTo(newScale)
            offset.snapTo(if (newScale == 1f) Offset.Zero else newOffset)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.cd_product_photo),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { viewport = it }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tap ->
                        haptics.tick()
                        scope.launch {
                            if (scale.value > 1f) {
                                launch { scale.animateTo(1f, spring) }
                                offset.animateTo(Offset.Zero, offsetSpring)
                            } else {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                launch { scale.animateTo(DOUBLE_TAP_ZOOM, spring) }
                                offset.animateTo((center - tap) * (DOUBLE_TAP_ZOOM - 1f), offsetSpring)
                            }
                        }
                    })
                }
                .transformable(transformState)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offset.value.x
                    translationY = offset.value.y
                },
        )

        FilledIconButton(
            onClick = onBackClicked,
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.16f),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(Spacing.l),
        ) {
            AppIcon(AppSymbols.Close, contentDescription = stringResource(R.string.action_close))
        }
    }
}
