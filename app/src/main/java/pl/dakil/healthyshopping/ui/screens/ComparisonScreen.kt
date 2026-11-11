package pl.dakil.healthyshopping.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.ui.components.ErrorState
import pl.dakil.healthyshopping.ui.components.NutrientKind
import pl.dakil.healthyshopping.ui.components.ProductImage
import pl.dakil.healthyshopping.ui.components.ScoreBadge
import pl.dakil.healthyshopping.ui.components.displayValue
import pl.dakil.healthyshopping.ui.components.find
import pl.dakil.healthyshopping.ui.components.harmLevel
import pl.dakil.healthyshopping.ui.components.parseNutrientNumber
import pl.dakil.healthyshopping.ui.components.toneOrNeutral
import pl.dakil.healthyshopping.ui.designsystem.AppLinearProgress
import pl.dakil.healthyshopping.ui.designsystem.CollapsingScaffold
import pl.dakil.healthyshopping.ui.designsystem.EmptyState
import pl.dakil.healthyshopping.ui.designsystem.FormSheet
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.bottomBarPadding
import pl.dakil.healthyshopping.ui.designsystem.entrance
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.pressScale
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.segmentedItemShape
import pl.dakil.healthyshopping.ui.screens.settings.SectionHeader
import pl.dakil.healthyshopping.ui.theme.SegmentedShapes
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.theme.phraseTone
import pl.dakil.healthyshopping.ui.theme.tabular
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonUiState
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonViewModel

private val LabelWidth = 112.dp
private val ColumnWidth = 124.dp
private val ScrollFadeWidth = 36.dp

@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel,
    comparisonCount: Int,
    showHighlightedIngredients: Boolean,
    onProductClicked: (String) -> Unit,
    onRemoveProduct: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    // Removing one product reloads them all; keep the last table on screen meanwhile.
    var lastProducts by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    LaunchedEffect(uiState) {
        (uiState as? ComparisonUiState.Success)?.let { lastProducts = it.products }
    }

    CollapsingScaffold(
        title = stringResource(R.string.comparison_title),
        subtitle = if (comparisonCount > 0) {
            pluralStringResource(R.plurals.comparison_count, comparisonCount, comparisonCount)
        } else null,
        large = true,
        // A tab root: the overlaid navigation bar supplies the bottom inset (bottomBarPadding).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        actions = {
            if (comparisonCount > 0) {
                IconButton(onClick = { confirmClear = true }, shapes = IconButtonDefaults.shapes()) {
                    AppIcon(AppSymbols.DeleteSweep, contentDescription = stringResource(R.string.comparison_clear))
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val state = uiState
            when {
                comparisonCount == 0 -> EmptyComparison()
                state is ComparisonUiState.Error -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = bottomBarPadding()),
                    verticalArrangement = Arrangement.Center,
                ) { ErrorState(state.errorType, onRetry = { viewModel.refreshComparison() }) }
                state is ComparisonUiState.Success ->
                    ComparisonTable(state.products, showHighlightedIngredients, onProductClicked, onRemoveProduct)
                lastProducts.isNotEmpty() -> {
                    ComparisonTable(lastProducts, showHighlightedIngredients, onProductClicked, onRemoveProduct)
                    AppLinearProgress(Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter))
                }
                else -> LoadingIndicator(Modifier.padding(top = 96.dp).size(64.dp))
            }
        }
    }

    if (confirmClear) {
        FormSheet(
            title = stringResource(R.string.comparison_clear_title),
            symbol = AppSymbols.DeleteSweep,
            supportingText = stringResource(R.string.comparison_clear_body),
            onDismiss = { confirmClear = false },
            dismissLabel = stringResource(R.string.action_cancel),
            confirmLabel = stringResource(R.string.action_clear),
            destructive = true,
            onConfirm = {
                viewModel.clearComparison()
                confirmClear = false
            },
        )
    }
}

