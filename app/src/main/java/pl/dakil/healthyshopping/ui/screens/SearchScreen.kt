package pl.dakil.healthyshopping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.dakil.healthyshopping.data.model.SearchProduct
import pl.dakil.healthyshopping.ui.viewmodel.SearchUiState
import pl.dakil.healthyshopping.ui.viewmodel.SearchViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SettingsViewModel
import pl.dakil.healthyshopping.ui.viewmodel.SearchSort
import pl.dakil.healthyshopping.ui.viewmodel.SortType
import pl.dakil.healthyshopping.ui.viewmodel.SortOrder
import pl.dakil.healthyshopping.data.repository.AVAILABLE_NUTRIENTS
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
    onProductClicked: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val visibleNutrients by settingsViewModel.visibleNutrients.collectAsState()
    val nutrientColors by settingsViewModel.nutrientColors.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Wpisz nazwę produktu...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            if (uiState is SearchUiState.Success) {
                SortSection(
                    currentSort = sort,
                    onSortChange = { viewModel.setSort(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is SearchUiState.Idle -> {
                        SearchTutorial(query)
                    }
                    is SearchUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is SearchUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is SearchUiState.Success -> {
                        if (state.products.isEmpty()) {
                            Text(
                                text = "Brak wyników",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.products) { product ->
                                    ProductCard(
                                        product = product,
                                        visibleNutrientIds = visibleNutrients,
                                        nutrientColors = nutrientColors,
                                        onClick = { product.ean?.let { onProductClicked(it) } }
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

@Composable
fun ProductCard(
    product: SearchProduct,
    visibleNutrientIds: Set<String>,
    nutrientColors: Map<String, String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image -> 1:1, handling null with icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.image?.url != null) {
                    AsyncImage(
                        model = product.image.url,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "Brak zdjęcia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name ?: "Nieznany produkt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                NutrientPreviewRow(
                    product = product,
                    visibleNutrientIds = visibleNutrientIds,
                    nutrientColors = nutrientColors
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Score Box
            val scoreColorHex = product.score?.color ?: "#CCCCCC"
            val scoreColor = try {
                Color(android.graphics.Color.parseColor(scoreColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(scoreColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.score?.value?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SearchTutorial(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val trimmed = query.trim()
        val isShort = trimmed.isNotEmpty() && trimmed.length < 3
        
        val icon = if (isShort) Icons.Default.Info else Icons.Default.Search
        val iconColor = if (isShort) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = iconColor
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val title = if (isShort) "Wpisz jeszcze trochę..." else "Zacznij szukać!"
        val description = if (isShort) {
            val remaining = 3 - trimmed.length
            "Wyszukiwarka potrzebuje co najmniej 3 znaków. Wpisz jeszcze co najmniej $remaining ${if (remaining == 1) "znak" else "znaki"}."
        } else {
            "Wpisz nazwę produktu, aby sprawdzić jego skład i wpływ na zdrowie."
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TutorialItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SortSection(
    currentSort: SearchSort,
    onSortChange: (SearchSort) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Score Sort
        SortChip(
            label = "Wynik",
            isSelected = currentSort.type == SortType.SCORE,
            order = if (currentSort.type == SortType.SCORE) currentSort.order else null,
            onClick = {
                val newOrder = if (currentSort.type == SortType.SCORE && currentSort.order == SortOrder.DESCENDING) {
                    SortOrder.ASCENDING
                } else {
                    SortOrder.DESCENDING
                }
                onSortChange(SearchSort(type = SortType.SCORE, order = newOrder))
            }
        )

        // Nutrient Sorts
        AVAILABLE_NUTRIENTS.forEach { nutrient ->
            SortChip(
                label = nutrient.name,
                isSelected = currentSort.type == SortType.NUTRIENT && currentSort.nutrientId == nutrient.id,
                order = if (currentSort.type == SortType.NUTRIENT && currentSort.nutrientId == nutrient.id) currentSort.order else null,
                onClick = {
                    val newOrder = if (currentSort.type == SortType.NUTRIENT && currentSort.nutrientId == nutrient.id && currentSort.order == SortOrder.DESCENDING) {
                        SortOrder.ASCENDING
                    } else {
                        SortOrder.DESCENDING
                    }
                    onSortChange(SearchSort(type = SortType.NUTRIENT, order = newOrder, nutrientId = nutrient.id))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortChip(
    label: String,
    isSelected: Boolean,
    order: SortOrder?,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = if (order == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutrientPreviewRow(
    product: SearchProduct,
    visibleNutrientIds: Set<String>,
    nutrientColors: Map<String, String>
) {
    val nutrientsToShow = product.nutrients?.nutrients?.filter { 
        visibleNutrientIds.contains(it.id) 
    } ?: emptyList()

    if (nutrientsToShow.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        nutrientsToShow.forEach { nutrient ->
            val colorHex = nutrientColors[nutrient.id] ?: "#CCCCCC"
            val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
            
            val value = nutrient.details?.value ?: ""
            val unit = nutrient.details?.unit ?: ""
            
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
