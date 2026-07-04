package pl.dakil.healthyshopping.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import pl.dakil.healthyshopping.data.repository.SearchAutoFocusOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    settingsViewModel: SettingsViewModel,
    bottomPadding: Dp = 0.dp,
    onProductClicked: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val visibleNutrients by settingsViewModel.visibleNutrients.collectAsState()
    val nutrientColors by settingsViewModel.nutrientColors.collectAsState()
    val showTemporaryNutrient by settingsViewModel.showTemporaryNutrient.collectAsState()
    val uniformNutrientWidth by settingsViewModel.uniformNutrientWidth.collectAsState()
    val nutrientWidth by settingsViewModel.nutrientWidth.collectAsState()
    val searchAutoFocusOption by settingsViewModel.searchAutoFocusOption.collectAsState()
    val comparisonEans by settingsViewModel.comparisonEans.collectAsState()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(query)) }

    LaunchedEffect(query) {
        if (textFieldValue.text != query) {
            textFieldValue = textFieldValue.copy(text = query)
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val shouldFocus = when (searchAutoFocusOption) {
            SearchAutoFocusOption.ALWAYS -> true
            SearchAutoFocusOption.EMPTY_FIELD -> query.isEmpty()
            SearchAutoFocusOption.NEVER -> false
        }
        if (shouldFocus) {
            focusRequester.requestFocus()
            if (searchAutoFocusOption == SearchAutoFocusOption.ALWAYS) {
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(textFieldValue.text.length)
                )
            }
        }
    }

    val effectiveVisibleNutrients = remember(visibleNutrients, sort, showTemporaryNutrient) {
        val nutrientId = sort.nutrientId
        if (showTemporaryNutrient && sort.type == SortType.NUTRIENT && nutrientId != null) {
            visibleNutrients + nutrientId
        } else {
            visibleNutrients
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomPadding)
        ) {
            // Search Bar — fully rounded and filled, like a real search bar
            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (it.text != query) {
                        viewModel.onSearchQueryChange(it.text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Wpisz nazwę produktu...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
                trailingIcon = if (textFieldValue.text.isNotEmpty()) {
                    {
                        IconButton(onClick = {
                            textFieldValue = TextFieldValue("")
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                        }
                    }
                } else null,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
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
                        pl.dakil.healthyshopping.ui.components.StandardError(
                            errorType = state.errorType,
                            customMessage = state.message,
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
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(state.products) { product ->
                                    ProductListItem(
                                        product = product,
                                        visibleNutrientIds = effectiveVisibleNutrients,
                                        nutrientColors = nutrientColors,
                                        sort = sort,
                                        uniformNutrientWidth = uniformNutrientWidth,
                                        nutrientWidth = nutrientWidth,
                                        isInComparison = product.ean != null && product.ean in comparisonEans,
                                        onClick = { product.ean?.let { onProductClicked(it) } },
                                        onToggleComparison = {
                                            product.ean?.let { ean ->
                                                if (ean in comparisonEans) {
                                                    settingsViewModel.removeFromComparison(ean)
                                                } else {
                                                    settingsViewModel.addToComparison(ean)
                                                }
                                            }
                                        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductListItem(
    product: SearchProduct,
    visibleNutrientIds: Set<String>,
    nutrientColors: Map<String, String>,
    sort: SearchSort,
    uniformNutrientWidth: Boolean,
    nutrientWidth: Int,
    isInComparison: Boolean,
    onClick: () -> Unit,
    onToggleComparison: () -> Unit
) {
    val context = LocalContext.current
    var showContextMenu by remember { mutableStateOf(false) }

    Box {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            ),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            headlineContent = {
                Text(
                    text = product.name ?: "Nieznany produkt",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                NutrientPreviewRow(
                    product = product,
                    visibleNutrientIds = visibleNutrientIds,
                    nutrientColors = nutrientColors,
                    sort = sort,
                    uniformNutrientWidth = uniformNutrientWidth,
                    nutrientWidth = nutrientWidth
                )
            },
            trailingContent = {
                val scoreColorHex = product.score?.color ?: "#CCCCCC"
                val scoreColor = try {
                    Color(android.graphics.Color.parseColor(scoreColorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
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
        )

        // Context menu anchored to the bottom-right corner of the item.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
        ) {
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isInComparison) "Usuń z porównywarki" else "Dodaj do porównywarki") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isInComparison) Icons.Default.CheckCircle else Icons.Default.AddChart,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onToggleComparison()
                        showContextMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Udostępnij") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = {
                        val shareText = buildString {
                            product.name?.let { append(it).append("\n") }
                            append("https://zdrowezakupy.org/product/${product.ean}")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                        showContextMenu = false
                    }
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
        } else null
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutrientPreviewRow(
    product: SearchProduct,
    visibleNutrientIds: Set<String>,
    nutrientColors: Map<String, String>,
    sort: SearchSort,
    uniformNutrientWidth: Boolean,
    nutrientWidth: Int
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
            val displayValue = if (value.isEmpty()) "-" else "$value $unit"
            
            val isSortedNutrient = sort.type == SortType.NUTRIENT && sort.nutrientId == nutrient.id
            
            Text(
                text = displayValue,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = if (isSortedNutrient) FontWeight.Bold else FontWeight.Normal,
                modifier = if (uniformNutrientWidth) Modifier.widthIn(min = nutrientWidth.dp) else Modifier
            )
        }
    }
}
