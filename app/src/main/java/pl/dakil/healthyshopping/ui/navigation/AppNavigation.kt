package pl.dakil.healthyshopping.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.dakil.healthyshopping.ui.screens.DetailsScreen
import pl.dakil.healthyshopping.ui.screens.MainScreen
import pl.dakil.healthyshopping.ui.screens.SearchScreen
import pl.dakil.healthyshopping.ui.screens.SettingsScreen
import pl.dakil.healthyshopping.ui.scanner.ScannerScreen
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel

sealed class BottomNavItem(var title: String, var icon: androidx.compose.ui.graphics.vector.ImageVector, var route: String) {
    data object Main : BottomNavItem("Główna", Icons.Default.Home, "main")
    data object Search : BottomNavItem("Szukaj", Icons.Default.Search, "search")
    data object Settings : BottomNavItem("Ustawienia", Icons.Default.Settings, "settings_route")
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("main", "search", "settings_route")
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val items = listOf(
                        BottomNavItem.Main,
                        BottomNavItem.Search,
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
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
            Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                MainScreen(
                    onSearchClicked = { ean ->
                        viewModel.getProduct(ean)
                        navController.navigate("details")
                    },
                    onScanClicked = {
                        navController.navigate("scanner")
                    }
                )
            }
        }
        composable("search") {
            Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onProductClicked = { ean ->
                        viewModel.getProduct(ean)
                        navController.navigate("details")
                    }
                )
            }
        }
        composable("settings_route") {
            Box(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClicked = { navController.popBackStack() }
                )
            }
        }
        composable("scanner") {
            ScannerScreen(
                onBarcodeDetected = { barcode ->
                    // Automatically search when scanned
                    navController.popBackStack("main", inclusive = false)
                    viewModel.getProduct(barcode)
                    navController.navigate("details")
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
        composable("details") {
            val uiState by viewModel.uiState.collectAsState()
            val showGroupedIngredients by settingsViewModel.showGroupedIngredients.collectAsState()
            val showNutritionProgressBars by settingsViewModel.showNutritionProgressBars.collectAsState()
            val showHighlightedIngredients by settingsViewModel.showHighlightedIngredients.collectAsState()
            val showProductTags by settingsViewModel.showProductTags.collectAsState()

            DetailsScreen(
                uiState = uiState,
                showGroupedIngredients = showGroupedIngredients,
                showNutritionProgressBars = showNutritionProgressBars,
                showHighlightedIngredients = showHighlightedIngredients,
                showProductTags = showProductTags,
                onBackClicked = {
                    viewModel.resetState()
                    navController.popBackStack()
                },
                onRetry = {
                    // Retry logic or simply go back (can implement a retry callback in ViewModel later)
                    viewModel.resetState()
                    navController.popBackStack()
                }
            )
        }
        }
    }
}
