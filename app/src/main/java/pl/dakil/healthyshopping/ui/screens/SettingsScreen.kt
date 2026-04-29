package pl.dakil.healthyshopping.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.ui.screens.settings.SettingsCategoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateAppearance: () -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateProductDetails: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateAbout: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCategoryItem(
                icon = Icons.Default.Brush,
                title = "Wygląd",
                subtitle = "Motyw aplikacji",
                onClick = onNavigateAppearance
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )

            SettingsCategoryItem(
                icon = Icons.Default.Search,
                title = "Wyszukiwarka",
                subtitle = "Skupienie, sortowanie, podgląd składników",
                onClick = onNavigateSearch
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )

            SettingsCategoryItem(
                icon = Icons.Default.ShoppingBag,
                title = "Szczegóły produktu",
                subtitle = "Sekcje, składniki, wskazówki GDA",
                onClick = onNavigateProductDetails
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )

            SettingsCategoryItem(
                icon = Icons.Default.History,
                title = "Historia",
                subtitle = "Liczba produktów, czyszczenie",
                onClick = onNavigateHistory
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )

            SettingsCategoryItem(
                icon = Icons.Default.Info,
                title = "O aplikacji",
                subtitle = "Wersja, autorzy, repozytorium",
                onClick = onNavigateAbout
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
