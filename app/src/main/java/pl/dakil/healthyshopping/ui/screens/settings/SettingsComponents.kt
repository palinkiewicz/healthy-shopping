package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt
import pl.dakil.healthyshopping.data.repository.AppColorTheme
import pl.dakil.healthyshopping.data.repository.DarkThemeOption
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption

private const val DISABLED_ALPHA = 0.38f

// ---------------------------------------------------------------------------
// Shared setting row composables (native ListItem look)
// ---------------------------------------------------------------------------

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp, end = 16.dp),
    )
}

/**
 * Shared base for every settings row, so switches, sliders and selects share
 * the exact same paddings and sizing. [trailing] is sized to its own content.
 */
@Composable
fun SettingRow(
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier,
            )
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(title) },
        supportingContent = if (summary != null || supporting != null) {
            {
                Column {
                    summary?.let { Text(it) }
                    supporting?.invoke()
                }
            }
        } else null,
        leadingContent = leading,
        trailingContent = trailing,
    )
}

@Composable
fun SwitchRow(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    SettingRow(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled) },
    )
}

@Composable
fun SliderRow(
    title: String,
    summary: String? = null,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    steps: Int = valueRange.last - valueRange.first - 1,
    valueLabel: (Int) -> String = { it.toString() },
    enabled: Boolean = true,
) {
    SettingRow(
        title = title,
        summary = summary,
        enabled = enabled,
        supporting = {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = steps,
                enabled = enabled,
            )
        },
        trailing = { Text(valueLabel(value)) },
    )
}

@Composable
fun <T> SelectRow(
    title: String,
    summary: String? = null,
    selectedLabel: String,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    SettingRow(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = { expanded = true },
        trailing = {
            // Menu anchored to this trailing box so it opens on the right.
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                    options.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onSelect(value)
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
    )
}

/** A row that opens another screen, marked with a trailing chevron. */
@Composable
fun NavigationRow(
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    SettingRow(
        title = title,
        summary = summary,
        onClick = onClick,
        leading = icon?.let { { Icon(it, contentDescription = null) } },
        trailing = {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

// ---------------------------------------------------------------------------
// Nutrient settings item
// ---------------------------------------------------------------------------

@Composable
fun NutrientSettingItem(
    name: String,
    isVisible: Boolean,
    colorHex: String,
    onToggleVisible: (Boolean) -> Unit,
    onColorChange: (String) -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }
    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }

    SettingRow(
        title = name,
        onClick = { onToggleVisible(!isVisible) },
        leading = { Checkbox(checked = isVisible, onCheckedChange = onToggleVisible) },
        trailing = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                    .clickable { showColorDialog = true }
            )
        },
    )

    if (showColorDialog) {
        val presetColors = listOf(
            "#FFFFFF", "#2196F3", "#03A9F4", "#00BCD4", "#009688",
            "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107",
            "#FF9800", "#FF5722", "#F44336", "#E91E63", "#9C27B0",
            "#673AB7", "#3F51B5", "#795548", "#9E9E9E", "#607D8B"
        )

        Dialog(onDismissRequest = { showColorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Wybierz kolor dla: $name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(presetColors.size) { index ->
                            val hex = presetColors[index]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (hex.lowercase() == colorHex.lowercase()) 3.dp else 1.dp,
                                        color = if (hex.lowercase() == colorHex.lowercase()) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onColorChange(hex)
                                        showColorDialog = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showColorDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Display name helpers
// ---------------------------------------------------------------------------

fun getColorThemeDisplayName(theme: AppColorTheme): String = when (theme) {
    AppColorTheme.ZDROWSKLAD -> "Zdrówskład"
    AppColorTheme.DYNAMIC -> "Kolory systemu"
    AppColorTheme.OCEAN -> "Ocean"
    AppColorTheme.LAVENDER -> "Lawenda"
    AppColorTheme.SUNSET -> "Zachód słońca"
    AppColorTheme.ROSE -> "Róża"
    AppColorTheme.TEAL -> "Morski"
}

fun getDarkThemeOptionDisplayName(option: DarkThemeOption): String = when (option) {
    DarkThemeOption.FOLLOW_SYSTEM -> "Systemowy"
    DarkThemeOption.LIGHT -> "Jasny"
    DarkThemeOption.DARK -> "Ciemny"
}

fun getAutoFocusOptionDisplayName(option: SearchAutoFocusOption): String = when (option) {
    SearchAutoFocusOption.NEVER       -> "Nigdy"
    SearchAutoFocusOption.EMPTY_FIELD -> "Gdy pole jest puste"
    SearchAutoFocusOption.ALWAYS      -> "Zawsze"
}
