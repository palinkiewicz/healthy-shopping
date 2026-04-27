package pl.dakil.healthyshopping.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.NavType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import pl.dakil.healthyshopping.ui.screens.FullScreenImageScreen
import pl.dakil.healthyshopping.ui.screens.DetailsScreen
import pl.dakil.healthyshopping.ui.screens.MainScreen
import pl.dakil.healthyshopping.ui.screens.SearchScreen
import pl.dakil.healthyshopping.ui.screens.SettingsScreen
import pl.dakil.healthyshopping.ui.screens.ComparisonScreen
import pl.dakil.healthyshopping.ui.scanner.ScannerScreen
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel
import pl.dakil.healthyshopping.ui.viewmodel.ProductUiState

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var route: String) {
    data object Main : BottomNavItem("Główna", Icons.Default.Home, "main")
    data object Search : BottomNavItem("Szukaj", Icons.Default.Search, "search")
    data object Comparison : BottomNavItem("Porównanie", Icons.AutoMirrored.Filled.CompareArrows, "comparison")
    data object Settings : BottomNavItem("Ustawienia", Icons.Default.Settings, "settings_route")
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel,
    comparisonViewModel: ComparisonViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("main", "search", "comparison", "settings_route")
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = tween(400), initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
                exit = slideOutVertically(animationSpec = tween(400), targetOffsetY = { it }) + fadeOut(animationSpec = tween(400))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val items = listOf(
                        BottomNavItem.Main,
                        BottomNavItem.Search,
                        BottomNavItem.Comparison,
                        BottomNavItem.Settings
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues),
        enterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
                androidx.compose.animation.EnterTransition.None
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            }
        },
        exitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
                androidx.compose.animation.ExitTransition.None
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        },
        popEnterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
                androidx.compose.animation.EnterTransition.None
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            }
        },
        popExitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route
            if (fromRoute in bottomBarRoutes && toRoute in bottomBarRoutes) {
                androidx.compose.animation.ExitTransition.None
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        }
    ) {
        composable("main") {
            val recentlyViewedItems by settingsViewModel.recentlyViewedItems.collectAsState()
            val recentlyViewedLimit by settingsViewModel.recentlyViewedLimit.collectAsState()
            MainScreen(
                recentlyViewedItems = recentlyViewedItems,
                recentlyViewedLimit = recentlyViewedLimit,
                bottomPadding = paddingValues.calculateBottomPadding(),
                onSearchClicked = { ean ->
                    navController.navigate("details/$ean")
                },
                onScanClicked = {
                    navController.navigate("scanner")
                }
            )
        }
        composable("search") {
            SearchScreen(
                viewModel = searchViewModel,
                settingsViewModel = settingsViewModel,
                bottomPadding = paddingValues.calculateBottomPadding(),
                onProductClicked = { ean ->
                    navController.navigate("details/$ean")
                }
            )
        }
        composable("settings_route") {
            SettingsScreen(
                viewModel = settingsViewModel,
                bottomPadding = paddingValues.calculateBottomPadding()
            )
        }
        composable("comparison") {
            val showHighlightedIngredients by settingsViewModel.showHighlightedIngredients.collectAsState()
            ComparisonScreen(
                viewModel = comparisonViewModel,
                showHighlightedIngredients = showHighlightedIngredients,
                bottomPadding = paddingValues.calculateBottomPadding(),
                onProductClicked = { ean ->
                    navController.navigate("details/$ean")
                }
            )
        }
        composable("scanner") {
            ScannerScreen(
                onBarcodeDetected = { barcode ->
                    // Automatically search when scanned
                    navController.popBackStack("main", inclusive = false)
                    navController.navigate("details/$barcode")
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "details/{ean}",
            arguments = listOf(navArgument("ean") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://zdrowezakupy.org/product/{ean}" })
        ) { backStackEntry ->
            val ean = backStackEntry.arguments?.getString("ean") ?: ""
            val uiState by viewModel.uiState.collectAsState()
            val showGroupedIngredients by settingsViewModel.showGroupedIngredients.collectAsState()
            val showNutritionProgressBars by settingsViewModel.showNutritionProgressBars.collectAsState()
            val showHighlightedIngredients by settingsViewModel.showHighlightedIngredients.collectAsState()
            val detailsSectionOrder by settingsViewModel.detailsSectionOrder.collectAsState()
            val hiddenDetailsSections by settingsViewModel.hiddenDetailsSections.collectAsState()
            val comparisonEans by settingsViewModel.comparisonEans.collectAsState()
            val isProductInComparison = ean in comparisonEans

            LaunchedEffect(ean) {
                if (ean.isNotBlank()) {
                    val currentState = viewModel.uiState.value
                    if (currentState !is ProductUiState.Success || currentState.product.ean != ean) {
                        viewModel.getProduct(ean)
                    }
                }
            }

            LaunchedEffect(uiState) {
                if (uiState is ProductUiState.Success) {
                    viewModel.addToHistory((uiState as ProductUiState.Success).product)
                }
            }

            val context = LocalContext.current

            DetailsScreen(
                uiState = uiState,
                showGroupedIngredients = showGroupedIngredients,
                showNutritionProgressBars = showNutritionProgressBars,
                showHighlightedIngredients = showHighlightedIngredients,
                detailsSectionOrder = detailsSectionOrder,
                hiddenDetailsSections = hiddenDetailsSections,
                isProductInComparison = isProductInComparison,
                onToggleComparison = {
                    if (isProductInComparison) {
                        settingsViewModel.removeFromComparison(ean)
                    } else {
                        settingsViewModel.addToComparison(ean)
                    }
                },
                onBackClicked = {
                    val hasPrevious = navController.previousBackStackEntry != null
                    viewModel.resetState()
                    if (hasPrevious) {
                        navController.popBackStack()
                    } else {
                        (context as? Activity)?.finish()
                    }
                },
                onRetry = {
                    if (ean.isNotBlank()) {
                        viewModel.getProduct(ean)
                    } else {
                        val hasPrevious = navController.previousBackStackEntry != null
                        viewModel.resetState()
                        if (hasPrevious) {
                            navController.popBackStack()
                        } else {
                            (context as? Activity)?.finish()
                        }
                    }
                },
                onImageClicked = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("full_screen_image/$encodedUrl")
                }
            )
        }
        composable(
            route = "full_screen_image/{imageUrl}",
            arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val imageUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            FullScreenImageScreen(
                imageUrl = imageUrl,
                onBackClicked = { navController.popBackStack() }
            )
        }
        }
    }
}
