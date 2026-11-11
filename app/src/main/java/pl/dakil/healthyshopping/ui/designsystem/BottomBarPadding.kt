package pl.dakil.healthyshopping.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Height of the bottom navigation bar, including the system navigation-bar inset beneath it.
 *
 * The bar is drawn *over* the full-height NavHost instead of in a Scaffold's bottomBar slot. A
 * Scaffold would pad the NavHost by the bar's height, and since the bar's slide-out is a draw-only
 * offset that padding stays until the exit animation ends, so a screen without the bar showed an
 * empty strip where the bar had been, then jumped. Overlaid, pushed screens get the full height
 * right away (the bar slides away over them), and tab screens reserve the room themselves.
 */
val LocalBottomBarPadding = compositionLocalOf { 0.dp }

/** Bottom padding a tab screen needs so its last item scrolls clear of the bar, plus [extra]. */
@Composable
@ReadOnlyComposable
fun bottomBarPadding(extra: Dp = 0.dp): Dp = LocalBottomBarPadding.current + extra
