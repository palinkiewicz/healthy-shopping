package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption
import pl.dakil.healthyshopping.ui.components.label
import pl.dakil.healthyshopping.ui.components.nutrientNameRes
import pl.dakil.healthyshopping.ui.designsystem.FormSheet
import pl.dakil.healthyshopping.ui.designsystem.MorphShape
import pl.dakil.healthyshopping.ui.designsystem.PolygonShape
import pl.dakil.healthyshopping.ui.designsystem.SegmentedGroup
import pl.dakil.healthyshopping.ui.designsystem.checkableItemColors
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion
import pl.dakil.healthyshopping.ui.designsystem.decorativeSpec
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.pressScale
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.segmentedInnerShapes
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.theme.contentColorOn
import pl.dakil.healthyshopping.ui.theme.parseHexColor
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

private val PresetColors = listOf(
    "#FFFFFF", "#2196F3", "#03A9F4", "#00BCD4", "#009688",
    "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107",
    "#FF9800", "#FF5722", "#F44336", "#E91E63", "#9C27B0",
    "#673AB7", "#3F51B5", "#795548", "#9E9E9E", "#607D8B",
)

@Composable
fun SearchSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val autoFocus by viewModel.searchAutoFocusOption.collectAsState()
    val showTemporaryNutrient by viewModel.showTemporaryNutrient.collectAsState()
    val uniformWidth by viewModel.uniformNutrientWidth.collectAsState()
    val nutrientWidth by viewModel.nutrientWidth.collectAsState()
    val visibleNutrients by viewModel.visibleNutrients.collectAsState()
    val nutrientColors by viewModel.nutrientColors.collectAsState()

    var colorPickerFor by rememberSaveable { mutableStateOf<String?>(null) }

    SettingsSubscreen(stringResource(R.string.settings_search), onNavigateBack) {
        SectionHeader(stringResource(R.string.settings_section_general))
        SegmentedGroup {
            SelectRow(
                title = stringResource(R.string.settings_auto_focus),
                summary = stringResource(R.string.settings_auto_focus_description),
                selectedLabel = autoFocus.label(),
                options = SearchAutoFocusOption.entries.map { it to it.label() },
                onSelect = viewModel::setSearchAutoFocusOption,
            )
            SwitchRow(
                title = stringResource(R.string.settings_temporary_nutrient),
                summary = stringResource(R.string.settings_temporary_nutrient_description),
                checked = showTemporaryNutrient,
                onCheckedChange = viewModel::setShowTemporaryNutrient,
            )
            SwitchRow(
                title = stringResource(R.string.settings_uniform_width),
                summary = stringResource(R.string.settings_uniform_width_description),
                checked = uniformWidth,
                onCheckedChange = viewModel::setUniformNutrientWidth,
            )
            if (uniformWidth) {
                SliderRow(
                    title = stringResource(R.string.settings_nutrient_width),
                    summary = stringResource(R.string.settings_nutrient_width_description),
                    value = nutrientWidth,
                    onValueChange = viewModel::setNutrientWidth,
                    valueRange = 32..128,
                    steps = 11,
                    valueLabel = { "${it}dp" },
                )
            }
        }

        SectionHeader(stringResource(R.string.settings_preview_nutrients))
        Text(
            stringResource(R.string.settings_preview_nutrients_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.s),
        )
        SegmentedGroup {
            AVAILABLE_NUTRIENTS.forEach { nutrient ->
                NutrientRow(
                    name = stringResource(nutrientNameRes(nutrient.id)),
                    visible = nutrient.id in visibleNutrients,
                    colorHex = nutrientColors[nutrient.id] ?: nutrient.defaultColor,
                    onVisibleChange = { viewModel.setNutrientVisible(nutrient.id, it) },
                    onPickColor = { colorPickerFor = nutrient.id },
                )
            }
        }
    }

    colorPickerFor?.let { id ->
        val nutrient = AVAILABLE_NUTRIENTS.firstOrNull { it.id == id } ?: return@let
        ColorPickerSheet(
            name = stringResource(nutrientNameRes(id)),
            selectedHex = nutrientColors[id] ?: nutrient.defaultColor,
            onSelect = { viewModel.setNutrientColor(id, it); colorPickerFor = null },
            onDismiss = { colorPickerFor = null },
        )
    }
}

/** A nutrient's visibility on the result list: the row toggles it, its color cookie opens the palette. */
@Composable
private fun NutrientRow(
    name: String,
    visible: Boolean,
    colorHex: String,
    onVisibleChange: (Boolean) -> Unit,
    onPickColor: () -> Unit,
) {
    val haptics = rememberHaptics()
    val color = parseHexColor(colorHex) ?: Color.Gray
    val pickDescription = stringResource(R.string.cd_pick_color, name)
    val swatchShape = remember { PolygonShape(MaterialShapes.Cookie9Sided) }
    SegmentedListItem(
        checked = visible,
        onCheckedChange = { haptics.toggle(it); onVisibleChange(it) },
        shapes = segmentedInnerShapes(),
        colors = checkableItemColors(),
        leadingContent = {
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .pressScale(interaction, 0.88f)
                    .clip(swatchShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, swatchShape)
                    .clickable(interaction, indication = null, role = Role.Button) { onPickColor() }
                    .semantics { contentDescription = pickDescription },
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(AppSymbols.Palette, contentDescription = null, tint = contentColorOn(color), modifier = Modifier.size(18.dp))
            }
        },
        supportingContent = { Text(stringResource(R.string.settings_nutrient_color_hint)) },
        trailingContent = {
            Switch(
                checked = visible,
                onCheckedChange = null,
                thumbContent = if (visible) {
                    { AppIcon(AppSymbols.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else null,
            )
        },
    ) { Text(name) }
}

private val swatchMorph by lazy { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }

@Composable
private fun ColorPickerSheet(
    name: String,
    selectedHex: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val haptics = rememberHaptics()
    FormSheet(
        title = stringResource(R.string.settings_color_for, name),
        symbol = AppSymbols.Palette,
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            maxItemsInEachRow = 5,
        ) {
            PresetColors.forEach { hex ->
                val isSelected = hex.equals(selectedHex, ignoreCase = true)
                val progress by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = decorativeSpec(MaterialTheme.motionScheme.defaultSpatialSpec()),
                    label = "colorMorph",
                )
                val spin = if (decorativeMotion) 30f else 0f
                val color = parseHexColor(hex) ?: Color.Gray
                val interaction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .pressScale(interaction, 0.88f)
                        .graphicsLayer { rotationZ = progress * spin }
                        .clip(MorphShape(swatchMorph, progress))
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MorphShape(swatchMorph, progress))
                        .semantics { selected = isSelected }
                        .clickable(interaction, indication = null, role = Role.RadioButton) {
                            if (!isSelected) haptics.tick()
                            onSelect(hex)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        AppIcon(
                            AppSymbols.Check,
                            contentDescription = null,
                            tint = contentColorOn(color),
                            modifier = Modifier.graphicsLayer { rotationZ = -progress * spin },
                        )
                    }
                }
            }
        }
    }
}
