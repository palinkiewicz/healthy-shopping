package pl.dakil.healthyshopping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.dakil.healthyshopping.data.network.RetrofitClient
import pl.dakil.healthyshopping.data.repository.ProductRepository
import pl.dakil.healthyshopping.data.repository.SettingsRepository
import pl.dakil.healthyshopping.ui.navigation.AppNavigation
import pl.dakil.healthyshopping.ui.theme.HealthyShoppingTheme
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonViewModel

class MainActivity : ComponentActivity() {

    private val productRepository by lazy { ProductRepository(RetrofitClient.apiService) }
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(productRepository) as T
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }

    private val searchViewModel: SearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(productRepository, settingsRepository) as T
            }
        }
    }

    private val comparisonViewModel: ComparisonViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ComparisonViewModel(productRepository, settingsRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreset by settingsViewModel.themePreset.collectAsState()

            HealthyShoppingTheme(
                themePreset = themePreset
            ) {
                AppNavigation(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    searchViewModel = searchViewModel,
                    comparisonViewModel = comparisonViewModel
                )
            }
        }
    }
}