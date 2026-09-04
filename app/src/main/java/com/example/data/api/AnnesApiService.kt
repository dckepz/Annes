package com.example.data.api

import com.example.data.models.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface AnnesApiService {

    @POST("auth/login.php")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Response<LoginResponse>

    @GET("auth/verify.php")
    suspend fun verifyToken(): Response<LoginResponse>

    @GET("analytics/dashboard.php")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("products/list.php")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("active_only") activeOnly: Boolean? = false,
        @Query("preorder_only") preorderOnly: Boolean? = null
    ): Response<ProductListResponse>

    @POST("products/create.php")
    suspend fun createProduct(
        @Body product: CreateProductRequest
    ): Response<GenericApiResponse>

    @PUT("products/update.php")
    suspend fun updateProduct(
        @Query("id") id: Int,
        @Body product: UpdateProductRequest
    ): Response<GenericApiResponse>

    @DELETE("products/delete.php")
    suspend fun deleteProduct(
        @Query("id") id: Int
    ): Response<GenericApiResponse>

    @GET("sales/list.php")
    suspend fun getSales(
        @Query("type") type: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<SalesListResponse>

    @POST("sales/record.php")
    suspend fun recordSale(
        @Body sale: RecordSaleRequest
    ): Response<GenericApiResponse>

    @GET("inventory/list.php")
    suspend fun getInventory(
        @Query("low_stock") lowStock: Boolean? = null
    ): Response<InventoryListResponse>

    @PUT("inventory/update.php")
    suspend fun updateStock(
        @Body request: UpdateStockRequest
    ): Response<GenericApiResponse>

    @GET("settings/get.php")
    suspend fun getSetting(
        @Query("key") key: String
    ): Response<Map<String, Any>>

    @POST("settings/update.php")
    suspend fun updateSetting(
        @Body request: SettingUpdateRequest
    ): Response<GenericApiResponse>

    companion object {
        const val DEFAULT_EMULATOR_BASE_URL = "http://10.0.2.2/annesfashion/api/"
        const val DEFAULT_LOCAL_BASE_URL = "http://localhost/annesfashion/api/"
        const val DEFAULT_PROD_BASE_URL = "https://yourdomain.com/annesfashion/api/"

        fun create(
            baseUrl: String,
            tokenProvider: () -> String?
        ): AnnesApiService {
            val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.header("Accept", "application/json")
                tokenProvider()?.let { token ->
                    if (token.isNotBlank()) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                }
                chain.proceed(requestBuilder.build())
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(AnnesApiService::class.java)
        }
    }
}
