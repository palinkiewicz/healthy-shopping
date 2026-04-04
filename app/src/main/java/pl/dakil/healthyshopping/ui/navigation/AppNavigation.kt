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
import pl.dakil.healthyshopping.ui.screens.DetailsScreen
import pl.dakil.healthyshopping.ui.screens.MainScreen
import pl.dakil.healthyshopping.ui.screens.SettingsScreen
import pl.dakil.healthyshopping.ui.scanner.ScannerScreen
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("main") {
            MainScreen(
                onSearchClicked = { ean ->
                    viewModel.getProduct(ean)
                    navController.navigate("details")
                },
                onScanClicked = {
                    navController.navigate("scanner")
                },
                onSettingsClicked = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClicked = { navController.popBackStack() }
            )
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
