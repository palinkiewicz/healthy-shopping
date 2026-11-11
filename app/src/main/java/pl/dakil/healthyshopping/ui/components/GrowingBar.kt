package pl.dakil.healthyshopping.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import pl.dakil.healthyshopping.ui.designsystem.UsageBar
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion

/**
 * A [UsageBar] (wavy or straight, per the Feel settings) that grows in from empty the first time it
 * appears — decorative, so it starts full with decorative motion off — then springs with its value.
 */
@Composable
fun GrowingBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val decorative = decorativeMotion
    var appeared by remember { mutableStateOf(!decorative) }
    LaunchedEffect(Unit) { appeared = true }
    UsageBar(
        fraction = if (appeared) fraction else 0f,
        color = color,
        trackColor = trackColor,
        modifier = modifier,
    )
}
