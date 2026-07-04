package pl.dakil.healthyshopping.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.data.repository.DetailsSection
import pl.dakil.healthyshopping.ui.components.flatTopAppBarColors
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    bottomPadding: Dp = 0.dp
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
            SectionHeader("Wyświetlanie")

            SwitchRow(
                title = "Grupuj szkodliwe składniki",
                summary = "Szkodliwe składniki zostaną pogrupowane według szkodliwości",
                checked = showGroupedIngredients,
                onCheckedChange = { viewModel.setShowGroupedIngredients(it) }
            )

            SwitchRow(
                title = "Wskazówki GDA",
                summary = "Wyświetlaj procent dziennego zapotrzebowania przy tabeli wartości odżywczych na 100g",
                checked = showNutritionProgressBars,
                onCheckedChange = { viewModel.setShowNutritionProgressBars(it) }
            )

            SwitchRow(
                title = "Podświetl kluczowe składniki",
                summary = "Wyróżnij wybrane składniki w pełnym opisie produktu",
                checked = showHighlightedIngredients,
                onCheckedChange = { viewModel.setShowHighlightedIngredients(it) }
            )

            SwitchRow(
                title = "Wyświetlaj tagi produktu",
                summary = "Pokazuj specjalne etykiety pod wynikiem zdrowotnym",
                checked = showProductTags,
                onCheckedChange = { viewModel.setShowProductTags(it) }
            )

            SectionHeader("Kolejność i widoczność sekcji")

            Text(
                text = "Dostosuj układ ekranu produktu. Możesz ukryć sekcje lub zmienić ich kolejność.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

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
                                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Przesuń w górę")
                            }

                            IconButton(
                                onClick = { viewModel.moveDetailsSection(index, index + 1) },
                                enabled = index < detailsSectionOrder.size - 1
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Przesuń w dół")
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

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}
