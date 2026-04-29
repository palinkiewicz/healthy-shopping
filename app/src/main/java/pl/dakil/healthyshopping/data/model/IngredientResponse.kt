package pl.dakil.healthyshopping.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IngredientResponse(
    val name: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val influence: String? = null,
    val harmfulLevel: Int? = null,
    val type: Int? = null,
    val url: String? = null
)
