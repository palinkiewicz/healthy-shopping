package pl.dakil.healthyshopping.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.ui.theme.ingredient_light_green
import pl.dakil.healthyshopping.ui.theme.ingredient_light_red
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonUiState
import pl.dakil.healthyshopping.ui.viewmodel.ComparisonViewModel
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import pl.dakil.healthyshopping.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel,
    showHighlightedIngredients: Boolean,
    bottomPadding: Dp = 0.dp,
    onProductClicked: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Porównywarka") },
                actions = {
                    if (uiState is ComparisonUiState.Success && (uiState as ComparisonUiState.Success).products.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearComparison() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Wyczyść wszystko")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ComparisonUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is ComparisonUiState.Success -> {
                    val products = (uiState as ComparisonUiState.Success).products
                    if (products.isEmpty()) {
                        EmptyComparisonContent()
                    } else {
                        ComparisonTable(
                            products = products,
                            showHighlightedIngredients = showHighlightedIngredients,
                            onProductClicked = onProductClicked
                        )
                    }
                }
                is ComparisonUiState.Error -> {
                    ErrorContent(
                        message = (uiState as ComparisonUiState.Error).message,
                        onRetry = { viewModel.refreshComparison() }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ComparisonTable(
    products: List<ProductResponse>,
    showHighlightedIngredients: Boolean,
    onProductClicked: (String) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val labelWidth = 140.dp
    val columnWidth = 180.dp

    Column(modifier = Modifier.fillMaxSize()) {
        // Table Header (Sticky vertically)
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Top-left corner cell
            Box(modifier = Modifier.width(labelWidth).height(200.dp))

            products.forEach { product ->
                Column(
                    modifier = Modifier
                        .width(columnWidth)
                        .height(200.dp)
                        .clickable { product.ean?.let { onProductClicked(it) } }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (product.image?.url != null) {
                        AsyncImage(
                            model = product.image.url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.name ?: "Nieznany",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Table Body
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Table Rows
                DataTableContent(
                    products = products,
                    labelWidth = labelWidth,
                    columnWidth = columnWidth,
                    horizontalScrollState = horizontalScrollState,
                    showHighlightedIngredients = showHighlightedIngredients
                )
            }
        }
    }
}

@Composable
fun DataTableContent(
    products: List<ProductResponse>,
    labelWidth: androidx.compose.ui.unit.Dp,
    columnWidth: androidx.compose.ui.unit.Dp,
    horizontalScrollState: ScrollState,
    showHighlightedIngredients: Boolean
) {
    // 1. Health Score
    TableDataRow(
        label = "Wynik",
        labelWidth = labelWidth,
        columnWidth = columnWidth,
        horizontalScrollState = horizontalScrollState,
        items = products
    ) { product ->
        val score = product.score?.value
        val colorHex = product.score?.color
        
        val defaultScoreColor = MaterialTheme.colorScheme.primary
        val scoreColor = remember(colorHex, defaultScoreColor) {
            try {
                if (colorHex != null) Color(android.graphics.Color.parseColor(colorHex)) else defaultScoreColor
            } catch (e: Exception) {
                defaultScoreColor
            }
        }

        if (score != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(scoreColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            DataMissingText()
        }
    }

    // 3. Nutrients
    val nutrientLabels = listOf(
        "Energia" to listOf("energetyczna", "energia", "kalorie"),
        "Tłuszcz" to listOf("tłuszcz"),
        "Kw. nasycone" to listOf("nasycone"),
        "Węglowodany" to listOf("węglowodany"),
        "Cukry" to listOf("cukry"),
        "Białko" to listOf("białko"),
        "Sól" to listOf("sól"),
        "Błonnik" to listOf("błonnik")
    )

    SectionHeader("Wartości odżywcze (100g/ml)", labelWidth, products.size, columnWidth, horizontalScrollState)

    nutrientLabels.forEach { (displayName, searchKeys) ->
        TableDataRow(
            label = displayName,
            labelWidth = labelWidth,
            columnWidth = columnWidth,
            horizontalScrollState = horizontalScrollState,
            items = products
        ) { product ->
            val nutrient = product.nutrientValues?.nutrients?.find { nut ->
                val lowerName = nut.name?.lowercase() ?: ""
                searchKeys.any { key -> lowerName.contains(key) }
            }
            if (nutrient?.details?.value != null) {
                Text(
                    text = "${nutrient.details.value} ${nutrient.details.unit ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                DataMissingText()
            }
        }
    }

    // 4. Ingredients Counts
    SectionHeader("Składniki", labelWidth, products.size, columnWidth, horizontalScrollState)

    val harmfulLevels = listOf(5, 4, 3, 2, 1)
    val levelNames = listOf("B. szkodliwe", "Szkodliwe", "Podejrzane", "Neutralne", "Korzystne")

    harmfulLevels.forEachIndexed { index, level ->
        TableDataRow(
            label = levelNames[index],
            labelWidth = labelWidth,
            columnWidth = columnWidth,
            horizontalScrollState = horizontalScrollState,
            items = products
        ) { product ->
            val levelIngredients = product.ingredients?.filter { it.harmfulLevel == level } ?: emptyList()
            if (levelIngredients.isNotEmpty()) {
                val names = levelIngredients.map { it.displayName ?: it.name ?: "Nieznany" }.joinToString(", ")
                Text(
                    text = names,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            } else {
                Text(text = "brak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }

    // 5. Full Description
    SectionHeader("Pełny skład", labelWidth, products.size, columnWidth, horizontalScrollState)
    TableDataRow(
        label = "Opis",
        labelWidth = labelWidth,
        columnWidth = columnWidth,
        horizontalScrollState = horizontalScrollState,
        items = products
    ) { product ->
        val description = product.description
        if (!description.isNullOrBlank()) {
            val isDark = isSystemInDarkTheme()
            val annotatedString = if (showHighlightedIngredients && !product.ingredientPhrases.isNullOrEmpty()) {
                buildAnnotatedString {
                    append(description)
                    product.ingredientPhrases.forEach { phraseInfo ->
                        val phrase = phraseInfo.phrase ?: return@forEach
                        val apiHex = phraseInfo.backgroundColor ?: ""
                        val colors = translateComparisonIngredientColor(apiHex, isDark)
                        
                        var start = description.indexOf(phrase, ignoreCase = true)
                        while (start != -1) {
                            addStyle(
                                SpanStyle(background = colors.first, color = colors.second),
                                start,
                                start + phrase.length
                            )
                            start = description.indexOf(phrase, start + phrase.length, ignoreCase = true)
                        }
                    }
                }
            } else {
                buildAnnotatedString { append(description) }
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(4.dp)
            )
        } else {
            DataMissingText()
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
}



@Composable
fun <T> TableDataRow(
    label: String,
    labelWidth: androidx.compose.ui.unit.Dp,
    columnWidth: androidx.compose.ui.unit.Dp,
    horizontalScrollState: ScrollState,
    items: List<T>,
    content: @Composable (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
            .height(IntrinsicSize.Min)
    ) {
        // Fixed label column part
        Box(
            modifier = Modifier
                .width(labelWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items.forEach { item ->
            Box(
                modifier = Modifier
                    .width(columnWidth)
                    .fillMaxHeight()
                    .padding(12.dp)
                    .drawBehind {
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, 0f),
                            end = Offset(0f, this.size.height),
                            strokeWidth = 1f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                content(item)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun SectionHeader(
    title: String,
    labelWidth: androidx.compose.ui.unit.Dp,
    productsCount: Int,
    columnWidth: androidx.compose.ui.unit.Dp,
    horizontalScrollState: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(labelWidth + (columnWidth * productsCount)).padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
}

@Composable
fun DataMissingText() {
    Text(
        text = "Brak danych",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        textAlign = TextAlign.Center
    )
}

@Composable
fun EmptyComparisonContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconColor = MaterialTheme.colorScheme.primary
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = iconColor
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Twoja porównywarka jest pusta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Dodaj produkty, aby zobaczyć ich zestawienie i łatwiej wybrać najzdrowszą opcję.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Jak dodać produkty?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TutorialStep(
            number = "1",
            text = "Znajdź interesujący Cię produkt w wyszukiwarce lub zeskanuj kod."
        )
        
        TutorialStep(
            number = "2",
            text = "Na ekranie szczegółów produktu kliknij ikonę plusa (+) w górnym pasku."
        )
        
        TutorialStep(
            number = "3",
            text = "Wróć tutaj, aby zobaczyć porównanie dodanych produktów."
        )
    }
}

@Composable
fun TutorialStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun translateComparisonIngredientColor(apiHex: String, isDark: Boolean): Pair<Color, Color> {
    return when (apiHex.uppercase()) {
        "#D9F6E4" -> if (isDark) ingredient_dark_green to ingredient_dark_onGreen else ingredient_light_green to ingredient_light_onGreen
        "#EBF3CC" -> if (isDark) ingredient_dark_yellow to ingredient_dark_onYellow else ingredient_light_yellow to ingredient_light_onYellow
        "#FEEDD9" -> if (isDark) ingredient_dark_orange to ingredient_dark_onOrange else ingredient_light_orange to ingredient_light_onOrange
        "#FFF3C4" -> if (isDark) ingredient_dark_red to ingredient_dark_onRed else ingredient_light_red to ingredient_light_onRed
        else -> {
            val bgColor = try {
                Color(android.graphics.Color.parseColor(apiHex))
            } catch (e: Exception) {
                Color.Transparent
            }
            if (isDark) {
                bgColor.copy(alpha = 0.3f) to Color.White
            } else {
                bgColor.copy(alpha = 0.9f) to Color.Black
            }
        }
    }
}
