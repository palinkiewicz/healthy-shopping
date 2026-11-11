package pl.dakil.healthyshopping.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.BuildConfig
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.designsystem.FormSheet
import pl.dakil.healthyshopping.ui.designsystem.SegmentedGroup
import pl.dakil.healthyshopping.ui.designsystem.bottomBarPadding
import pl.dakil.healthyshopping.ui.designsystem.ShapedIcon
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion
import pl.dakil.healthyshopping.ui.designsystem.entrance
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbol
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.screens.settings.NavigationRow
import pl.dakil.healthyshopping.ui.screens.settings.SectionHeader
import pl.dakil.healthyshopping.ui.theme.Spacing

private const val REPOSITORY_URL = "https://github.com/palinkiewicz/healthy-shopping"
private const val AUTHOR_URL = "https://github.com/palinkiewicz"
private const val AUTHOR_NAME = "palinkiewicz"
private const val CO_CREATOR_URL = "https://github.com/creeper82"
private const val CO_CREATOR_NAME = "creeper82"

/** The "More" tab: the app's business card and the entry points to every settings page. */
@Composable
fun MoreScreen(
    onNavigateAppearance: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateProductDetails: () -> Unit,
    onNavigateHistory: () -> Unit,
) {
    var showAbout by rememberSaveable { mutableStateOf(false) }
    if (showAbout) AboutSheet(onDismiss = { showAbout = false })

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = Spacing.maxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = Spacing.gutter)
                .padding(bottom = bottomBarPadding(Spacing.xl)),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Header(Modifier.entrance(0))

            Column(Modifier.entrance(1)) {
                SectionHeader(stringResource(R.string.more_section_personalize))
                SegmentedGroup {
                    NavigationRow(
                        title = stringResource(R.string.settings_appearance),
                        summary = stringResource(R.string.settings_appearance_summary),
                        icon = AppSymbols.Palette,
                        onClick = onNavigateAppearance,
                    )
                }
            }

            Column(Modifier.entrance(2)) {
                SectionHeader(stringResource(R.string.more_section_features))
                SegmentedGroup {
                    NavigationRow(
                        title = stringResource(R.string.settings_search),
                        summary = stringResource(R.string.settings_search_summary),
                        icon = AppSymbols.ManageSearch,
                        onClick = onNavigateSearch,
                    )
                    NavigationRow(
                        title = stringResource(R.string.settings_details),
                        summary = stringResource(R.string.settings_details_summary),
                        icon = AppSymbols.ShoppingBasket,
                        onClick = onNavigateProductDetails,
                    )
                    NavigationRow(
                        title = stringResource(R.string.settings_history),
                        summary = stringResource(R.string.settings_history_summary),
                        icon = AppSymbols.History,
                        onClick = onNavigateHistory,
                    )
                }
            }

            Column(Modifier.entrance(3)) {
                SectionHeader(stringResource(R.string.more_section_about))
                SegmentedGroup {
                    NavigationRow(
                        title = stringResource(R.string.about_title),
                        summary = stringResource(R.string.about_summary),
                        icon = AppSymbols.Info,
                        onClick = { showAbout = true },
                    )
                }
            }
        }
    }
}

/** The app's glyph in a cookie that settles into place, its name, tagline and version. */
@Composable
private fun Header(modifier: Modifier = Modifier) {
    // A small flourish each time the tab opens; with decorative motion off it simply sits still.
    val decorative = decorativeMotion
    val rotation = remember { Animatable(if (decorative) -120f else 0f) }
    val spring = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { rotation.animateTo(0f, spring) }
    Column(
        modifier.fillMaxWidth().padding(top = Spacing.xxl, bottom = Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShapedIcon(
            symbol = AppSymbols.Eco,
            polygon = MaterialShapes.Cookie12Sided,
            size = 112.dp,
            iconSize = 52.dp,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerRotation = rotation.value,
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmallEmphasized,
            // No Surface/Scaffold provides a content color here, so set it explicitly.
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.l).semantics { heading() },
        )
        Text(
            stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.s, start = Spacing.l, end = Spacing.l),
        )
        Text(
            stringResource(R.string.version_x, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun AboutSheet(onDismiss: () -> Unit) {
    FormSheet(
        title = stringResource(R.string.app_name),
        symbol = AppSymbols.Eco,
        supportingText = stringResource(R.string.about_description),
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_close),
    ) {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
            Text(
                stringResource(R.string.version_x, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
            )
        }
        LinkButton(
            label = stringResource(R.string.about_repository),
            symbol = AppSymbols.Code,
            url = REPOSITORY_URL,
            modifier = Modifier.fillMaxWidth(),
        )
        // People get full-width rows: half-width buttons cut their names off.
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            PersonRow(
                name = AUTHOR_NAME,
                role = stringResource(R.string.about_author),
                symbol = AppSymbols.Person,
                url = AUTHOR_URL,
                index = 0,
            )
            PersonRow(
                name = CO_CREATOR_NAME,
                role = stringResource(R.string.about_idea),
                symbol = AppSymbols.Lightbulb,
                url = CO_CREATOR_URL,
                index = 1,
            )
        }
    }
}

@Composable
private fun PersonRow(name: String, role: String, symbol: AppSymbol, url: String, index: Int) {
    val uriHandler = LocalUriHandler.current
    SegmentedListItem(
        onClick = { runCatching { uriHandler.openUri(url) } },
        shapes = ListItemDefaults.segmentedShapes(index, 2),
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        leadingContent = { ShapedIcon(symbol) },
        supportingContent = { Text(role) },
        trailingContent = {
            AppIcon(AppSymbols.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
    ) { Text(name) }
}

@Composable
private fun LinkButton(label: String, symbol: AppSymbol, url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    FilledTonalButton(
        onClick = { runCatching { uriHandler.openUri(url) } },
        shapes = ButtonDefaults.shapes(),
        modifier = modifier,
    ) {
        AppIcon(symbol, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Text(label, maxLines = 1, modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
    }
}
