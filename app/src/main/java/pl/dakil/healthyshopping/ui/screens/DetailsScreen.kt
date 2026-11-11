package pl.dakil.healthyshopping.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.Ingredient
import pl.dakil.healthyshopping.data.model.IngredientResponse
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.data.repository.DetailsSection
import pl.dakil.healthyshopping.ui.components.ErrorState
import pl.dakil.healthyshopping.ui.components.GrowingBar
import pl.dakil.healthyshopping.ui.components.ProductImage
import pl.dakil.healthyshopping.ui.components.ScoreBadge
import pl.dakil.healthyshopping.ui.components.dailyShare
import pl.dakil.healthyshopping.ui.components.detailsSectionFor
import pl.dakil.healthyshopping.ui.components.displayValue
import pl.dakil.healthyshopping.ui.components.formatNutritionForCopy
import pl.dakil.healthyshopping.ui.components.harmLevel
import pl.dakil.healthyshopping.ui.components.labelRes
import pl.dakil.healthyshopping.ui.components.nutrientKindOf
import pl.dakil.healthyshopping.ui.components.parseNutrientNumber
import pl.dakil.healthyshopping.ui.components.symbol
import pl.dakil.healthyshopping.ui.components.tagInfo
import pl.dakil.healthyshopping.ui.components.toneOrNeutral
import pl.dakil.healthyshopping.ui.designsystem.BackButton
import pl.dakil.healthyshopping.ui.designsystem.CollapsingScaffold
import pl.dakil.healthyshopping.ui.designsystem.CountUpText
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.entrance
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbol
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.segmentedItemColors
import pl.dakil.healthyshopping.ui.theme.HealthTone
import pl.dakil.healthyshopping.ui.theme.Spacing
import pl.dakil.healthyshopping.ui.theme.accent
import pl.dakil.healthyshopping.ui.theme.parseHexColor
import pl.dakil.healthyshopping.ui.theme.phraseTone
import pl.dakil.healthyshopping.ui.theme.tabular
import pl.dakil.healthyshopping.ui.theme.tone
import pl.dakil.healthyshopping.ui.viewmodel.IngredientUiState
import pl.dakil.healthyshopping.ui.viewmodel.ProductUiState

/** What the details page shows, as chosen in the product-details settings. */
data class DetailsDisplay(
    val groupedIngredients: Boolean,
    val nutritionBars: Boolean,
    val highlightedIngredients: Boolean,
    val productTags: Boolean,
    val sectionOrder: List<String>,
    val hiddenSections: Set<String>,
)

@Composable
fun DetailsScreen(
    uiState: ProductUiState,
    display: DetailsDisplay,
    isProductInComparison: Boolean,
    ingredientUiState: IngredientUiState,
    onToggleComparison: () -> Unit,
    onBackClicked: () -> Unit,
    onRetry: () -> Unit,
    onImageClicked: (String) -> Unit,
    onIngredientClicked: (Int) -> Unit,
    onDismissIngredientDetails: () -> Unit,
) {
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addedMessage = stringResource(R.string.comparison_added)
    val removedMessage = stringResource(R.string.comparison_removed)
    val copiedMessage = stringResource(R.string.copied)
    val product = (uiState as? ProductUiState.Success)?.product

    CollapsingScaffold(
        title = stringResource(R.string.details_title),
        navigationIcon = { BackButton(onBackClicked) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (product != null) {
                IconToggleButton(
                    checked = isProductInComparison,
                    onCheckedChange = { checked ->
                        haptics.toggle(checked)
                        onToggleComparison()
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(if (checked) addedMessage else removedMessage)
                        }
                    },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    AppIcon(
                        if (isProductInComparison) AppSymbols.CheckCircle else AppSymbols.AddChart,
                        contentDescription = stringResource(
                            if (isProductInComparison) R.string.comparison_remove else R.string.comparison_add
                        ),
                        filled = isProductInComparison,
                    )
                }
                val shareText = product.share?.shareText
                if (shareText != null) {
                    val chooserTitle = stringResource(R.string.action_share)
                    IconButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(send, chooserTitle))
                        },
                        shapes = IconButtonDefaults.shapes(),
                    ) { AppIcon(AppSymbols.Share, contentDescription = chooserTitle) }
                }
            }
        },
    ) { padding ->
        val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            transitionSpec = { fadeIn(fadeInSpec) togetherWith fadeOut(fadeOutSpec) },
            modifier = Modifier.fillMaxSize().padding(padding),
            label = "details",
        ) { state ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (state) {
                    is ProductUiState.Idle -> Unit
                    is ProductUiState.Loading -> LoadingIndicator(Modifier.size(72.dp))
                    is ProductUiState.Error -> Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                    ) { ErrorState(state.errorType, onRetry = onRetry) }
                    is ProductUiState.Success -> ProductContent(
                        product = state.product,
                        display = display,
                        onImageClicked = onImageClicked,
                        onIngredientClicked = onIngredientClicked,
                        onCopied = {
                            haptics.confirm()
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        },
                    )
                }
            }
        }
    }

    if (ingredientUiState !is IngredientUiState.Idle) {
        IngredientSheet(ingredientUiState, onDismiss = onDismissIngredientDetails)
    }
}

