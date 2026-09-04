package com.example.data.api

import com.example.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class SupabaseDiagnosticResult(
    val isReachable: Boolean,
    val httpStatusCode: Int,
    val message: String,
    val latencyMs: Long = 0,
    val tablesDetected: List<String> = emptyList(),
    val missingTables: List<String> = emptyList(),
    val isAuthValid: Boolean = false,
    val troubleshootingTip: String? = null
)

class SupabaseClient(
    private val baseUrl: String = "https://yteejssnesajnuibfacx.supabase.co",
    private val anonKey: String = "sb_publishable_l-fKjBTzFUvE__qQrQEZRw_V2_2LAmv",
    private val serviceKey: String = ""
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val activeKey: String
        get() = when {
            serviceKey.isNotBlank() -> serviceKey
            anonKey.isNotBlank() -> anonKey
            else -> ""
        }

    private fun cleanBaseUrl(): String {
        val trimmed = baseUrl.trim()
        val withoutTrailing = if (trimmed.endsWith("/")) trimmed.dropLast(1) else trimmed
        return if (withoutTrailing.endsWith("/rest/v1")) withoutTrailing else "$withoutTrailing/rest/v1"
    }

    private fun newRequestBuilder(endpoint: String): Request.Builder {
        val url = "${cleanBaseUrl()}/$endpoint"
        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")

        val key = activeKey
        if (key.isNotBlank()) {
            builder.header("apikey", key)
            builder.header("Authorization", "Bearer $key")
        }
        return builder
    }

    /**
     * Run full diagnostics against Supabase endpoints and tables
     */
    suspend fun diagnoseConnection(): SupabaseDiagnosticResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val key = activeKey
        if (key.isBlank()) {
            return@withContext SupabaseDiagnosticResult(
                isReachable = false,
                httpStatusCode = 401,
                message = "API Key is missing",
                troubleshootingTip = "Enter your Supabase Anon/Public Key in the Sync Center. You can find this in Supabase Dashboard -> Project Settings -> API."
            )
        }

        try {
            // Test 1: Check products table with universal columns
            val request = newRequestBuilder("products?select=id,title,price,category&limit=5")
                .get()
                .build()

            var latency = 0L
            var statusCode = 0
            var responseBody = ""

            client.newCall(request).execute().use { response ->
                latency = System.currentTimeMillis() - startTime
                statusCode = response.code
                responseBody = response.body?.string().orEmpty()
            }

            when (statusCode) {
                200 -> {
                    val detected = mutableListOf<String>("products")
                    // Check other tables without blocking or connection leaks
                    if (checkTableExists("sales")) detected.add("sales")
                    if (checkTableExists("customers")) detected.add("customers")
                    if (checkTableExists("inventory_logs")) detected.add("inventory_logs")
                    if (checkTableExists("chat_conversations")) detected.add("chat_conversations")
                    if (checkTableExists("chat_messages")) detected.add("chat_messages")
                    if (checkTableExists("users")) detected.add("users")
                    if (checkTableExists("orders")) detected.add("orders")
                    if (checkTableExists("preorders")) detected.add("preorders")

                    val missing = mutableListOf<String>()
                    if (!detected.contains("orders")) missing.add("orders")
                    if (!detected.contains("preorders")) missing.add("preorders")

                    SupabaseDiagnosticResult(
                        isReachable = true,
                        httpStatusCode = 200,
                        message = "Connected to Supabase Successfully!",
                        latencyMs = latency,
                        tablesDetected = detected,
                        missingTables = missing,
                        isAuthValid = true,
                        troubleshootingTip = if (missing.isNotEmpty()) {
                            "Connected to Supabase! Note: ${missing.joinToString(", ")} table(s) can be created via the SQL Schema tab if needed for web orders."
                        } else {
                            "Database is connected and ready for two-way synchronization with the website."
                        }
                    )
                }
                401 -> {
                    SupabaseDiagnosticResult(
                        isReachable = true,
                        httpStatusCode = 401,
                        message = "Unauthorized (Invalid API Key)",
                        latencyMs = latency,
                        isAuthValid = false,
                        troubleshootingTip = "The API Key was rejected by Supabase. Please verify your Anon/Public key."
                    )
                }
                404 -> {
                    val isRelationMissing = responseBody.contains("relation", ignoreCase = true) || responseBody.contains("42P01")
                    SupabaseDiagnosticResult(
                        isReachable = true,
                        httpStatusCode = 404,
                        message = if (isRelationMissing) "Table 'products' does not exist in Supabase" else "Endpoint not found",
                        latencyMs = latency,
                        isAuthValid = true,
                        missingTables = listOf("products", "sales", "customers"),
                        troubleshootingTip = "Your Supabase project is active, but the SQL tables have not been created yet. Open the 'View Supabase SQL Schema' section below, copy the SQL script, and run it in your Supabase SQL Editor."
                    )
                }
                else -> {
                    SupabaseDiagnosticResult(
                        isReachable = true,
                        httpStatusCode = statusCode,
                        message = "Supabase responded with code $statusCode: ${responseBody.take(120)}",
                        latencyMs = latency,
                        troubleshootingTip = "Check if Row Level Security (RLS) is blocking anon queries, or verify your project URL."
                    )
                }
            }
        } catch (e: Exception) {
            SupabaseDiagnosticResult(
                isReachable = false,
                httpStatusCode = 0,
                message = "Network connection failed: ${e.localizedMessage ?: e.message}",
                troubleshootingTip = "Ensure your device has an active internet connection and that '${cleanBaseUrl()}' is accessible."
            )
        }
    }

    private fun checkTableExists(tableName: String): Boolean {
        return try {
            val req = newRequestBuilder("$tableName?select=*&limit=1").get().build()
            client.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Test connection to a custom Website URL / Webhook
     */
    suspend fun testWebsitePing(websiteUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (websiteUrl.isBlank()) return@withContext Pair(false, "Website URL is empty")
        try {
            val formatted = if (websiteUrl.startsWith("http://") || websiteUrl.startsWith("https://")) {
                websiteUrl
            } else {
                "https://$websiteUrl"
            }
            val request = Request.Builder().url(formatted).head().build()
            // Quick 3-second timeout for website ping so invalid custom domains never stall cloud sync
            val pingClient = client.newBuilder().callTimeout(3, TimeUnit.SECONDS).build()
            pingClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code in 200..399) {
                    Pair(true, "Website is online (HTTP ${response.code})")
                } else {
                    Pair(false, "Website returned HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Could not reach website: ${e.localizedMessage ?: "Timeout"}")
        }
    }

    // ==========================================
    // PRODUCTS CRUD (Supabase PostgREST)
    // ==========================================

    suspend fun fetchProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("products?select=*&order=id.desc").get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                    val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                    val rawList = adapter.fromJson(body) ?: emptyList()

                    val products = rawList.mapNotNull { map ->
                        parseProductFromMap(map)
                    }
                    Result.success(products)
                } else {
                    Result.failure(Exception("Supabase HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertProduct(product: Product): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mutableMapOf<String, Any?>(
                "title" to product.title,
                "description" to product.description,
                "price" to product.price,
                "category" to product.category,
                "sku" to product.sku,
                "image_url" to product.imageUrl,
                "is_active" to product.isActive,
                "is_featured" to product.isFeatured,
                "allow_preorder" to product.allowPreorder,
                "total_stock" to product.totalStock,
                "sizes" to product.sizes,
                "colors" to product.colors,
                "waist_sizes" to product.waistSizes,
                "bust_sizes" to product.bustSizes,
                "shoe_sizes" to product.shoeSizes
            )
            var json = moshi.adapter(Map::class.java).toJson(map)
            var body = json.toRequestBody("application/json".toMediaType())
            var request = newRequestBuilder("products").post(body).build()
            var response = client.newCall(request).execute()

            // If failed due to extra variant or total_stock columns missing in schema, retry without them
            if (response.code == 400) {
                map.remove("sizes")
                map.remove("colors")
                map.remove("waist_sizes")
                map.remove("bust_sizes")
                map.remove("shoe_sizes")
                map.remove("total_stock")
                json = moshi.adapter(Map::class.java).toJson(map)
                body = json.toRequestBody("application/json".toMediaType())
                request = newRequestBuilder("products").post(body).build()
                response = client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Insert failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: Int, product: Product): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mutableMapOf<String, Any?>(
                "title" to product.title,
                "description" to product.description,
                "price" to product.price,
                "category" to product.category,
                "is_featured" to product.isFeatured,
                "allow_preorder" to product.allowPreorder,
                "total_stock" to product.totalStock,
                "image_url" to product.imageUrl,
                "sizes" to product.sizes,
                "colors" to product.colors,
                "waist_sizes" to product.waistSizes,
                "bust_sizes" to product.bustSizes,
                "shoe_sizes" to product.shoeSizes
            )
            var json = moshi.adapter(Map::class.java).toJson(map)
            var body = json.toRequestBody("application/json".toMediaType())
            var request = newRequestBuilder("products?id=eq.$id").patch(body).build()
            var response = client.newCall(request).execute()

            // Retry without extra columns if not in schema
            if (response.code == 400) {
                map.remove("sizes")
                map.remove("colors")
                map.remove("waist_sizes")
                map.remove("bust_sizes")
                map.remove("shoe_sizes")
                map.remove("total_stock")
                json = moshi.adapter(Map::class.java).toJson(map)
                body = json.toRequestBody("application/json".toMediaType())
                request = newRequestBuilder("products?id=eq.$id").patch(body).build()
                response = client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Update failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("products?id=eq.$id").delete().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Delete failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // SALES / ORDERS CRUD
    // ==========================================

    suspend fun fetchSales(): Result<List<Sale>> = withContext(Dispatchers.IO) {
        try {
            // Try fetching from 'sales' first, then 'orders'
            var body = ""
            var isSuccess = false
            var code = 0

            val request = newRequestBuilder("sales?select=*&order=id.desc").get().build()
            client.newCall(request).execute().use { resp ->
                code = resp.code
                isSuccess = resp.isSuccessful
                body = resp.body?.string().orEmpty()
            }

            if (!isSuccess) {
                val reqOrders = newRequestBuilder("orders?select=*&order=id.desc").get().build()
                client.newCall(reqOrders).execute().use { respOrders ->
                    code = respOrders.code
                    isSuccess = respOrders.isSuccessful
                    body = respOrders.body?.string().orEmpty()
                }
            }

            if (isSuccess) {
                val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                val rawList = adapter.fromJson(body) ?: emptyList()

                val sales = rawList.mapNotNull { map ->
                    parseSaleFromMap(map)
                }
                Result.success(sales)
            } else {
                Result.failure(Exception("Supabase HTTP $code: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertSale(sale: Sale): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "sale_number" to sale.saleNumber,
                "sale_type" to sale.saleType,
                "customer_name" to sale.customerName,
                "customer_phone" to sale.customerPhone,
                "total_amount" to sale.totalAmount,
                "payment_method" to sale.paymentMethod,
                "payment_status" to sale.paymentStatus,
                "transaction_reference" to sale.transactionReference,
                "notes" to sale.notes
            )
            val json = moshi.adapter(Map::class.java).toJson(map)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("sales").post(body).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                // Try orders table if sales table failed
                val ordersRequest = newRequestBuilder("orders").post(body).build()
                val ordersResponse = client.newCall(ordersRequest).execute()
                Result.success(ordersResponse.isSuccessful)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CUSTOMERS CRUD
    // ==========================================

    suspend fun fetchCustomers(): Result<List<Customer>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("customers?select=*&order=id.desc").get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                val rawList = adapter.fromJson(body) ?: emptyList()

                val customers = rawList.mapNotNull { map ->
                    parseCustomerFromMap(map)
                }
                Result.success(customers)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertCustomer(customer: Customer): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "name" to customer.name,
                "email" to customer.email,
                "phone" to customer.phone,
                "address" to customer.address,
                "city" to customer.city,
                "customer_type" to customer.customerType,
                "total_orders" to customer.totalOrders,
                "total_spent" to customer.totalSpent
            )
            val json = moshi.adapter(Map::class.java).toJson(map)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("customers").post(body).build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // PRE-ORDERS CRUD
    // ==========================================

    suspend fun fetchPreorders(): Result<List<Preorder>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("preorders?select=*&order=id.desc").get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                val rawList = adapter.fromJson(body) ?: emptyList()

                val preorders = rawList.mapNotNull { map ->
                    parsePreorderFromMap(map)
                }
                Result.success(preorders)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertPreorder(preorder: Preorder): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "preorder_number" to preorder.preorderNumber,
                "product_id" to preorder.productId,
                "product_title" to preorder.productTitle,
                "customer_name" to preorder.customerName,
                "customer_phone" to preorder.customerPhone,
                "quantity" to preorder.quantity,
                "deposit_amount" to preorder.depositAmount,
                "total_amount" to preorder.totalAmount,
                "status" to preorder.status,
                "expected_date" to preorder.expectedDate,
                "notes" to preorder.notes
            )
            val json = moshi.adapter(Map::class.java).toJson(map)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("preorders").post(body).build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreorderStatus(id: Long, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mapOf("status" to newStatus)
            val json = moshi.adapter(Map::class.java).toJson(map)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("preorders?id=eq.$id").patch(body).build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // WEBSITE CHAT & SUPPORT CONVERSATIONS
    // ==========================================

    suspend fun fetchConversations(): Result<List<Conversation>> = withContext(Dispatchers.IO) {
        try {
            // Try chat_conversations first, fallback to conversations
            var request = newRequestBuilder("chat_conversations?select=*&order=id.desc").get().build()
            var response = client.newCall(request).execute()
            var body = response.body?.string().orEmpty()

            if (!response.isSuccessful && response.code == 404) {
                request = newRequestBuilder("conversations?select=*&order=id.desc").get().build()
                response = client.newCall(request).execute()
                body = response.body?.string().orEmpty()
            }

            if (response.isSuccessful) {
                val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                val rawList = adapter.fromJson(body) ?: emptyList()

                val conversations = rawList.mapNotNull { map ->
                    parseConversationFromMap(map)
                }
                Result.success(conversations)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMessages(conversationId: Long): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("chat_messages?conversation_id=eq.$conversationId&order=id.asc").get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                val rawList = adapter.fromJson(body) ?: emptyList()

                val messages = rawList.mapNotNull { map ->
                    parseChatMessageFromMap(map)
                }
                Result.success(messages)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(message: ChatMessage): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "conversation_id" to message.conversationId,
                "sender_type" to message.sender,
                "sender_name" to message.senderName,
                "content" to message.messageText
            )
            val json = moshi.adapter(Map::class.java).toJson(map)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = newRequestBuilder("chat_messages").post(body).build()
            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // PARSING HELPERS
    // ==========================================

    private fun parseProductFromMap(map: Map<String, Any?>): Product? {
        return try {
            val id = (map["id"] as? Number)?.toInt() ?: return null
            val title = (map["title"] as? String) ?: (map["name"] as? String) ?: "Product #$id"
            val desc = (map["description"] as? String).orEmpty()
            val price = (map["price"] as? Number)?.toDouble() ?: 0.0
            val cat = (map["category"] as? String) ?: "dresses"
            val sku = (map["sku"] as? String) ?: "LK-${cat.take(3).uppercase()}-$id"
            val img = (map["image_url"] as? String) ?: (map["image"] as? String)
            val active = (map["is_active"] as? Number)?.toInt() ?: if (map["is_active"] == true) 1 else 1
            val feat = (map["is_featured"] as? Number)?.toInt() ?: if (map["is_featured"] == true) 1 else 0
            val preorder = (map["allow_preorder"] as? Number)?.toInt() ?: if (map["allow_preorder"] == true) 1 else 0
            val stock = (map["total_stock"] as? Number)?.toInt() ?: (map["stock"] as? Number)?.toInt() ?: 10
            val created = (map["created_at"] as? String)

            val parseList: (Any?) -> List<String> = { raw ->
                when (raw) {
                    is List<*> -> raw.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
                    is String -> if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    else -> emptyList()
                }
            }

            val sizes = parseList(map["sizes"])
            val colors = parseList(map["colors"])
            val waistSizes = parseList(map["waist_sizes"])
            val bustSizes = parseList(map["bust_sizes"])
            val shoeSizes = parseList(map["shoe_sizes"])

            Product(
                id = id,
                title = title,
                description = desc,
                price = price,
                category = cat,
                sku = sku,
                imageUrl = img,
                isActive = active,
                isFeatured = feat,
                allowPreorder = preorder,
                totalStock = stock,
                createdAt = created,
                sizes = sizes,
                colors = colors,
                waistSizes = waistSizes,
                bustSizes = bustSizes,
                shoeSizes = shoeSizes
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSaleFromMap(map: Map<String, Any?>): Sale? {
        return try {
            val id = (map["id"] as? Number)?.toInt() ?: return null
            val saleNo = (map["sale_number"] as? String) ?: (map["order_number"] as? String) ?: "AF-$id"
            val saleType = (map["sale_type"] as? String) ?: (map["type"] as? String) ?: "online"
            val custName = (map["customer_name"] as? String) ?: (map["customer"] as? String)
            val custPhone = (map["customer_phone"] as? String) ?: (map["phone"] as? String)
            val amount = (map["total_amount"] as? Number)?.toDouble() ?: (map["amount"] as? Number)?.toDouble() ?: 0.0
            val payMethod = (map["payment_method"] as? String) ?: "mpesa"
            val payStatus = (map["payment_status"] as? String) ?: (map["status"] as? String) ?: "paid"
            val ref = (map["transaction_reference"] as? String) ?: (map["reference"] as? String)
            val notes = (map["notes"] as? String)
            val created = (map["created_at"] as? String)

            Sale(
                id = id,
                saleNumber = saleNo,
                saleType = saleType,
                customerName = custName,
                customerPhone = custPhone,
                totalAmount = amount,
                paymentMethod = payMethod,
                paymentStatus = payStatus,
                transactionReference = ref,
                notes = notes,
                createdAt = created
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseCustomerFromMap(map: Map<String, Any?>): Customer? {
        return try {
            val id = (map["id"] as? Number)?.toLong() ?: 1L
            val name = (map["name"] as? String) ?: "Valued Customer"
            val email = (map["email"] as? String)
            val phone = (map["phone"] as? String)
            val address = (map["address"] as? String)
            val city = (map["city"] as? String) ?: "Nairobi"
            val type = (map["customer_type"] as? String) ?: "walk-in"
            val orders = (map["total_orders"] as? Number)?.toInt() ?: 0
            val spent = (map["total_spent"] as? Number)?.toDouble() ?: 0.0
            val created = (map["created_at"] as? String)

            Customer(
                id = id,
                name = name,
                email = email,
                phone = phone,
                address = address,
                city = city,
                customerType = type,
                totalOrders = orders,
                totalSpent = spent,
                createdAt = created
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parsePreorderFromMap(map: Map<String, Any?>): Preorder? {
        return try {
            val id = (map["id"] as? Number)?.toLong() ?: 1L
            val preorderNo = (map["preorder_number"] as? String) ?: "PO-$id"
            val prodId = (map["product_id"] as? Number)?.toInt() ?: 0
            val title = (map["product_title"] as? String) ?: "Custom Piece"
            val custName = (map["customer_name"] as? String) ?: "Client"
            val custPhone = (map["customer_phone"] as? String) ?: ""
            val qty = (map["quantity"] as? Number)?.toInt() ?: 1
            val deposit = (map["deposit_amount"] as? Number)?.toDouble() ?: 0.0
            val total = (map["total_amount"] as? Number)?.toDouble() ?: 0.0
            val status = (map["status"] as? String) ?: "pending"
            val expDate = (map["expected_date"] as? String)
            val notes = (map["notes"] as? String)
            val created = (map["created_at"] as? String)

            Preorder(
                id = id,
                preorderNumber = preorderNo,
                productId = prodId,
                productTitle = title,
                customerName = custName,
                customerPhone = custPhone,
                quantity = qty,
                depositAmount = deposit,
                totalAmount = total,
                status = status,
                expectedDate = expDate,
                notes = notes,
                createdAt = created
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseConversationFromMap(map: Map<String, Any?>): Conversation? {
        return try {
            val id = (map["id"] as? Number)?.toLong() ?: 1L
            val name = (map["customer_name"] as? String) ?: "Customer"
            val phone = (map["customer_phone"] as? String)
            val email = (map["customer_email"] as? String)
            val status = (map["status"] as? String) ?: "active"
            val lastMsg = (map["last_message"] as? String) ?: ""
            val unread = (map["unread_count"] as? Number)?.toInt() ?: 0
            val created = (map["created_at"] as? String)
            val updated = (map["updated_at"] as? String)

            Conversation(
                id = id,
                customerName = name,
                customerPhone = phone,
                customerEmail = email,
                status = status,
                lastMessage = lastMsg,
                unreadCount = unread,
                createdAt = created,
                updatedAt = updated
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseChatMessageFromMap(map: Map<String, Any?>): ChatMessage? {
        return try {
            val id = (map["id"] as? Number)?.toLong() ?: 1L
            val convId = (map["conversation_id"] as? Number)?.toLong() ?: 1L
            val sender = (map["sender_type"] as? String) ?: (map["sender"] as? String) ?: "customer"
            val senderName = (map["sender_name"] as? String) ?: ""
            val msgText = (map["content"] as? String) ?: (map["message_text"] as? String) ?: (map["message"] as? String) ?: ""
            val created = (map["created_at"] as? String)

            ChatMessage(
                id = id,
                conversationId = convId,
                sender = sender,
                senderName = senderName,
                messageText = msgText,
                createdAt = created
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun generateSupabaseSetupSql(): String {
            return """
-- =========================================================
-- ANNE'S FASHION LINE - COMPLETE DATABASE SCHEMA & RLS FIX
-- Run this in your Supabase SQL Editor:
-- https://supabase.com/dashboard/project/yteejssnesajnuibfacx/sql
-- =========================================================

-- 1. PRODUCTS TABLE
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    category TEXT NOT NULL DEFAULT 'dresses',
    sku TEXT UNIQUE,
    image_url TEXT,
    is_active INTEGER DEFAULT 1,
    is_featured INTEGER DEFAULT 0,
    allow_preorder INTEGER DEFAULT 0,
    total_stock INTEGER DEFAULT 10,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. SALES TABLE
CREATE TABLE IF NOT EXISTS public.sales (
    id BIGSERIAL PRIMARY KEY,
    sale_number TEXT UNIQUE NOT NULL,
    sale_type TEXT DEFAULT 'in-store', -- 'in-store' or 'online'
    customer_name TEXT,
    customer_phone TEXT,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    payment_method TEXT DEFAULT 'mpesa', -- 'mpesa', 'cash', 'card', 'bank-transfer'
    payment_status TEXT DEFAULT 'paid', -- 'paid', 'pending', 'refunded'
    transaction_reference TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. ORDERS TABLE (Website & Online Orders)
CREATE TABLE IF NOT EXISTS public.orders (
    id BIGSERIAL PRIMARY KEY,
    order_number TEXT UNIQUE NOT NULL,
    sale_number TEXT,
    customer_name TEXT,
    customer_phone TEXT,
    customer_email TEXT,
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    status TEXT DEFAULT 'pending',
    payment_status TEXT DEFAULT 'paid',
    payment_method TEXT DEFAULT 'mpesa',
    transaction_reference TEXT,
    items JSONB DEFAULT '[]'::jsonb,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. PREORDERS TABLE (Boutique Pre-orders)
CREATE TABLE IF NOT EXISTS public.preorders (
    id BIGSERIAL PRIMARY KEY,
    preorder_number TEXT UNIQUE NOT NULL,
    product_id BIGINT,
    product_title TEXT,
    customer_name TEXT,
    customer_phone TEXT,
    quantity INTEGER DEFAULT 1,
    deposit_amount NUMERIC(10,2) DEFAULT 0.00,
    total_amount NUMERIC(10,2) DEFAULT 0.00,
    status TEXT DEFAULT 'pending', -- 'pending', 'ready', 'fulfilled', 'cancelled'
    expected_date TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. CUSTOMERS TABLE
CREATE TABLE IF NOT EXISTS public.customers (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    address TEXT,
    city TEXT DEFAULT 'Nairobi',
    customer_type TEXT DEFAULT 'walk-in', -- 'walk-in', 'VIP', 'online'
    total_orders INTEGER DEFAULT 0,
    total_spent NUMERIC(10,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. INVENTORY LOGS TABLE
CREATE TABLE IF NOT EXISTS public.inventory_logs (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    product_title TEXT,
    quantity_change INTEGER NOT NULL,
    action_type TEXT NOT NULL, -- 'restock', 'adjustment', 'return', 'sale'
    user_name TEXT DEFAULT 'Admin',
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. ENABLE ROW LEVEL SECURITY (RLS)
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.preorders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventory_logs ENABLE ROW LEVEL SECURITY;

-- Drop existing policies to prevent conflict errors
DROP POLICY IF EXISTS "Allow anon read products" ON public.products;
DROP POLICY IF EXISTS "Allow anon insert products" ON public.products;
DROP POLICY IF EXISTS "Allow anon update products" ON public.products;
DROP POLICY IF EXISTS "Allow anon delete products" ON public.products;

DROP POLICY IF EXISTS "Allow anon read sales" ON public.sales;
DROP POLICY IF EXISTS "Allow anon insert sales" ON public.sales;

DROP POLICY IF EXISTS "Allow anon read orders" ON public.orders;
DROP POLICY IF EXISTS "Allow anon insert orders" ON public.orders;
DROP POLICY IF EXISTS "Allow anon update orders" ON public.orders;

DROP POLICY IF EXISTS "Allow anon read preorders" ON public.preorders;
DROP POLICY IF EXISTS "Allow anon insert preorders" ON public.preorders;
DROP POLICY IF EXISTS "Allow anon update preorders" ON public.preorders;

DROP POLICY IF EXISTS "Allow anon read customers" ON public.customers;
DROP POLICY IF EXISTS "Allow anon insert customers" ON public.customers;

DROP POLICY IF EXISTS "Allow anon read inventory" ON public.inventory_logs;
DROP POLICY IF EXISTS "Allow anon insert inventory" ON public.inventory_logs;

-- Re-create permissive policies for Mobile App & Website integration
CREATE POLICY "Allow anon read products" ON public.products FOR SELECT USING (true);
CREATE POLICY "Allow anon insert products" ON public.products FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow anon update products" ON public.products FOR UPDATE USING (true);
CREATE POLICY "Allow anon delete products" ON public.products FOR DELETE USING (true);

CREATE POLICY "Allow anon read sales" ON public.sales FOR SELECT USING (true);
CREATE POLICY "Allow anon insert sales" ON public.sales FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow anon read orders" ON public.orders FOR SELECT USING (true);
CREATE POLICY "Allow anon insert orders" ON public.orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow anon update orders" ON public.orders FOR UPDATE USING (true);

CREATE POLICY "Allow anon read preorders" ON public.preorders FOR SELECT USING (true);
CREATE POLICY "Allow anon insert preorders" ON public.preorders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow anon update preorders" ON public.preorders FOR UPDATE USING (true);

CREATE POLICY "Allow anon read customers" ON public.customers FOR SELECT USING (true);
CREATE POLICY "Allow anon insert customers" ON public.customers FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow anon read inventory" ON public.inventory_logs FOR SELECT USING (true);
CREATE POLICY "Allow anon insert inventory" ON public.inventory_logs FOR INSERT WITH CHECK (true);

-- Grant privileges to anon and authenticated roles to solve 42501 permission denied errors
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated, service_role;

-- Enable Realtime publication for synced entities
ALTER PUBLICATION supabase_realtime ADD TABLE public.products, public.sales, public.preorders, public.customers;
            """.trimIndent()
        }
    }
}
