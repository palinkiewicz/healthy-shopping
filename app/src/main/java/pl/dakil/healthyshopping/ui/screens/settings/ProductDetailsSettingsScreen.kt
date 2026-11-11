package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.components.detailsSectionFor
import pl.dakil.healthyshopping.ui.components.labelRes
import pl.dakil.healthyshopping.ui.components.symbol
import pl.dakil.healthyshopping.ui.designsystem.BackButton
import pl.dakil.healthyshopping.ui.designsystem.CollapsingScaffold
import pl.dakil.healthyshopping.ui.designsystem.SegmentedGroup
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.segmentedItemShape
import pl.dakil.healthyshopping.ui.theme.SegmentedShapes
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ProductDetailsSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val grouped by viewModel.showGroupedIngredients.collectAsState()
    val gda by viewModel.showNutritionProgressBars.collectAsState()
    val highlighted by viewModel.showHighlightedIngredients.collectAsState()
    val tags by viewModel.showProductTags.collectAsState()
    val order by viewModel.detailsSectionOrder.collectAsState()
    val hidden by viewModel.hiddenDetailsSections.collectAsState()
    val haptics = rememberHaptics()

    // Local working copy so rows glide during a drag; the move is committed once on drag stop.
    var localOrder by remember { mutableStateOf(order) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(order) { if (!isDragging) localOrder = order }

    val listState = rememberLazyListState()
    // The first two lazy items are the header blocks, so section rows start at index 2.
    val headerItems = 2
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val f = from.index - headerItems
        val t = to.index - headerItems
        if (f in localOrder.indices && t in localOrder.indices) {
            localOrder = localOrder.toMutableList().apply { add(t, removeAt(f)) }
            haptics.frequentTick()
        }
    }

    CollapsingScaffold(
        title = stringResource(R.string.settings_details),
        navigationIcon = { BackButton(onNavigateBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                modifier = Modifier.widthIn(max = Spacing.maxContentWidth).fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.gutter, end = Spacing.gutter, top = Spacing.s, bottom = Spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(SegmentedShapes.gap),
            ) {
                item(key = "display") {
                    Column {
                        SectionHeader(stringResource(R.string.settings_section_display))
                        SegmentedGroup {
                            SwitchRow(
                                title = stringResource(R.string.settings_grouped),
                                summary = stringResource(R.string.settings_grouped_description),
                                checked = grouped,
                                onCheckedChange = viewModel::setShowGroupedIngredients,
                            )
                            SwitchRow(
                                title = stringResource(R.string.settings_gda),
                                summary = stringResource(R.string.settings_gda_description),
                                checked = gda,
                                onCheckedChange = viewModel::setShowNutritionProgressBars,
                            )
                            SwitchRow(
                                title = stringResource(R.string.settings_highlight),
                                summary = stringResource(R.string.settings_highlight_description),
                                checked = highlighted,
                                onCheckedChange = viewModel::setShowHighlightedIngredients,
                            )
                            SwitchRow(
                                title = stringResource(R.string.settings_tags),
                                summary = stringResource(R.string.settings_tags_description),
                                checked = tags,
                                onCheckedChange = viewModel::setShowProductTags,
                            )
                        }
                    }
                }
                item(key = "order_header") {
                    Column {
                        SectionHeader(stringResource(R.string.settings_section_order))
                        Text(
                            stringResource(R.string.settings_section_order_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.s),
                        )
                    }
                }
                itemsIndexed(localOrder, key = { _, id -> id }) { index, id ->
                    val section = detailsSectionFor(id) ?: return@itemsIndexed
                    val visible = id !in hidden
                    val label = stringResource(section.labelRes())
                    val moveUp = stringResource(R.string.cd_move_up)
                    val moveDown = stringResource(R.string.cd_move_down)
                    ReorderableItem(reorderState, key = id) { dragging ->
                        val itemScope = this
                        val elevation by animateDpAsState(if (dragging) 8.dp else 0.dp, label = "rowElevation")
                        val container by animateColorAsState(
                            if (dragging) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer,
                            label = "rowColor",
                        )
                        val shape = if (dragging) MaterialTheme.shapes.large else segmentedItemShape(index, localOrder.size)
                        Surface(
                            color = container,
                            shape = shape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation, shape)
                                .semantics {
                                    // Keyboard and screen-reader users reorder without dragging.
                                    customActions = buildList {
                                        if (index > 0) add(CustomAccessibilityAction(moveUp) {
                                            viewModel.moveDetailsSection(index, index - 1); true
                                        })
                                        if (index < localOrder.lastIndex) add(CustomAccessibilityAction(moveDown) {
                                            viewModel.moveDetailsSection(index, index + 1); true
                                        })
                                    }
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(start = Spacing.m, end = Spacing.s, top = Spacing.s, bottom = Spacing.s),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                            ) {
                                ShapedIcon(section.symbol, modifier = Modifier.alpha(if (visible) 1f else 0.5f), filled = visible)
                                Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = visible,
                                    onCheckedChange = {
                                        haptics.toggle(it)
                                        viewModel.setDetailsSectionVisible(id, it)
                                    },
                                    thumbContent = if (visible) {
                                        { AppIcon(AppSymbols.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                    } else null,
                                )
                                AppIcon(
                                    AppSymbols.DragIndicator,
                                    contentDescription = stringResource(R.string.cd_reorder),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = with(itemScope) {
                                        Modifier.longPressDraggableHandle(
                                            onDragStarted = {
                                                haptics.longPress()
                                                isDragging = true
                                                dragStartIndex = index
                                            },
                                            onDragStopped = {
                                                isDragging = false
                                                val to = localOrder.indexOf(id)
                                                if (dragStartIndex in localOrder.indices && to >= 0 && dragStartIndex != to) {
                                                    haptics.confirm()
                                                    viewModel.moveDetailsSection(dragStartIndex, to)
                                                }
                                                dragStartIndex = -1
                                            },
                                        )
                                    }.size(24.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
