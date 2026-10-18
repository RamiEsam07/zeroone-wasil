package com.example.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val storeId: String?
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val nameAr: String,
    val description: String,
    val category: String,
    val phone: String,
    val address: String,
    val logoUrl: String,
    val bannerUrl: String,
    val status: String,
    val openingHours: String,
    val minimumOrder: Double,
    val deliveryFee: Double,
    val currency: String,
    val rating: Double,
    val totalOrdersCount: Int,
    val autoAcceptOrders: Boolean,
    val soundAlertsEnabled: Boolean
)

@Entity(
    tableName = "categories",
    indices = [Index("storeId")]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val nameAr: String,
    val sortOrder: Int
)

@Entity(
    tableName = "products",
    indices = [Index("storeId"), Index("categoryId")]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val categoryId: String,
    val name: String,
    val nameAr: String,
    val description: String,
    val price: Double,
    val image: String,
    val isAvailable: Boolean,
    val preparationTimeMinutes: Int,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val stockQuantity: Int = 0
)

@Entity(
    tableName = "orders",
    indices = [Index("storeId"), Index("status")]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val status: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val total: Double,
    val notes: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val driverId: String?,
    val driverName: String?,
    val driverPhone: String?,
    val createdAt: Long,
    val estimatedReadyTimeMinutes: Int
)

@Entity(
    tableName = "order_items",
    indices = [Index("orderId"), Index("productId")]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val productNameAr: String,
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val specialInstructions: String,
    val optionsSummary: String
)
