package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption
import pl.dakil.healthyshopping.ui.components.flatTopAppBarColors
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val visibleNutrients by viewModel.visibleNutrients.collectAsState()
    val nutrientColors by viewModel.nutrientColors.collectAsState()
    val showTemporaryNutrient by viewModel.showTemporaryNutrient.collectAsState()
    val uniformNutrientWidth by viewModel.uniformNutrientWidth.collectAsState()
    val nutrientWidth by viewModel.nutrientWidth.collectAsState()
    val searchAutoFocusOption by viewModel.searchAutoFocusOption.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wyszukiwarka") },
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
            SectionHeader("Ogólne")

            SelectRow(
                title = "Automatyczne skupienie",
                summary = "Kiedy pole wyszukiwania ma być aktywowane automatycznie",
                selectedLabel = getAutoFocusOptionDisplayName(searchAutoFocusOption),
                options = SearchAutoFocusOption.entries.map { it to getAutoFocusOptionDisplayName(it) },
                onSelect = { viewModel.setSearchAutoFocusOption(it) }
            )

            SwitchRow(
                title = "Pokaż sortowaną wartość",
                summary = "Jeśli sortujesz według wartości odżywczej, która nie jest wybrana z listy, zostanie ona tymczasowo dodana do widoku",
                checked = showTemporaryNutrient,
                onCheckedChange = { viewModel.setShowTemporaryNutrient(it) }
            )

            SwitchRow(
                title = "Wyrównaj podgląd wartości",
                summary = "Każda etykieta będzie miała minimalną szerokość, co ułatwi porównywanie wartości na pierwszy rzut oka",
                checked = uniformNutrientWidth,
                onCheckedChange = { viewModel.setUniformNutrientWidth(it) }
            )

            if (uniformNutrientWidth) {
                SliderRow(
                    title = "Szerokość etykiety wartości",
                    summary = "Minimalna szerokość etykiet wartości odżywczych na liście produktów",
                    value = nutrientWidth,
                    onValueChange = { viewModel.setNutrientWidth(it) },
                    valueRange = 32..128,
                    steps = 11,
                    valueLabel = { "${it}dp" }
                )
            }

            SectionHeader("Lista wartości na podglądzie")

            Text(
                text = "Wybierz wartości odżywcze, które chcesz widzieć bezpośrednio na liście produktów",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            AVAILABLE_NUTRIENTS.forEach { nutrient ->
                NutrientSettingItem(
                    name = nutrient.name,
                    isVisible = visibleNutrients.contains(nutrient.id),
                    colorHex = nutrientColors[nutrient.id] ?: nutrient.defaultColor,
                    onToggleVisible = { viewModel.setNutrientVisible(nutrient.id, it) },
                    onColorChange = { viewModel.setNutrientColor(nutrient.id, it) }
                )
            }

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}
