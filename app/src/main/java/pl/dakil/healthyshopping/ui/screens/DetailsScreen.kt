package pl.dakil.healthyshopping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import coil.compose.AsyncImage
import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.ui.viewmodel.ProductUiState
import pl.dakil.healthyshopping.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: ProductUiState,
    showGroupedIngredients: Boolean,
    showNutritionProgressBars: Boolean,
    showHighlightedIngredients: Boolean,
    detailsSectionOrder: List<String>,
    hiddenDetailsSections: Set<String>,
    isProductInComparison: Boolean,
    onToggleComparison: () -> Unit,
    onBackClicked: () -> Unit,
    onRetry: () -> Unit,
    onImageClicked: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analiza produktu") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    val context = LocalContext.current
                    if (uiState is ProductUiState.Success) {
                        val product = uiState.product
                        
                        IconButton(onClick = onToggleComparison) {
                            Icon(
                                imageVector = if (isProductInComparison) Icons.Default.CheckCircle else Icons.Default.AddChart,
                                contentDescription = if (isProductInComparison) "Usuń z porównania" else "Dodaj do porównania",
                                tint = if (isProductInComparison) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        val shareText = product.share?.shareText
                        if (shareText != null) {
                            IconButton(onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Udostępnij")
                            }
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
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ProductUiState.Idle -> {}
                is ProductUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is ProductUiState.Success -> {
                    ProductDetailsContent(
                        product = uiState.product,
                        showGroupedIngredients = showGroupedIngredients,
                        showNutritionProgressBars = showNutritionProgressBars,
                        showHighlightedIngredients = showHighlightedIngredients,
                        detailsSectionOrder = detailsSectionOrder,
                        hiddenDetailsSections = hiddenDetailsSections,
                        onImageClicked = onImageClicked
                    )
                }
                is ProductUiState.Error -> {
                    ErrorContent(uiState.message, onRetry)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailsContent(
    product: ProductResponse,
    showGroupedIngredients: Boolean,
    showNutritionProgressBars: Boolean,
    showHighlightedIngredients: Boolean,
    detailsSectionOrder: List<String>,
    hiddenDetailsSections: Set<String>,
    onImageClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Unverified Alert Banner (Always first if exists)
        product.unverifiedCell?.let { unverified ->
            val isDarkTheme = isSystemInDarkTheme()
            val parsedColor = try {
                Color(android.graphics.Color.parseColor(unverified.color ?: "#FCF2E3"))
            } catch (e: Exception) {
                null
            }
            val bgColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                parsedColor ?: MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = if (isDarkTheme) MaterialTheme.colorScheme.onErrorContainer else Color.Black.copy(alpha = 0.8f)
            val descColor = if (isDarkTheme) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = unverified.title ?: "Produkt dodany automatycznie",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = textColor
                        )
                        unverified.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = descColor,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Product Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            product.image?.url?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Zdjęcie produktu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onImageClicked(url) }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name ?: "Nieznany produkt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    product.category?.name?.let { catName ->
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Compact Score Box (Shown if SCORE_CARD is hidden)
                if ("score_card" in hiddenDetailsSections) {
                    val scoreColor = try {
                        Color(android.graphics.Color.parseColor(product.score?.color ?: "#CCCCCC"))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(44.dp)
                            .background(scoreColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${product.score?.value ?: "?"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Sections
        detailsSectionOrder.forEach { sectionId ->
            if (sectionId in hiddenDetailsSections) return@forEach

            when (sectionId) {
                "score_card" -> {
                    // Health Score Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Wynik Zdrowotny",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = product.score?.label ?: "Brak danych",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            val scoreColor = try {
                                Color(android.graphics.Color.parseColor(product.score?.color ?: "#CCCCCC"))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(scoreColor, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${product.score?.value ?: "?"}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                "product_tags" -> {
                    // Tags
                    product.tags?.let { tags ->
                        if (tags.isNotEmpty()) {
                            val isDarkTheme = isSystemInDarkTheme()
                            val tagGreenBg = if (isDarkTheme) Color(0xFF2E5E3E) else Color(0xFFD9F6E4)
                            val tagRedBg = if (isDarkTheme) Color(0xFF5A0000) else Color(0xFFFF8A8A)
                            val tagGreenText = if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.8f)
                            val tagRedText = if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.8f)
                            val tagNeutralBg = MaterialTheme.colorScheme.surfaceVariant
                            val tagNeutralText = MaterialTheme.colorScheme.onSurfaceVariant
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tags.forEach { tag ->
                                    val (tagName, chipBg, chipText) = when (tag) {
                                        "no_sugar_added" -> Triple("Brak dodanego cukru", tagGreenBg, tagGreenText)
                                        "high_fiber" -> Triple("Wysoka zawartość błonnika", tagGreenBg, tagGreenText)
                                        "high_protein" -> Triple("Wysoka zawartość białka", tagGreenBg, tagGreenText)
                                        "not_recommended_for_overweight_people" -> Triple("Szkodliwe dla osób z nadwagą", tagRedBg, tagRedText)
                                        "not_recommended_for_people_with_high_cholesterol" -> Triple("Szkodliwe dla osób z wysokim cholesterolem", tagRedBg, tagRedText)
                                        "not_recommended_for_people_with_diabetes" -> Triple("Szkodliwe dla cukrzyków", tagRedBg, tagRedText)
                                        "not_recommended_for_people_with_hypertension" -> Triple("Szkodliwe dla osób z nadciśnieniem", tagRedBg, tagRedText)
                                        else -> Triple(tag, tagNeutralBg, tagNeutralText)
                                    }

                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tagName, color = chipText) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = chipBg,
                                            labelColor = chipText
                                        ),
                                        border = null,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                "nutrition_table" -> {
                    // Macronutrients
                    product.nutrientValues?.nutrients?.let { nutrients ->
                        if (nutrients.isNotEmpty()) {
                            Text(
                                text = "Wartości odżywcze na 100${product.nutrientValues.per100Unit ?: "g"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    nutrients.forEach { nut ->
                                        val nutName = nut.name ?: "Nieznane"
                                        val valStr = nut.details?.value ?: "0"
                                        val numVal = valStr.replace(Regex("[^0-9.,]"), "").replace(",", ".").toDoubleOrNull() ?: 0.0
                                        
                                        // Calculate RWS percentage and color
                                        val (rwsPercent, barColor) = calculateRws(nutName, numVal)

                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = nutName,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "${nut.details?.value ?: "-"} ${nut.details?.unit ?: ""}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            if (showNutritionProgressBars) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    LinearProgressIndicator(
                                                        progress = rwsPercent,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = barColor,
                                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${(rwsPercent * 100).toInt()}%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                        modifier = Modifier.width(32.dp),
                                                        textAlign = TextAlign.End
                                                    )
                                                }
                                            }
                                        }
                                        if (nut != nutrients.last()) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 4.dp), 
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                "ingredients_list" -> {
                    // Ingredients List
                    Text(
                        text = "Lista Składników",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (product.description.isNullOrBlank()) {
                                Text(
                                    text = "Brak szczegółowego opisu składu.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            } else {
                                if (showHighlightedIngredients && !product.ingredientPhrases.isNullOrEmpty()) {
                                    val isDarkTheme = isSystemInDarkTheme()
                                    
                                    val annotatedText = buildAnnotatedString {
                                        val desc = product.description
                                        append(desc)
                                        
                                        product.ingredientPhrases.forEach { phraseInfo ->
                                            val phrase = phraseInfo.phrase ?: return@forEach
                                            val colorHex = phraseInfo.backgroundColor ?: ""
                                            
                                            val (bgColor, textColor) = translateIngredientColor(colorHex, isDarkTheme)
                                            
                                            var startIndex = desc.indexOf(phrase, ignoreCase = true)
                                            while (startIndex >= 0) {
                                                val endIndex = startIndex + phrase.length
                                                addStyle(
                                                    style = SpanStyle(background = bgColor, color = textColor),
                                                    start = startIndex,
                                                    end = endIndex
                                                )
                                                startIndex = desc.indexOf(phrase, startIndex + phrase.length, ignoreCase = true)
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = annotatedText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = product.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                "harmful_ingredients" -> {
                    // All ingredients with harmful level
                    product.ingredients?.let { ingredients ->
                        if (ingredients.isNotEmpty()) {
                            Text(
                                text = "Szkodliwość składników",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (showGroupedIngredients) {
                                val grouped = ingredients.groupBy { it.harmfulLevel ?: 0 }.toSortedMap(reverseOrder())
                                grouped.forEach { (level, list) ->
                                    val groupName = when(level) {
                                        5 -> "Bardzo szkodliwe"
                                        4 -> "Szkodliwe"
                                        3 -> "Podejrzane"
                                        2 -> "Neutralne"
                                        1 -> "Korzystne"
                                        else -> "Brak danych"
                                    }
                                    
                                    val isDarkTheme = isSystemInDarkTheme()
                                    val (headerBg, headerTextColor) = when (level) {
                                        1 -> if (isDarkTheme) ingredient_dark_green to ingredient_dark_onGreen else ingredient_light_green to ingredient_light_onGreen
                                        2 -> if (isDarkTheme) ingredient_dark_yellow to ingredient_dark_onYellow else ingredient_light_yellow to ingredient_light_onYellow
                                        3 -> if (isDarkTheme) ingredient_dark_orange to ingredient_dark_onOrange else ingredient_light_orange to ingredient_light_onOrange
                                        4, 5 -> if (isDarkTheme) ingredient_dark_red to ingredient_dark_onRed else ingredient_light_red to ingredient_light_onRed
                                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(headerBg)
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = groupName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = headerTextColor
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(Color.Black.copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = list.size.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = headerTextColor
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                list.forEach { ing ->
                                                    Text(
                                                        text = "• ${ing.displayName ?: ing.name ?: "Nieznany"}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                ingredients.forEach { ing ->
                                    val level = ing.harmfulLevel ?: 0
                                    val isDarkTheme = isSystemInDarkTheme()
                                    val (badgeColor, badgeTextColor) = when (level) {
                                        1 -> if (isDarkTheme) ingredient_dark_green to ingredient_dark_onGreen else ingredient_light_green to ingredient_light_onGreen
                                        2 -> if (isDarkTheme) ingredient_dark_yellow to ingredient_dark_onYellow else ingredient_light_yellow to ingredient_light_onYellow
                                        3 -> if (isDarkTheme) ingredient_dark_orange to ingredient_dark_onOrange else ingredient_light_orange to ingredient_light_onOrange
                                        4, 5 -> if (isDarkTheme) ingredient_dark_red to ingredient_dark_onRed else ingredient_light_red to ingredient_light_onRed
                                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ing.displayName ?: ing.name ?: "Nieznany",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(badgeColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = level.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeTextColor
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Spróbuj ponownie")
        }
    }
}

fun translateIngredientColor(apiHex: String, isDark: Boolean): Pair<Color, Color> {
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
            // For unknown colors, we use a fallback alpha and standard text color
            if (isDark) {
                bgColor.copy(alpha = 0.3f) to Color.White
            } else {
                bgColor.copy(alpha = 0.9f) to Color.Black
            }
        }
    }
}

@Composable
fun calculateRws(nutrientName: String, value: Double): Pair<Float, Color> {
    // Reference values for an average adult (2000 kcal)
    val rwsMap = mapOf(
        "energetyczna" to 2000.0,
        "energia" to 2000.0,
        "kalorie" to 2000.0,
        "tłuszcz" to 70.0,
        "nasycone" to 20.0,
        "węglowodany" to 260.0,
        "cukry" to 90.0,
        "białko" to 50.0,
        "sól" to 6.0,
        "błonnik" to 25.0
    )

    val lowerName = nutrientName.lowercase()
    val rwsTarget = rwsMap.entries.firstOrNull { lowerName.contains(it.key) }?.value ?: return Pair(0f, MaterialTheme.colorScheme.primary)

    val percentage = (value / rwsTarget).coerceIn(0.0, 1.0).toFloat()
    
    val isGoodNutrient = lowerName.contains("białko") || lowerName.contains("błonnik")
    val isNeutral = lowerName.contains("energia") || lowerName.contains("kalorie") || lowerName.contains("węglowodany")
    
    val color = if (isGoodNutrient) {
        Color(0xFF4CAF50) // Green
    } else if (isNeutral) {
        MaterialTheme.colorScheme.primary
    } else {
        // Bad nutrients: fat, sugar, salt, saturated fat
        when {
            percentage < 0.33f -> Color(0xFF4CAF50) // Green
            percentage < 0.66f -> Color(0xFFFFB300) // Yellow/Orange
            else -> Color(0xFFE53935) // Red
        }
    }
    
    return Pair(percentage, color)
}
