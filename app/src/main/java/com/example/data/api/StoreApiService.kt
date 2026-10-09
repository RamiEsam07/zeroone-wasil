package com.example.data.api

import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreStatus
import com.example.data.model.User

data class CreateProductRequest(
    val name: String,
    val nameAr: String = "",
    val description: String,
    val price: Double,
    val categoryId: String,
    val image: String = "",
    val isAvailable: Boolean = true,
    val preparationTimeMinutes: Int = 15
)

data class UpdateProductRequest(
    val name: String? = null,
    val nameAr: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val categoryId: String? = null,
    val image: String? = null,
    val isAvailable: Boolean? = null,
    val preparationTimeMinutes: Int? = null
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus,
    val rejectionReason: String? = null,
    val estimatedReadyTimeMinutes: Int? = null
)

data class UpdateStoreStatusRequest(
    val status: StoreStatus
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

/**
 * REST API Service Abstraction representing the shared delivery platform backend
 * for merchant store operations.
 */
interface StoreApiService {
    // Orders
    suspend fun getStoreOrders(storeId: String, status: String? = null): ApiResponse<List<Order>>
    suspend fun getOrderById(storeId: String, orderId: String): ApiResponse<Order>
    suspend fun updateOrderStatus(storeId: String, orderId: String, request: UpdateOrderStatusRequest): ApiResponse<Order>

    // Products
    suspend fun getStoreProducts(storeId: String, categoryId: String? = null, search: String? = null): ApiResponse<List<Product>>
    suspend fun createProduct(storeId: String, request: CreateProductRequest): ApiResponse<Product>
    suspend fun updateProduct(storeId: String, productId: String, request: UpdateProductRequest): ApiResponse<Product>
    suspend fun deleteProduct(storeId: String, productId: String): ApiResponse<Boolean>
    suspend fun setProductAvailability(storeId: String, productId: String, isAvailable: Boolean): ApiResponse<Product>

    // Categories
    suspend fun getStoreCategories(storeId: String): ApiResponse<List<Category>>
    suspend fun createCategory(storeId: String, name: String, nameAr: String): ApiResponse<Category>

    // Store & Auth
    suspend fun getStoreProfile(storeId: String): ApiResponse<Store>
    suspend fun updateStoreStatus(storeId: String, request: UpdateStoreStatusRequest): ApiResponse<Store>
    suspend fun updateStoreProfile(storeId: String, store: Store): ApiResponse<Store>
    suspend fun authenticateUser(email: String): ApiResponse<Pair<User, Store>>
}
