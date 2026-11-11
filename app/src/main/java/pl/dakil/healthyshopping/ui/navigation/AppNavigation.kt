package pl.dakil.healthyshopping.ui.navigation

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import pl.dakil.healthyshopping.ui.designsystem.LocalBottomBarPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.designsystem.RollingNumber
import pl.dakil.healthyshopping.ui.designsystem.icon.AnimatedAppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbol
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.scanner.ScannerScreen
import pl.dakil.healthyshopping.ui.screens.ComparisonScreen
import pl.dakil.healthyshopping.ui.screens.DetailsDisplay
import pl.dakil.healthyshopping.ui.screens.DetailsScreen
import pl.dakil.healthyshopping.ui.screens.FullScreenImageScreen
import pl.dakil.healthyshopping.ui.screens.HomeScreen
import pl.dakil.healthyshopping.ui.screens.MoreScreen
import pl.dakil.healthyshopping.ui.screens.SearchScreen
import pl.dakil.healthyshopping.ui.screens.settings.AppearanceSettingsScreen
import pl.dakil.healthyshopping.ui.screens.settings.HistorySettingsScreen
import pl.dakil.healthyshopping.ui.screens.settings.ProductDetailsSettingsScreen
import pl.dakil.healthyshopping.ui.screens.settings.SearchSettingsScreen
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonViewModel
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel
import pl.dakil.healthyshopping.ui.viewmodel.ProductUiState
import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private enum class Tab(val route: String, @param:StringRes val label: Int, val symbol: AppSymbol) {
    Home("main", R.string.tab_home, AppSymbols.Home),
    Search("search", R.string.tab_search, AppSymbols.Search),
    Comparison("comparison", R.string.tab_comparison, AppSymbols.CompareArrows),
    More("settings_route", R.string.tab_more, AppSymbols.MoreHoriz),
}

private val tabRoutes = Tab.entries.map { it.route }.toSet()

