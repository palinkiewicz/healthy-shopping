package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.data.repository.DetailsSection
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val showGroupedIngredients by viewModel.showGroupedIngredients.collectAsState()
    val showNutritionProgressBars by viewModel.showNutritionProgressBars.collectAsState()
    val showHighlightedIngredients by viewModel.showHighlightedIngredients.collectAsState()
    val showProductTags by viewModel.showProductTags.collectAsState()
    val detailsSectionOrder by viewModel.detailsSectionOrder.collectAsState()
    val hiddenDetailsSections by viewModel.hiddenDetailsSections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły produktu") },
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
            SettingsItemSwitch(
                title = "Grupuj szkodliwe składniki",
                subtitle = "Szkodliwe składniki zostaną pogrupowane według szkodliwości",
                checked = showGroupedIngredients,
                onCheckedChange = { viewModel.setShowGroupedIngredients(it) }
            )

            SettingsItemSwitch(
                title = "Wskazówki GDA",
                subtitle = "Wyświetlaj procent dziennego zapotrzebowania przy tabeli wartości odżywczych na 100g",
                checked = showNutritionProgressBars,
                onCheckedChange = { viewModel.setShowNutritionProgressBars(it) }
            )

            SettingsItemSwitch(
                title = "Podświetl kluczowe składniki",
                subtitle = "Wyróżnij wybrane składniki w pełnym opisie produktu",
                checked = showHighlightedIngredients,
                onCheckedChange = { viewModel.setShowHighlightedIngredients(it) }
            )

            SettingsItemSwitch(
                title = "Wyświetlaj tagi produktu",
                subtitle = "Pokazuj specjalne etykiety pod wynikiem zdrowotnym",
                checked = showProductTags,
                onCheckedChange = { viewModel.setShowProductTags(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Text(
                text = "Kolejność i widoczność sekcji",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            
            Text(
                text = "Dostosuj układ ekranu produktu. Możesz ukryć sekcje lub zmienić ich kolejność.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    for (index in detailsSectionOrder.indices) {
                        val sectionId = detailsSectionOrder[index]
                        val section = DetailsSection.entries.find { it.id == sectionId }
                        if (section != null) {
                            key(sectionId) {
                                val isVisible = sectionId !in hiddenDetailsSections

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isVisible,
                                        onCheckedChange = { viewModel.setDetailsSectionVisible(sectionId, it) }
                                    )

                                    Text(
                                        text = section.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                        color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )

                                    IconButton(
                                        onClick = { viewModel.moveDetailsSection(index, index - 1) },
                                        enabled = index > 0
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Przesuń w górę")
                                    }

                                    IconButton(
                                        onClick = { viewModel.moveDetailsSection(index, index + 1) },
                                        enabled = index < detailsSectionOrder.size - 1
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Przesuń w dół")
                                    }
                                }

                                if (index < detailsSectionOrder.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
