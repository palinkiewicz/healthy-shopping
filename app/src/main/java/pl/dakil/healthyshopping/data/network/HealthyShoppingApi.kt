package pl.dakil.healthyshopping.data.network

import pl.dakil.healthyshopping.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HealthyShoppingApi {
    @GET("api/2.0/product/{ean}")
    suspend fun getProduct(@Path("ean") ean: String): ProductResponse
}