@Composable
private fun ProductContent(
    product: ProductResponse,
    display: DetailsDisplay,
    onImageClicked: (String) -> Unit,
    onIngredientClicked: (Int) -> Unit,
    onCopied: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    fun copy(label: String, text: String) {
        scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, text))) }
        onCopied()
    }

    val sections = display.sectionOrder
        .filter { it !in display.hiddenSections }
        .mapNotNull(::detailsSectionFor)
    val scoreHidden = DetailsSection.SCORE_CARD !in sections

    LazyColumn(
        modifier = Modifier.widthIn(max = Spacing.maxContentWidth).fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.gutter, end = Spacing.gutter, top = Spacing.s, bottom = Spacing.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        product.unverifiedCell?.let { unverified ->
            item(key = "unverified") {
                UnverifiedBanner(unverified.title, unverified.description, Modifier.entrance(0))
            }
        }
        item(key = "hero") {
            HeroCard(
                product = product,
                showScore = scoreHidden,
                onImageClicked = onImageClicked,
                modifier = Modifier.entrance(0),
            )
        }
        sections.forEachIndexed { index, section ->
            val entranceModifier = Modifier.fillMaxWidth()
            when (section) {
                DetailsSection.SCORE_CARD -> item(key = section.id) {
                    ScoreCard(product, entranceModifier.entrance(index + 1))
                }
                DetailsSection.PRODUCT_TAGS -> {
                    val tags = product.tags.orEmpty()
                    if (display.productTags && tags.isNotEmpty()) item(key = section.id) {
                        Section(section, entranceModifier.entrance(index + 1)) { TagsFlow(tags) }
                    }
                }
                DetailsSection.NUTRITION_TABLE -> {
                    val nutrients = product.nutrientValues?.nutrients.orEmpty()
                    if (nutrients.isNotEmpty()) item(key = section.id) {
                        val unit = product.nutrientValues?.per100Unit ?: "g"
                        Section(
                            section,
                            entranceModifier.entrance(index + 1),
                            title = stringResource(R.string.nutrition_per_100, unit),
                            action = {
                                CopyButton(stringResource(R.string.cd_copy_nutrition)) {
                                    copy("Nutrition", formatNutritionForCopy(product, resources))
                                }
                            },
                        ) { NutritionCard(product, display.nutritionBars) }
                    }
                }
                DetailsSection.INGREDIENTS_LIST -> item(key = section.id) {
                    Section(
                        section,
                        entranceModifier.entrance(index + 1),
                        action = if (!product.description.isNullOrBlank()) {
                            {
                                CopyButton(stringResource(R.string.cd_copy_ingredients)) {
                                    copy("Ingredients", product.description)
                                }
                            }
                        } else null,
                    ) { IngredientsCard(product, display.highlightedIngredients) }
                }
                DetailsSection.HARMFUL_INGREDIENTS -> {
                    val ingredients = product.ingredients.orEmpty()
                    if (ingredients.isNotEmpty()) item(key = section.id) {
                        Section(section, entranceModifier.entrance(index + 1)) {
                            HarmfulIngredients(ingredients, display.groupedIngredients, onIngredientClicked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnverifiedBanner(title: String?, description: String?, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapedIcon(
                AppSymbols.Report,
                polygon = MaterialShapes.Cookie9Sided,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    title ?: stringResource(R.string.details_unverified_default),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                )
                if (!description.isNullOrBlank()) {
                    Text(description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = Spacing.xxs))
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    product: ProductResponse,
    showScore: Boolean,
    onImageClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val url = product.image?.url
            Box {
                ProductImage(
                    url = url,
                    shape = MaterialTheme.shapes.largeIncreased,
                    contentDescription = stringResource(R.string.cd_product_photo),
                    modifier = Modifier
                        .size(112.dp)
                        .then(if (url != null) Modifier.clickable { onImageClicked(url) } else Modifier),
                )
                if (url != null) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(Spacing.xs)
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(
                            AppSymbols.ZoomIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    product.name ?: stringResource(R.string.product_unknown),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    modifier = Modifier.semantics { heading() },
                )
                product.category?.name?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            AppSymbols.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Spacing.xs),
                        )
                    }
                }
                product.ean?.let {
                    Text(
                        stringResource(R.string.details_ean, it),
                        style = MaterialTheme.typography.labelMedium.tabular(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (showScore) ScoreBadge(product.score, size = 52.dp, hero = true)
        }
    }
}

/** A titled block on the page: a small shaped icon, the title and an optional trailing action. */
@Composable
private fun Section(
    section: DetailsSection,
    modifier: Modifier = Modifier,
    title: String = stringResource(section.labelRes()),
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        SectionTitle(title, section.symbol, action)
        content()
    }
}

@Composable
private fun SectionTitle(title: String, symbol: AppSymbol, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        ShapedIcon(symbol, size = 32.dp, polygon = MaterialShapes.Cookie7Sided)
        Text(
            title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        action?.invoke()
    }
}

@Composable
private fun CopyButton(description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, shapes = IconButtonDefaults.shapes()) {
        AppIcon(AppSymbols.ContentCopy, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ScoreCard(product: ProductResponse, modifier: Modifier = Modifier) {
    val score = product.score
    val scoreColor = parseHexColor(score?.color) ?: MaterialTheme.colorScheme.primary
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Row(
            Modifier.padding(Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            ScoreBadge(score, size = 96.dp, hero = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    stringResource(R.string.section_score),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    score?.label ?: stringResource(R.string.value_missing),
                    style = MaterialTheme.typography.headlineSmall,
                )
                val value = score?.value
                val max = score?.max
                if (value != null && max != null && max > 0) {
                    GrowingBar(value.toFloat() / max, scoreColor, Modifier.padding(top = Spacing.xs))
                    Text(
                        stringResource(R.string.details_score_of, value, max),
                        style = MaterialTheme.typography.labelMedium.tabular(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsFlow(tags: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        tags.forEach { tag ->
            val info = tagInfo(tag)
            val tone = info.tone?.tone()
            Surface(
                shape = CircleShape,
                color = tone?.container ?: MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = tone?.content ?: MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    Modifier.padding(start = Spacing.m, end = Spacing.l, top = Spacing.s, bottom = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(
                        when (info.tone) {
                            HealthTone.GOOD -> AppSymbols.CheckCircle
                            HealthTone.BAD -> AppSymbols.Warning
                            else -> AppSymbols.Sell
                        },
                        contentDescription = null,
                        filled = true,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        info.label?.let { stringResource(it) } ?: tag,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Spacing.s),
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(product: ProductResponse, showBars: Boolean) {
    val nutrients = product.nutrientValues?.nutrients.orEmpty()
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) {
            nutrients.forEachIndexed { index, nutrient ->
                val name = nutrient.name ?: stringResource(R.string.nutrient_unknown)
                // Sub-rows ("w tym cukry") are indented under their parent.
                val sub = name.trim().startsWith("w tym", ignoreCase = true)
                val share = dailyShare(nutrientKindOf(name), parseNutrientNumber(nutrient.details?.value))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = if (sub) Spacing.l else 0.dp, top = Spacing.s, bottom = Spacing.s),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            style = if (sub) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = if (sub) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        CountUpText(
                            nutrient.displayValue() ?: stringResource(R.string.value_missing_short),
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (showBars && share != null) {
                        Row(
                            Modifier.padding(top = Spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GrowingBar(
                                share.fraction,
                                share.tone?.accent() ?: MaterialTheme.colorScheme.primary,
                                Modifier.weight(1f),
                            )
                            CountUpText(
                                "${(share.fraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Spacing.m).width(40.dp),
                            )
                        }
                    }
                }
                if (index == nutrients.lastIndex && showBars) {
                    Text(
                        stringResource(R.string.details_gda_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.s, bottom = Spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientsCard(product: ProductResponse, highlight: Boolean) {
    val description = product.description
    val phrases = product.ingredientPhrases.orEmpty()
    // Resolve the highlight colors in composition: they follow the app's own dark mode.
    val tones = phrases.map { phraseTone(it.backgroundColor) }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (description.isNullOrBlank()) {
            Text(
                stringResource(R.string.details_no_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.l),
            )
            return@Surface
        }
        val text = buildAnnotatedString {
            append(description)
            if (highlight) {
                phrases.forEachIndexed { i, info ->
                    val phrase = info.phrase?.takeIf { it.isNotBlank() } ?: return@forEachIndexed
                    val tone = tones[i]
                    var start = description.indexOf(phrase, ignoreCase = true)
                    while (start != -1) {
                        addStyle(
                            SpanStyle(background = tone.container, color = tone.content, fontWeight = FontWeight.Medium),
                            start,
                            start + phrase.length,
                        )
                        start = description.indexOf(phrase, start + phrase.length, ignoreCase = true)
                    }
                }
            }
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(Spacing.l),
        )
    }
}

@Composable
private fun HarmfulIngredients(
    ingredients: List<Ingredient>,
    grouped: Boolean,
    onIngredientClicked: (Int) -> Unit,
) {
    if (!grouped) {
        IngredientGroup(ingredients, showLevel = true, onIngredientClicked)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        ingredients
            .groupBy { it.harmfulLevel ?: 0 }
            .toSortedMap(compareByDescending { it })
            .forEach { (level, items) ->
                val info = harmLevel(level)
                val tone = info.toneOrNeutral()
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    Row(
                        Modifier.padding(start = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        Text(
                            stringResource(info.groupLabel),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(shape = CircleShape, color = tone.container, contentColor = tone.content) {
                            Text(
                                items.size.toString(),
                                style = MaterialTheme.typography.labelMedium.tabular(),
                                modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs),
                            )
                        }
                    }
                    IngredientGroup(items, showLevel = false, onIngredientClicked)
                }
            }
    }
}

/** Ingredients as one segmented card; each tile's shaped icon carries its harm level's tone. */
@Composable
private fun IngredientGroup(
    ingredients: List<Ingredient>,
    showLevel: Boolean,
    onIngredientClicked: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        ingredients.forEachIndexed { index, ingredient ->
            val info = harmLevel(ingredient.harmfulLevel)
            val tone = info.toneOrNeutral()
            val id = ingredient.id
            val leading: @Composable () -> Unit = {
                ShapedIcon(
                    info.symbol,
                    polygon = MaterialShapes.Cookie9Sided,
                    containerColor = tone.container,
                    contentColor = tone.content,
                )
            }
            val supporting: (@Composable () -> Unit)? = if (showLevel) {
                { Text(stringResource(info.groupLabel)) }
            } else null
            val name = ingredient.displayName ?: ingredient.name ?: stringResource(R.string.ingredient_unknown)
            val shapes = ListItemDefaults.segmentedShapes(index, ingredients.size)
            if (id != null) {
                SegmentedListItem(
                    onClick = { onIngredientClicked(id) },
                    shapes = shapes,
                    colors = segmentedItemColors(),
                    leadingContent = leading,
                    supportingContent = supporting,
                    trailingContent = {
                        AppIcon(AppSymbols.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                ) { Text(name) }
            } else {
                SegmentedListItem(
                    shapes = shapes,
                    colors = segmentedItemColors(),
                    leadingContent = leading,
                    supportingContent = supporting,
                ) { Text(name) }
            }
        }
    }
}

@Composable
private fun IngredientSheet(state: IngredientUiState, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn(fadeInSpec) togetherWith fadeOut(fadeOutSpec) },
            label = "ingredient",
        ) { current ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (current) {
                    is IngredientUiState.Idle -> Unit
                    is IngredientUiState.Loading -> LoadingIndicator(Modifier.padding(vertical = Spacing.xxl).size(64.dp))
                    is IngredientUiState.Error -> ErrorState(current.errorType)
                    is IngredientUiState.Success -> IngredientDetails(current.ingredient)
                }
            }
        }
    }
}

@Composable
private fun IngredientDetails(ingredient: IngredientResponse) {
    val info = harmLevel(ingredient.harmfulLevel)
    val tone = info.toneOrNeutral()
    val title = ingredient.displayName ?: ingredient.name ?: stringResource(R.string.ingredient_unknown)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            ShapedIcon(
                info.symbol,
                polygon = MaterialShapes.Cookie9Sided,
                size = 56.dp,
                containerColor = tone.container,
                contentColor = tone.content,
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineSmallEmphasized)
                val code = ingredient.name
                if (code != null && code != ingredient.displayName && code.startsWith("E", ignoreCase = true)) {
                    Text(code, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Surface(shape = CircleShape, color = tone.container, contentColor = tone.content) {
            Text(
                stringResource(info.description),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            )
        }
        ingredient.description?.takeIf { it.isNotBlank() }?.let {
            SheetParagraph(stringResource(R.string.ingredient_description), it)
        }
        ingredient.influence?.takeIf { it.isNotBlank() }?.let {
            SheetParagraph(stringResource(R.string.ingredient_influence), it)
        }
    }
}

@Composable
private fun SheetParagraph(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(title, style = MaterialTheme.typography.labelLargeEmphasized, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
