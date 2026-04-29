package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val visibleNutrients by viewModel.visibleNutrients.collectAsState()
    val nutrientColors by viewModel.nutrientColors.collectAsState()
    val showTemporaryNutrient by viewModel.showTemporaryNutrient.collectAsState()
    val uniformNutrientWidth by viewModel.uniformNutrientWidth.collectAsState()
    val searchAutoFocusOption by viewModel.searchAutoFocusOption.collectAsState()
    
    var showSearchFocusDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wyszukiwarka") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
            SettingsItemClickable(
                title = "Automatyczne skupienie na szukaniu",
                subtitle = getAutoFocusOptionDisplayName(searchAutoFocusOption),
                onClick = { showSearchFocusDialog = true }
            )

            SettingsItemSwitch(
                title = "Pokaż sortowaną wartość",
                subtitle = "Jeśli sortujesz według wartości odżwyczej, która nie jest wybrany z listy, zostanie ona tymczasowo dodana do widoku",
                checked = showTemporaryNutrient,
                onCheckedChange = { viewModel.setShowTemporaryNutrient(it) }
            )

            SettingsItemSwitch(
                title = "Wyrównaj podgląd wartości",
                subtitle = "Każda etykieta będzie miała minimalną szerokość, co ułatwi porównywanie wartości na pierwszy rzut oka",
                checked = uniformNutrientWidth,
                onCheckedChange = { viewModel.setUniformNutrientWidth(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Text(
                text = "Lista wartości na podglądzie",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            Text(
                text = "Wybierz wartości odżywcze, które chcesz widzieć bezpośrednio na liście produktów",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
        }
    }

    if (showSearchFocusDialog) {
        Dialog(onDismissRequest = { showSearchFocusDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Automatyczne skupienie",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SearchAutoFocusOption.entries.forEach { option ->
                        key(option) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.setSearchAutoFocusOption(option)
                                        showSearchFocusDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = option == searchAutoFocusOption,
                                    onClick = {
                                        viewModel.setSearchAutoFocusOption(option)
                                        showSearchFocusDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = getAutoFocusOptionDisplayName(option),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showSearchFocusDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}
