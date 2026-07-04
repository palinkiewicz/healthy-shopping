package pl.dakil.healthyshopping.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.ui.components.flatTopAppBarColors
import pl.dakil.healthyshopping.ui.screens.settings.AboutAppDialog
import pl.dakil.healthyshopping.ui.screens.settings.SettingRow
import pl.dakil.healthyshopping.ui.screens.settings.NavigationRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateAppearance: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateProductDetails: () -> Unit,
    onNavigateHistory: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                colors = flatTopAppBarColors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            NavigationRow(
                title = "Wygląd",
                summary = "Motyw i kolory aplikacji",
                icon = Icons.Rounded.Brush,
                onClick = onNavigateAppearance
            )

            NavigationRow(
                title = "Wyszukiwarka",
                summary = "Skupienie, sortowanie, podgląd składników",
                icon = Icons.Rounded.Search,
                onClick = onNavigateSearch
            )

            NavigationRow(
                title = "Szczegóły produktu",
                summary = "Sekcje, składniki, wskazówki GDA",
                icon = Icons.Rounded.ShoppingBag,
                onClick = onNavigateProductDetails
            )

            NavigationRow(
                title = "Historia",
                summary = "Liczba produktów, czyszczenie",
                icon = Icons.Rounded.History,
                onClick = onNavigateHistory
            )

            SettingRow(
                title = "O aplikacji",
                summary = "Wersja, autorzy, repozytorium",
                leading = { Icon(Icons.Rounded.Info, contentDescription = null) },
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
}
