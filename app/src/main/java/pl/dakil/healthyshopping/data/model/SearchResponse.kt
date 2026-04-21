package pl.dakil.healthyshopping.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val products: List<SearchProduct> = emptyList()
)

@Serializable
data class SearchProduct(
    val ean: String? = null,
    val name: String? = null,
    val score: Score? = null,
    val harmfulLevel: Int? = null,
    val image: ImageInfo? = null,
    val nutrients: NutrientValues? = null
)
