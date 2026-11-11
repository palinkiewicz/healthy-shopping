package pl.dakil.healthyshopping.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.ui.components.ProductImage
import pl.dakil.healthyshopping.ui.components.ScoreBadge
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.bottomBarPadding
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion
import pl.dakil.healthyshopping.ui.designsystem.decorativeSpec
import pl.dakil.healthyshopping.ui.designsystem.entrance
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.pressScale
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.designsystem.shakeOn
import pl.dakil.healthyshopping.ui.screens.settings.SectionHeader
import pl.dakil.healthyshopping.ui.theme.Spacing
import java.util.Calendar
import kotlin.random.Random

@Composable
fun HomeScreen(
    recentlyViewedItems: List<SearchProduct>,
    recentlyViewedLimit: Int,
    onSearchClicked: (String) -> Unit,
    onScanClicked: () -> Unit,
) {
    val context = LocalContext.current
    val haptics = rememberHaptics()
    var ean by rememberSaveable { mutableStateOf("") }
    var rejectCount by remember { mutableIntStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onScanClicked() }

    fun scan() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) onScanClicked() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun submit() {
        val code = ean.trim()
        if (code.isEmpty()) {
            haptics.reject()
            rejectCount++
        } else {
            onSearchClicked(code)
        }
    }

    val recent = recentlyViewedItems.take(recentlyViewedLimit)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = Spacing.maxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = bottomBarPadding(Spacing.xl)),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Greeting(Modifier.padding(horizontal = Spacing.gutter).entrance(0))

            if (recentlyViewedLimit > 0) {
                Column(Modifier.entrance(1)) {
                    Box(Modifier.padding(horizontal = Spacing.gutter)) {
                        SectionHeader(stringResource(R.string.home_recent))
                    }
                    if (recent.isEmpty()) {
                        RecentEmptyHint(Modifier.padding(horizontal = Spacing.gutter))
                    } else {
                        RecentCarousel(recent, onProductClicked = onSearchClicked)
                    }
                }
            }

            ScanCard(
                ean = ean,
                onEanChange = { value -> ean = value.filter { it.isDigit() } },
                onScan = ::scan,
                onSubmit = ::submit,
                modifier = Modifier
                    .padding(horizontal = Spacing.gutter)
                    .entrance(2)
                    .shakeOn(rejectCount),
            )

            FactCard(Modifier.padding(horizontal = Spacing.gutter).entrance(3))
        }
    }
}

/** A greeting built around the meal of the day, with its dish settling into place on a cookie. */
@Composable
private fun Greeting(modifier: Modifier = Modifier) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (greeting, subtitle, symbol) = when (hour) {
        in 4..9 -> Triple(R.string.home_meal_breakfast, R.string.home_meal_breakfast_sub, AppSymbols.EggAlt)
        in 10..11 -> Triple(R.string.home_meal_brunch, R.string.home_meal_brunch_sub, AppSymbols.BakeryDining)
        in 12..15 -> Triple(R.string.home_meal_lunch, R.string.home_meal_lunch_sub, AppSymbols.RamenDining)
        in 16..17 -> Triple(R.string.home_meal_snack, R.string.home_meal_snack_sub, AppSymbols.Nutrition)
        in 18..21 -> Triple(R.string.home_meal_dinner, R.string.home_meal_dinner_sub, AppSymbols.DinnerDining)
        else -> Triple(R.string.home_meal_night, R.string.home_meal_night_sub, AppSymbols.Bedtime)
    }
    val decorative = decorativeMotion
    val rotation = remember { Animatable(if (decorative) -90f else 0f) }
    val spring = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { rotation.animateTo(0f, spring) }

    Row(
        modifier.fillMaxWidth().padding(top = Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                stringResource(greeting),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ShapedIcon(
            symbol = symbol,
            polygon = MaterialShapes.Cookie9Sided,
            size = 56.dp,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            containerRotation = rotation.value,
        )
    }
}

