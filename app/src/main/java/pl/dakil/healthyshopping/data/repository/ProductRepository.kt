package pl.dakil.healthyshopping.data.repository

import pl.dakil.healthyshopping.data.model.ProductResponse
import pl.dakil.healthyshopping.data.model.SearchResponse
import pl.dakil.healthyshopping.data.network.HealthyShoppingApi

class ProductRepository(private val api: HealthyShoppingApi) {
    suspend fun getProduct(ean: String): Result<ProductResponse> {
        return try {
            val response = api.getProduct(ean)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<SearchResponse> {
        return try {
            val response = api.searchProducts(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