@Composable
private fun EmptyComparison() {
    Column(
        Modifier
            .widthIn(max = Spacing.maxContentWidth)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.gutter)
            .padding(bottom = bottomBarPadding(Spacing.xl)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyState(
            symbol = AppSymbols.CompareArrows,
            title = stringResource(R.string.comparison_empty_title),
            body = stringResource(R.string.comparison_empty_body),
            modifier = Modifier.entrance(0),
        )
        val steps = listOf(
            AppSymbols.Search to R.string.comparison_step_1,
            AppSymbols.AddChart to R.string.comparison_step_2,
            AppSymbols.CompareArrows to R.string.comparison_step_3,
        )
        Column(verticalArrangement = Arrangement.spacedBy(SegmentedShapes.gap)) {
            steps.forEachIndexed { index, (symbol, text) ->
                Surface(
                    shape = segmentedItemShape(index, steps.size),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth().entrance(index + 1),
                ) {
                    Row(
                        Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
                    ) {
                        ShapedIcon(symbol)
                        Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonTable(
    products: List<ProductResponse>,
    highlight: Boolean,
    onProductClicked: (String) -> Unit,
    onRemoveProduct: (String) -> Unit,
) {
    val hScroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.s, bottom = bottomBarPadding(Spacing.xl)),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        // Product cards, scrolling sideways together with every row below.
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(LabelWidth))
            ScrollingCells(hScroll, fadeColor = MaterialTheme.colorScheme.surface) {
                products.forEach { product ->
                    ProductHeaderCard(product, onProductClicked, onRemoveProduct)
                }
            }
        }

        SectionHeader(stringResource(R.string.section_score))
        TableGroup {
            val best = bestIndex(products.map { it.score?.value?.toDouble() }, lowerIsBetter = false)
            TableRow(stringResource(R.string.comparison_row_score), products, hScroll, 0, 1) { i, product ->
                ValueCell(product.score?.value?.toString(), best == i)
            }
        }

        val unit = products.firstNotNullOfOrNull { it.nutrientValues?.per100Unit } ?: "g"
        SectionHeader(stringResource(R.string.nutrition_per_100, unit))
        TableGroup {
            NutrientKind.entries.forEachIndexed { row, kind ->
                val nutrients = products.map { it.nutrientValues?.nutrients.orEmpty().find(kind) }
                val best = kind.lowerIsBetter?.let { lower ->
                    bestIndex(nutrients.map { parseNutrientNumber(it?.details?.value) }, lower)
                }
                TableRow(stringResource(kind.label), products, hScroll, row, NutrientKind.entries.size) { i, _ ->
                    ValueCell(nutrients[i]?.displayValue(), best == i)
                }
            }
        }

        SectionHeader(stringResource(R.string.comparison_section_ingredients))
        TableGroup {
            val levels = listOf(5, 4, 3, 2, 1)
            levels.forEachIndexed { row, level ->
                val info = harmLevel(level)
                val tone = info.toneOrNeutral()
                TableRow(
                    label = stringResource(info.groupLabel),
                    products = products,
                    hScroll = hScroll,
                    index = row,
                    count = levels.size,
                    labelLeading = { Box(Modifier.size(10.dp).background(tone.container, CircleShape)) },
                ) { _, product ->
                    val names = product.ingredients.orEmpty()
                        .filter { it.harmfulLevel == level }
                        .map { it.displayName ?: it.name ?: "?" }
                    if (names.isEmpty()) MissingCell(stringResource(R.string.comparison_none))
                    else Text(
                        names.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        SectionHeader(stringResource(R.string.section_ingredients))
        TableGroup {
            TableRow(stringResource(R.string.comparison_row_description), products, hScroll, 0, 1) { _, product ->
                DescriptionCell(product, highlight)
            }
        }
    }
}

/**
 * The part of a row that scrolls sideways. Wherever more columns lie beyond an edge (under the sticky
 * label column, or past the screen's right side) the cells fade out under a short gradient in
 * [fadeColor] (the row's background) instead of being chopped off.
 */
@Composable
private fun ScrollingCells(
    hScroll: ScrollState,
    fadeColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val startFade by animateFloatAsState(if (hScroll.canScrollBackward) 1f else 0f, spec, label = "startFade")
    val endFade by animateFloatAsState(if (hScroll.canScrollForward) 1f else 0f, spec, label = "endFade")
    Row(
        modifier
            .horizontalScroll(hScroll)
            .drawWithContent {
                drawContent()
                // Drawn inside the scrolled content, so the viewport starts at the scroll offset.
                val width = ScrollFadeWidth.toPx()
                val left = hScroll.value.toFloat()
                val right = left + hScroll.viewportSize
                val clear = fadeColor.copy(alpha = 0f)
                if (startFade > 0f) drawRect(
                    brush = Brush.horizontalGradient(0f to fadeColor, 1f to clear, startX = left, endX = left + width),
                    topLeft = Offset(left, 0f),
                    size = Size(width, size.height),
                    alpha = startFade,
                )
                if (endFade > 0f) drawRect(
                    brush = Brush.horizontalGradient(0f to clear, 1f to fadeColor, startX = right - width, endX = right),
                    topLeft = Offset(right - width, 0f),
                    size = Size(width, size.height),
                    alpha = endFade,
                )
            },
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        content()
    }
}

/** A compact product card heading its column: photo with the score on it, name, and a small remove button. */
@Composable
private fun ProductHeaderCard(
    product: ProductResponse,
    onProductClicked: (String) -> Unit,
    onRemoveProduct: (String) -> Unit,
) {
    val haptics = rememberHaptics()
    val interaction = remember { MutableInteractionSource() }
    val ean = product.ean
    Surface(
        onClick = { ean?.let(onProductClicked) },
        enabled = ean != null,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        interactionSource = interaction,
        modifier = Modifier.width(ColumnWidth).pressScale(interaction),
    ) {
        Column(
            Modifier.padding(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProductImage(
                    url = product.image?.url,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(64.dp),
                )
                ScoreBadge(
                    product.score,
                    size = 30.dp,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
                if (ean != null) {
                    FilledTonalIconButton(
                        onClick = {
                            haptics.toggle(false)
                            onRemoveProduct(ean)
                        },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.align(Alignment.TopEnd).size(28.dp),
                    ) {
                        AppIcon(
                            AppSymbols.Close,
                            contentDescription = stringResource(R.string.comparison_remove),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Text(
                product.name ?: stringResource(R.string.product_unknown),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TableGroup(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SegmentedShapes.gap)) { content() }
}

/** One row: a sticky label tile on the left, then one cell per product scrolling with [hScroll]. */
@Composable
private fun TableRow(
    label: String,
    products: List<ProductResponse>,
    hScroll: ScrollState,
    index: Int,
    count: Int,
    labelLeading: (@Composable () -> Unit)? = null,
    cell: @Composable (Int, ProductResponse) -> Unit,
) {
    Surface(
        shape = segmentedItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Row(
                Modifier
                    .width(LabelWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = Spacing.m, vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                labelLeading?.invoke()
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ScrollingCells(hScroll, fadeColor = MaterialTheme.colorScheme.surfaceContainer, Modifier.fillMaxHeight()) {
                products.forEachIndexed { i, product ->
                    Box(
                        Modifier.width(ColumnWidth).fillMaxHeight().padding(vertical = Spacing.s, horizontal = Spacing.xs),
                        contentAlignment = Alignment.Center,
                    ) { cell(i, product) }
                }
            }
        }
    }
}

/** A value; the best one in its row sits on a highlighted pill with a trophy. */
@Composable
private fun ValueCell(value: String?, best: Boolean) {
    if (value == null) {
        MissingCell(stringResource(R.string.value_missing_short))
        return
    }
    if (!best) {
        Text(value, style = MaterialTheme.typography.bodyMedium.tabular())
        return
    }
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                AppSymbols.EmojiEvents,
                contentDescription = stringResource(R.string.cd_best_value),
                filled = true,
                modifier = Modifier.size(14.dp),
            )
            Text(
                value,
                style = MaterialTheme.typography.labelLargeEmphasized.tabular(),
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
    }
}

@Composable
private fun MissingCell(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    )
}

@Composable
private fun DescriptionCell(product: ProductResponse, highlight: Boolean) {
    val description = product.description
    if (description.isNullOrBlank()) {
        MissingCell(stringResource(R.string.value_missing))
        return
    }
    val phrases = product.ingredientPhrases.orEmpty()
    val tones = phrases.map { phraseTone(it.backgroundColor) }
    val text = buildAnnotatedString {
        append(description)
        if (highlight) phrases.forEachIndexed { i, info ->
            val phrase = info.phrase?.takeIf { it.isNotBlank() } ?: return@forEachIndexed
            var start = description.indexOf(phrase, ignoreCase = true)
            while (start != -1) {
                addStyle(SpanStyle(background = tones[i].container, color = tones[i].content), start, start + phrase.length)
                start = description.indexOf(phrase, start + phrase.length, ignoreCase = true)
            }
        }
    }
    Text(text, style = MaterialTheme.typography.bodySmall)
}

/** Index of the single best value, or null when fewer than two products have one or they tie. */
private fun bestIndex(values: List<Double?>, lowerIsBetter: Boolean): Int? {
    val known = values.withIndex().filter { it.value != null }
    if (known.size < 2) return null
    val target = if (lowerIsBetter) known.minOf { it.value!! } else known.maxOf { it.value!! }
    return known.filter { it.value == target }.singleOrNull()?.index
}
