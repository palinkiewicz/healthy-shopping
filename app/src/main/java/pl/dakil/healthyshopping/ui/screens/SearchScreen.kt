package pl.dakil.healthyshopping.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption
import pl.dakil.healthyshopping.ui.components.ErrorState
import pl.dakil.healthyshopping.ui.components.ProductImage
import pl.dakil.healthyshopping.ui.components.ScoreBadge
import pl.dakil.healthyshopping.ui.components.nutrientNameRes
import pl.dakil.healthyshopping.ui.designsystem.AppMenu
import pl.dakil.healthyshopping.ui.designsystem.AppMenuGroup
import pl.dakil.healthyshopping.ui.designsystem.AppMenuItem
import pl.dakil.healthyshopping.ui.designsystem.EmptyState
import pl.dakil.healthyshopping.ui.designsystem.bottomBarPadding
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.segmentedItemColors
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.theme.parseHexColor
import pl.dakil.healthyshopping.ui.theme.tabular
import pl.dakil.healthyshopping.ui.viewmodel.SearchSort
import pl.dakil.healthyshopping.ui.viewmodel.SearchUiState
import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SortOrder
import pl.dakil.healthyshopping.ui.viewmodel.SortType

/** How the user wants nutrient values previewed on each result row. */
private data class NutrientPreview(
    val visibleIds: Set<String>,
    val colors: Map<String, String>,
    val uniformWidth: Boolean,
    val width: Int,
)

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
    onProductClicked: (String) -> Unit,
) {
    val query by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val visibleNutrients by settingsViewModel.visibleNutrients.collectAsState()
    val nutrientColors by settingsViewModel.nutrientColors.collectAsState()
    val showTemporaryNutrient by settingsViewModel.showTemporaryNutrient.collectAsState()
    val uniformNutrientWidth by settingsViewModel.uniformNutrientWidth.collectAsState()
    val nutrientWidth by settingsViewModel.nutrientWidth.collectAsState()
    val autoFocus by settingsViewModel.searchAutoFocusOption.collectAsState()
    val comparisonEans by settingsViewModel.comparisonEans.collectAsState()
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current

    var fieldValue by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) {
        if (fieldValue.text != query) fieldValue = fieldValue.copy(text = query)
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        val shouldFocus = when (autoFocus) {
            SearchAutoFocusOption.ALWAYS -> true
            SearchAutoFocusOption.EMPTY_FIELD -> query.isEmpty()
            SearchAutoFocusOption.NEVER -> false
        }
        if (shouldFocus) {
            focusRequester.requestFocus()
            if (autoFocus == SearchAutoFocusOption.ALWAYS) {
                fieldValue = fieldValue.copy(selection = TextRange(fieldValue.text.length))
            }
        }
    }

    val preview = remember(visibleNutrients, sort, showTemporaryNutrient, nutrientColors, uniformNutrientWidth, nutrientWidth) {
        val sortedId = sort.nutrientId
        val ids = if (showTemporaryNutrient && sort.type == SortType.NUTRIENT && sortedId != null) {
            visibleNutrients + sortedId
        } else visibleNutrients
        NutrientPreview(ids, nutrientColors, uniformNutrientWidth, nutrientWidth)
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = Spacing.maxContentWidth).fillMaxWidth()) {
            TextField(
                value = fieldValue,
                onValueChange = {
                    fieldValue = it
                    if (it.text != query) viewModel.onSearchQueryChange(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.m, bottom = Spacing.xs)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { AppIcon(AppSymbols.Search, contentDescription = null) },
                trailingIcon = if (fieldValue.text.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                fieldValue = TextFieldValue("")
                                viewModel.onSearchQueryChange("")
                            },
                            shapes = IconButtonDefaults.shapes(),
                        ) { AppIcon(AppSymbols.Close, contentDescription = stringResource(R.string.action_clear)) }
                    }
                } else null,
                shape = CircleShape,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            AnimatedVisibility(
                visible = uiState is SearchUiState.Success && (uiState as SearchUiState.Success).products.size > 1,
                enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(),
            ) {
                SortRow(sort, onSortChange = { haptics.tick(); viewModel.setSort(it) })
            }
        }

        val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

        val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            transitionSpec = { fadeIn(fadeInSpec) togetherWith fadeOut(fadeOutSpec) },
            modifier = Modifier.fillMaxSize(),
            label = "searchState",
        ) { state ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when (state) {
                    is SearchUiState.Idle -> CenteredScroll {
                        val short = query.isNotBlank()
                        EmptyState(
                            symbol = if (short) AppSymbols.Search else AppSymbols.ManageSearch,
                            title = stringResource(if (short) R.string.search_keep_typing else R.string.search_start_title),
                            body = stringResource(if (short) R.string.search_keep_typing_body else R.string.search_start_body),
                        )
                    }
                    is SearchUiState.Loading -> LoadingIndicator(Modifier.padding(top = 96.dp).size(64.dp))
                    is SearchUiState.Error -> CenteredScroll { ErrorState(state.errorType) }
                    is SearchUiState.Success -> if (state.products.isEmpty()) {
                        CenteredScroll {
                            EmptyState(
                                symbol = AppSymbols.SearchOff,
                                title = stringResource(R.string.search_no_results),
                                body = stringResource(R.string.search_no_results_body),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.widthIn(max = Spacing.maxContentWidth).fillMaxSize(),
                            contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.s, bottom = bottomBarPadding(Spacing.xl)),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                        ) {
                            itemsIndexed(state.products, key = { i, p -> p.ean ?: "i$i" }) { index, product ->
                                ProductRow(
                                    product = product,
                                    index = index,
                                    count = state.products.size,
                                    preview = preview,
                                    sort = sort,
                                    isInComparison = product.ean != null && product.ean in comparisonEans,
                                    onClick = {
                                        focusManager.clearFocus()
                                        product.ean?.let(onProductClicked)
                                    },
                                    onToggleComparison = {
                                        product.ean?.let { ean ->
                                            if (ean in comparisonEans) settingsViewModel.removeFromComparison(ean)
                                            else settingsViewModel.addToComparison(ean)
                                        }
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredScroll(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = Spacing.xxl, bottom = bottomBarPadding(Spacing.xl)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
private fun SortRow(current: SearchSort, onSortChange: (SearchSort) -> Unit) {
    fun toggled(type: SortType, nutrientId: String?): SearchSort {
        val same = current.type == type && current.nutrientId == nutrientId
        val order = if (same && current.order == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
        return SearchSort(type = type, order = order, nutrientId = nutrientId)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        SortChip(
            label = stringResource(R.string.search_sort_score),
            selected = current.type == SortType.SCORE,
            order = current.order,
            onClick = { onSortChange(toggled(SortType.SCORE, null)) },
        )
        AVAILABLE_NUTRIENTS.forEach { nutrient ->
            SortChip(
                label = stringResource(nutrientNameRes(nutrient.id)),
                selected = current.type == SortType.NUTRIENT && current.nutrientId == nutrient.id,
                order = current.order,
                onClick = { onSortChange(toggled(SortType.NUTRIENT, nutrient.id)) },
            )
        }
    }
}

/** A sort chip whose arrow flips with a spring when the order reverses. */
@Composable
private fun SortChip(label: String, selected: Boolean, order: SortOrder, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (order == SortOrder.DESCENDING) 0f else 180f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "sortArrow",
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = CircleShape,
        leadingIcon = if (selected) {
            {
                AppIcon(
                    AppSymbols.ArrowDownward,
                    contentDescription = stringResource(
                        if (order == SortOrder.DESCENDING) R.string.cd_sort_descending else R.string.cd_sort_ascending
                    ),
                    modifier = Modifier.size(FilterChipDefaults.IconSize).graphicsLayer { rotationZ = rotation },
                )
            }
        } else null,
    )
}

@Composable
private fun ProductRow(
    product: SearchProduct,
    index: Int,
    count: Int,
    preview: NutrientPreview,
    sort: SearchSort,
    isInComparison: Boolean,
    onClick: () -> Unit,
    onToggleComparison: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    val shareTitle = stringResource(R.string.action_share)
    val shareText = buildString {
        product.name?.let { append(it).append("\n") }
        append("https://zdrowezakupy.org/product/${product.ean}")
    }

    Box(modifier) {
        SegmentedListItem(
            onClick = onClick,
            onLongClick = {
                haptics.longPress()
                menuOpen = true
            },
            onLongClickLabel = stringResource(R.string.cd_more_options),
            shapes = ListItemDefaults.segmentedShapes(index, count),
            colors = segmentedItemColors(),
            leadingContent = {
                ProductImage(
                    url = product.image?.url,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(56.dp),
                )
            },
            supportingContent = {
                NutrientPreviewRow(product, preview, sort)
            },
            trailingContent = {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ScoreBadge(product.score, size = 44.dp)
                    if (isInComparison) {
                        Box(
                            Modifier
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(
                                AppSymbols.CompareArrows,
                                contentDescription = stringResource(R.string.cd_in_comparison),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }
            },
        ) {
            Text(
                product.name ?: stringResource(R.string.product_unknown),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        AppMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            AppMenuGroup {
                AppMenuItem(
                    text = stringResource(if (isInComparison) R.string.comparison_remove else R.string.comparison_add),
                    symbol = if (isInComparison) AppSymbols.Delete else AppSymbols.AddChart,
                    onClick = {
                        if (isInComparison) haptics.toggle(false) else haptics.confirm()
                        onToggleComparison()
                        menuOpen = false
                    },
                )
                AppMenuItem(
                    text = shareTitle,
                    symbol = AppSymbols.Share,
                    enabled = product.ean != null,
                    onClick = {
                        menuOpen = false
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(send, shareTitle))
                    },
                )
            }
        }
    }
}

/** The nutrients the user picked, each as a value with a dot in the user's color for it. */
@Composable
private fun NutrientPreviewRow(product: SearchProduct, preview: NutrientPreview, sort: SearchSort) {
    val nutrients = product.nutrients?.nutrients?.filter { it.id in preview.visibleIds }.orEmpty()
    if (nutrients.isEmpty()) return
    FlowRow(
        modifier = Modifier.padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        nutrients.forEach { nutrient ->
            val color = parseHexColor(preview.colors[nutrient.id]) ?: MaterialTheme.colorScheme.outline
            val value = nutrient.details?.value?.takeIf { it.isNotBlank() }
            val text = if (value == null) "–" else "$value ${nutrient.details.unit.orEmpty()}".trim()
            val sorted = sort.type == SortType.NUTRIENT && sort.nutrientId == nutrient.id
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (preview.uniformWidth) Modifier.widthIn(min = preview.width.dp) else Modifier,
            ) {
                Box(Modifier.size(8.dp).background(color, CircleShape))
                Text(
                    text,
                    style = MaterialTheme.typography.labelMedium.tabular(),
                    fontWeight = if (sorted) FontWeight.Bold else FontWeight.Normal,
                    color = if (sorted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
        }
    }
}
