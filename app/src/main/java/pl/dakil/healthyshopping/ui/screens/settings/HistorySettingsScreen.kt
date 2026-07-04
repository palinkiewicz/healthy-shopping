package pl.dakil.healthyshopping.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.ui.components.flatTopAppBarColors
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val recentlyViewedLimit by viewModel.recentlyViewedLimit.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historia przeglądania") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = flatTopAppBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SliderRow(
                title = "Liczba wyświetlanych produktów",
                summary = if (recentlyViewedLimit == 0) "Wyłączono wyświetlanie historii" else "Pokazuje ostatnie $recentlyViewedLimit odwiedzonych produktów",
                value = recentlyViewedLimit,
                onValueChange = { viewModel.setRecentlyViewedLimit(it) },
                valueRange = 0..10,
                steps = 9
            )

            SettingRow(
                title = "Wyczyść historię",
                summary = "Usuń wszystkie ostatnio przeglądane produkty",
                leading = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    viewModel.clearRecentlyViewed()
                    Toast.makeText(context, "Historia została wyczyszczona", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}