/** The two ways in: the camera, or the barcode typed by hand. */
@Composable
private fun ScanCard(
    ean: String,
    onEanChange: (String) -> Unit,
    onScan: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            TextField(
                value = ean,
                onValueChange = onEanChange,
                placeholder = { Text(stringResource(R.string.home_ean_placeholder), maxLines = 1) },
                trailingIcon = {
                    FilledIconButton(
                        onClick = onSubmit,
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.padding(end = Spacing.xs),
                    ) {
                        AppIcon(AppSymbols.ArrowForward, contentDescription = stringResource(R.string.home_check))
                    }
                },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    stringResource(R.string.home_or),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.m),
                )
                HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Button(
                onClick = onScan,
                shapes = ButtonDefaults.shapes(),
                contentPadding = ButtonDefaults.MediumContentPadding,
                modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
            ) {
                AppIcon(
                    AppSymbols.BarcodeScanner,
                    contentDescription = null,
                    filled = true,
                    modifier = Modifier.size(ButtonDefaults.MediumIconSize),
                )
                Text(
                    stringResource(R.string.home_scan),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = ButtonDefaults.MediumIconSpacing),
                )
            }
        }
    }
}

@Composable
private fun RecentCarousel(items: List<SearchProduct>, onProductClicked: (String) -> Unit) {
    val state = rememberCarouselState { items.size }
    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = 160.dp,
        itemSpacing = Spacing.s,
        contentPadding = PaddingValues(horizontal = Spacing.gutter),
        modifier = Modifier.fillMaxWidth().height(192.dp),
    ) { index ->
        val product = items[index]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable(enabled = product.ean != null) { product.ean?.let(onProductClicked) },
        ) {
            ProductImage(
                url = product.image?.url,
                shape = RectangleShape,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
            )
            // A scrim keeps the name readable over any packshot.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
            )
            Text(
                product.name ?: stringResource(R.string.product_unknown),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.m),
            )
            ScoreBadge(
                score = product.score,
                size = 40.dp,
                modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.s),
            )
        }
    }
}

@Composable
private fun RecentEmptyHint(modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            ShapedIcon(AppSymbols.History, polygon = MaterialShapes.Cookie9Sided)
            Text(
                stringResource(R.string.home_recent_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A food fact. Each launch starts somewhere random and steps through a shuffled order, so "next"
 * doesn't repeat a fact until all of them have been shown.
 */
@Composable
private fun FactCard(modifier: Modifier = Modifier) {
    val haptics = rememberHaptics()
    val facts = stringArrayResource(R.array.home_facts)
    // Saved across tab switches and rotation; a fresh launch draws a new order.
    val seed = rememberSaveable { Random.nextLong() }
    val order = remember(seed, facts.size) { facts.indices.shuffled(Random(seed)) }
    var position by rememberSaveable { mutableIntStateOf(0) }
    val index = order[position % order.size]
    val spin by animateFloatAsState(
        targetValue = position * 72f,
        animationSpec = decorativeSpec(MaterialTheme.motionScheme.defaultSpatialSpec()),
        label = "factSpin",
    )
    val interaction = remember { MutableInteractionSource() }
    val enter = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Surface(
        onClick = {
            haptics.tick()
            position++
        },
        shape = MaterialTheme.shapes.extraLarge,
        // A quiet tonal step above the background: present, but never competing with the scan card.
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        interactionSource = interaction,
        modifier = modifier.fillMaxWidth().pressScale(interaction, 0.98f),
    ) {
        Row(
            Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            ShapedIcon(
                AppSymbols.TipsAndUpdates,
                polygon = MaterialShapes.Cookie9Sided,
                size = 36.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerRotation = spin,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    stringResource(R.string.home_fact_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        (slideInVertically(enter) { it / 3 } + fadeIn(fade)) togetherWith
                            (slideOutVertically(enter) { -it / 3 } + fadeOut(fade))
                    },
                    label = "fact",
                ) { i ->
                    Text(facts[i], style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    stringResource(R.string.home_fact_next),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}
