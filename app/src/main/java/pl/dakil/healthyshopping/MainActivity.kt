package pl.dakil.healthyshopping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.dakil.healthyshopping.data.network.RetrofitClient
import pl.dakil.healthyshopping.data.repository.ProductRepository
import pl.dakil.healthyshopping.ui.navigation.AppNavigation
import pl.dakil.healthyshopping.ui.theme.HealthyShoppingTheme
import pl.dakil.healthyshopping.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = ProductRepository(RetrofitClient.apiService)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthyShoppingTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}