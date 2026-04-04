package pl.dakil.healthyshopping.data.network

import pl.dakil.healthyshopping.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import pl.dakil.healthyshopping.data.model.SearchResponse

interface HealthyShoppingApi {
    @GET("api/2.0/product/{ean}")
    suspend fun getProduct(@Path("ean") ean: String): ProductResponse

    @GET("api/2.0/search/products/results")
    suspend fun searchProducts(@Query("query") query: String): SearchResponse
}