private fun NavBackStackEntry.isTabRoot() = destination.route in tabRoutes

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel,
    comparisonViewModel: ComparisonViewModel,
) {
    val navController = rememberNavController()
    val haptics = rememberHaptics()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val comparisonEans by settingsViewModel.comparisonEans.collectAsState()

    fun openProduct(ean: String) {
        viewModel.resetState()
        navController.navigate("details/$ean")
    }

    // The bar is overlaid on the full-height NavHost (not a Scaffold bottomBar), so screens without
    // it get their full height at once; tab screens pad themselves by its height (see LocalBottomBarPadding).
    val density = LocalDensity.current
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // First-frame estimate (the spec's 64dp bar over the system inset) until the real height is measured.
    var barHeight by remember { mutableStateOf(64.dp + navBarInset) }

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBottomBarPadding provides barHeight) {
            NavHost(
                navController = navController,
                startDestination = Tab.Home.route,
                // Paint the screen background so nothing flashes through while screens cross-fade.
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                // Tabs cross with fade-through; pushed screens use the spring-driven shared axis X.
                enterTransition = {
                    if (initialState.isTabRoot() && targetState.isTabRoot()) NavTransitions.fadeThroughEnter()
                    else NavTransitions.pushEnter()
                },
                exitTransition = {
                    if (initialState.isTabRoot() && targetState.isTabRoot()) NavTransitions.fadeThroughExit()
                    else NavTransitions.pushExit()
                },
                popEnterTransition = {
                    if (initialState.isTabRoot() && targetState.isTabRoot()) NavTransitions.fadeThroughEnter()
                    else NavTransitions.popEnter()
                },
                popExitTransition = {
                    if (initialState.isTabRoot() && targetState.isTabRoot()) NavTransitions.fadeThroughExit()
                    else NavTransitions.popExit()
                },
                // The back gesture scrubs the same pop (the defaults would shrink the page instead).
                predictivePopEnterTransition = { _ -> NavTransitions.popEnter() },
                predictivePopExitTransition = { _ -> NavTransitions.popExit() },
            ) {
                composable(Tab.Home.route) {
                    val recent by settingsViewModel.recentlyViewedItems.collectAsState()
                    val limit by settingsViewModel.recentlyViewedLimit.collectAsState()
                    HomeScreen(
                        recentlyViewedItems = recent,
                        recentlyViewedLimit = limit,
                        onSearchClicked = ::openProduct,
                        onScanClicked = { navController.navigate("scanner") },
                    )
                }
                composable(Tab.Search.route) {
                    SearchScreen(
                        viewModel = searchViewModel,
                        settingsViewModel = settingsViewModel,
                        onProductClicked = ::openProduct,
                    )
                }
                composable(Tab.Comparison.route) {
                    val highlighted by settingsViewModel.showHighlightedIngredients.collectAsState()
                    ComparisonScreen(
                        viewModel = comparisonViewModel,
                        comparisonCount = comparisonEans.size,
                        showHighlightedIngredients = highlighted,
                        onProductClicked = ::openProduct,
                        onRemoveProduct = settingsViewModel::removeFromComparison,
                    )
                }
                composable(Tab.More.route) {
                    MoreScreen(
                        onNavigateAppearance = { navController.navigate("settings_appearance") },
                        onNavigateSearch = { navController.navigate("settings_search") },
                        onNavigateProductDetails = { navController.navigate("settings_details") },
                        onNavigateHistory = { navController.navigate("settings_history") },
                    )
                }
                composable("settings_appearance") {
                    AppearanceSettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable("settings_search") {
                    SearchSettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable("settings_details") {
                    ProductDetailsSettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable("settings_history") {
                    HistorySettingsScreen(settingsViewModel, onNavigateBack = { navController.popBackStack() })
                }
                composable("scanner") {
                    ScannerScreen(
                        onBarcodeDetected = { barcode ->
                            navController.popBackStack(Tab.Home.route, inclusive = false)
                            openProduct(barcode)
                        },
                        onBackClicked = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "details/{ean}",
                    arguments = listOf(navArgument("ean") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "https://zdrowezakupy.org/product/{ean}" }),
                ) { entry ->
                    val ean = entry.arguments?.getString("ean") ?: ""
                    val context = LocalContext.current
                    val uiState by viewModel.uiState.collectAsState()
                    val ingredientUiState by viewModel.ingredientUiState.collectAsState()
                    val grouped by settingsViewModel.showGroupedIngredients.collectAsState()
                    val bars by settingsViewModel.showNutritionProgressBars.collectAsState()
                    val highlighted by settingsViewModel.showHighlightedIngredients.collectAsState()
                    val tags by settingsViewModel.showProductTags.collectAsState()
                    val order by settingsViewModel.detailsSectionOrder.collectAsState()
                    val hidden by settingsViewModel.hiddenDetailsSections.collectAsState()
                    val inComparison = ean in comparisonEans

                    LaunchedEffect(ean) {
                        if (ean.isNotBlank()) {
                            val current = viewModel.uiState.value
                            if (current !is ProductUiState.Success || current.product.ean != ean) viewModel.getProduct(ean)
                        }
                    }
                    LaunchedEffect(uiState) {
                        (uiState as? ProductUiState.Success)?.let { viewModel.addToHistory(it.product) }
                    }

                    fun leave() {
                        if (navController.previousBackStackEntry != null) navController.popBackStack()
                        else (context as? Activity)?.finish()
                    }

                    DetailsScreen(
                        uiState = uiState,
                        display = DetailsDisplay(
                            groupedIngredients = grouped,
                            nutritionBars = bars,
                            highlightedIngredients = highlighted,
                            productTags = tags,
                            sectionOrder = order,
                            hiddenSections = hidden,
                        ),
                        isProductInComparison = inComparison,
                        ingredientUiState = ingredientUiState,
                        onToggleComparison = {
                            if (inComparison) settingsViewModel.removeFromComparison(ean)
                            else settingsViewModel.addToComparison(ean)
                        },
                        onBackClicked = ::leave,
                        onRetry = {
                            if (ean.isNotBlank()) viewModel.getProduct(ean)
                            else {
                                viewModel.resetState()
                                leave()
                            }
                        },
                        onImageClicked = { url ->
                            navController.navigate("full_screen_image/${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}")
                        },
                        onIngredientClicked = viewModel::getIngredient,
                        onDismissIngredientDetails = viewModel::resetIngredientState,
                    )
                }
                composable(
                    route = "full_screen_image/{imageUrl}",
                    arguments = listOf(navArgument("imageUrl") { type = NavType.StringType }),
                ) { entry ->
                    val encoded = entry.arguments?.getString("imageUrl") ?: ""
                    FullScreenImageScreen(
                        imageUrl = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString()),
                        onBackClicked = { navController.popBackStack() },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = currentRoute in tabRoutes,
            enter = slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { it } +
                fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec()) { it } +
                fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ShortNavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.onSizeChanged { barHeight = with(density) { it.height.toDp() } },
            ) {
                Tab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    ShortNavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) haptics.tick()
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val count = comparisonEans.size
                            if (tab == Tab.Comparison && count > 0) {
                                BadgedBox(badge = { Badge { RollingNumber(count.toString()) } }) {
                                    AnimatedAppIcon(tab.symbol, contentDescription = null, filled = selected)
                                }
                            } else {
                                AnimatedAppIcon(tab.symbol, contentDescription = null, filled = selected)
                            }
                        },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        }
    }
}
