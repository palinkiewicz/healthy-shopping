package pl.dakil.healthyshopping.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

/**
 * Top app bar colors matching the screen background, so the bar blends
 * seamlessly with the content (no tinted band at the top).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun flatTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.background
)
