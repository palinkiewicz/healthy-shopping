package pl.dakil.healthyshopping.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import pl.dakil.healthyshopping.data.repository.ThemePreset
import pl.dakil.healthyshopping.ui.theme.HealthyShoppingTheme
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import pl.dakil.healthyshopping.data.repository.DetailsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    bottomPadding: Dp = 0.dp
) {
    val themePreset by viewModel.themePreset.collectAsState()
    val showGroupedIngredients by viewModel.showGroupedIngredients.collectAsState()
    val showNutritionProgressBars by viewModel.showNutritionProgressBars.collectAsState()
    val showHighlightedIngredients by viewModel.showHighlightedIngredients.collectAsState()
    val showProductTags by viewModel.showProductTags.collectAsState()
    val visibleNutrients by viewModel.visibleNutrients.collectAsState()
    val nutrientColors by viewModel.nutrientColors.collectAsState()
    val detailsSectionOrder by viewModel.detailsSectionOrder.collectAsState()
    val hiddenDetailsSections by viewModel.hiddenDetailsSections.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
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
            
            SettingsCategoryHeader("Wygląd aplikacji")
            
            SettingsItemClickable(
                title = "Motyw aplikacji",
                subtitle = getPresetDisplayName(themePreset),
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            SettingsCategoryHeader("Szczegóły produktu")

            SettingsItemSwitch(
                title = "Grupuj szkodliwe składniki",
                subtitle = "Ustaw domyślny widok grupowania składników na ekranie produktu.",
                checked = showGroupedIngredients,
                onCheckedChange = { viewModel.setShowGroupedIngredients(it) }
            )

            SettingsItemSwitch(
                title = "Wskazówki GDA / Paski postępu",
                subtitle = "Wyświetlaj procent dziennego zapotrzebowania przy tabeli wartości odżywczych na 100g.",
                checked = showNutritionProgressBars,
                onCheckedChange = { viewModel.setShowNutritionProgressBars(it) }
            )

            SettingsItemSwitch(
                title = "Podświetl kluczowe składniki",
                subtitle = "Wyróżnij wybrane składniki w pełnym opisie produktu.",
                checked = showHighlightedIngredients,
                onCheckedChange = { viewModel.setShowHighlightedIngredients(it) }
            )

            SettingsItemSwitch(
                title = "Wyświetlaj tagi produktów",
                subtitle = "Pokazuj specjalne etykiety pod wynikiem zdrowotnym.",
                checked = showProductTags,
                onCheckedChange = { viewModel.setShowProductTags(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            SettingsCategoryHeader("Historia przeglądania")
            
            val recentlyViewedLimit by viewModel.recentlyViewedLimit.collectAsState()
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Liczba wyświetlanych produktów: $recentlyViewedLimit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (recentlyViewedLimit == 0) "Wyłączono wyświetlanie historii." else "Pokazuje ostatnie $recentlyViewedLimit odwiedzonych produktów na ekranie głównym.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Slider(
                    value = recentlyViewedLimit.toFloat(),
                    onValueChange = { viewModel.setRecentlyViewedLimit(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            val context = LocalContext.current
            TextButton(
                onClick = { 
                    viewModel.clearRecentlyViewed()
                    Toast.makeText(context, "Historia została wyczyszczona", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(start = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Wyczyść historię")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            SettingsCategoryHeader("Kolejność i widoczność sekcji")
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


            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            SettingsCategoryHeader("Podgląd na wynikach wyszukiwania")
            Text(
                text = "Wybierz wartości odżywcze, które chcesz widzieć bezpośrednio na liście produktów.",
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

    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
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
                        text = "Wybierz motyw",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    for (preset in ThemePreset.entries) {
                        val isDynamicOnOldAndroid = preset == ThemePreset.DYNAMIC && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                        
                        if (!isDynamicOnOldAndroid) {
                            key(preset) {
                                ThemePreviewRow(
                                    preset = preset,
                                    isSelected = preset == themePreset,
                                    onClick = {
                                        viewModel.setThemePreset(preset)
                                        showThemeDialog = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { showThemeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItemSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsItemClickable(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

fun getPresetDisplayName(preset: ThemePreset): String {
    return when(preset) {
        ThemePreset.SYSTEM -> "Systemowy"
        ThemePreset.DYNAMIC -> "Dynamic Color (Android 12+)"
        ThemePreset.LIGHT -> "Jasny"
        ThemePreset.DARK -> "Ciemny"
        ThemePreset.OLED -> "OLED (Czysta Czerń)"
        ThemePreset.SEPIA -> "Sepia (Ochrona Wzroku)"
        ThemePreset.FOREST -> "Forest (Zieleń)"
    }
}

@Composable
fun ThemePreviewRow(
    preset: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = getPresetDisplayName(preset),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        
        // Mini Preview Card
        HealthyShoppingTheme(themePreset = preset) {
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun NutrientSettingItem(
    name: String,
    isVisible: Boolean,
    colorHex: String,
    onToggleVisible: (Boolean) -> Unit,
    onColorChange: (String) -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }
    val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleVisible(!isVisible) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isVisible, onCheckedChange = onToggleVisible)
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                .clickable { showColorDialog = true }
        )
    }

    if (showColorDialog) {
        val presetColors = listOf(
            "#FFFFFF", "#2196F3", "#03A9F4", "#00BCD4", "#009688", 
            "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", 
            "#FF9800", "#FF5722", "#F44336", "#E91E63", "#9C27B0", 
            "#673AB7", "#3F51B5", "#795548", "#9E9E9E", "#607D8B"
        )

        Dialog(onDismissRequest = { showColorDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Wybierz kolor dla: $name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(presetColors.size) { index ->
                            val hex = presetColors[index]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (hex.lowercase() == colorHex.lowercase()) 3.dp else 1.dp,
                                        color = if (hex.lowercase() == colorHex.lowercase()) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onColorChange(hex)
                                        showColorDialog = false
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showColorDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}
