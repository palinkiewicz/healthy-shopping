package pl.dakil.healthyshopping.ui.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A short sideways shake each time [trigger] changes (except its initial 0): feedback that an input
 * was rejected. It answers the user's action, so it plays regardless of decorative motion.
 */
@Composable
fun Modifier.shakeOn(trigger: Int): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        offset.snapTo(0f)
        offset.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 1_200f), initialVelocity = 1_600f)
    }
    return graphicsLayer { translationX = offset.value }
}
