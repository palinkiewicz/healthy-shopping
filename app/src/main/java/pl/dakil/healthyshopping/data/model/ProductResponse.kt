package pl.dakil.healthyshopping.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val ean: String? = null,
    val name: String? = null,
    val description: String? = null,
    val harmfulLevel: Int? = null,
    val lastVerificationDate: String? = null,
    val nutrientValues: NutrientValues? = null,
    val category: Category? = null,
    val newCategory: Category? = null,
    val categoryConfirmed: Boolean? = null,
    val image: ImageInfo? = null,
    val score: Score? = null,
    val tags: List<String>? = emptyList(),
    val badgeImage: ImageInfo? = null,
    val healthyServing: HealthyServing? = null,
    val share: ShareInfo? = null,
    val unverifiedCell: String? = null,
    val promotedManufacturerCell: String? = null,
    val viewAttributes: ViewAttributes? = null,
    val additionalTags: List<String>? = emptyList(),
    val ingredientPhrases: List<IngredientPhrase>? = emptyList(),
    val shareDeeplink: String? = null,
    val recommendation: Recommendation? = null,
    val ingredients: List<Ingredient>? = emptyList()
)

@Serializable
data class NutrientValues(
    val per100Unit: String? = null,
    val nutrients: List<Nutrient>? = emptyList(),
    val healthyServingText: String? = null
)

@Serializable
data class Nutrient(
    val id: String? = null,
    val name: String? = null,
    val shortName: String? = null,
    val group: Int? = null,
    val details: NutrientDetails? = null
)

@Serializable
data class NutrientDetails(
    val unit: String? = null,
    val value: String? = null,
    val quantifier: String? = null,
    val ranges: Ranges? = null
)

@Serializable
data class Ranges(
    val low: Double? = null,
    val medium: Double? = null,
    val high: Double? = null,
    val neutral: Boolean? = null
)

@Serializable
data class Category(
    val id: Int? = null,
    val name: String? = null,
    val type: Int? = null,
    val reportCategoryType: Int? = null
)

@Serializable
data class ImageInfo(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class Score(
    val value: Int? = null,
    val max: Int? = null,
    val label: String? = null,
    val color: String? = null,
    val faqDeeplink: String? = null
)

@Serializable
data class HealthyServing(
    val text: String? = null,
    val deeplink: String? = null,
    val color: String? = null,
    val image: ImageInfo? = null
)

@Serializable
data class ShareInfo(
    val text: String? = null,
    val shareText: String? = null,
    val color: String? = null,
    val image: ImageInfo? = null
)

@Serializable
data class ViewAttributes(
    val grayName: Boolean? = null
)

@Serializable
data class IngredientPhrase(
    val phrase: String? = null,
    val backgroundColor: String? = null
)

@Serializable
data class Recommendation(
    val topProduct: String? = null
)

@Serializable
data class Ingredient(
    val id: Int? = null,
    val name: String? = null,
    val displayName: String? = null,
    val harmfulLevel: Int? = null,
    val url: String? = null
)
