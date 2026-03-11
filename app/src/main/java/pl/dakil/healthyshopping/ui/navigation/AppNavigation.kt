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
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
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
                }
            )
        }
        composable("details") {
            val uiState by viewModel.uiState.collectAsState()

            DetailsScreen(
                uiState = uiState,
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
