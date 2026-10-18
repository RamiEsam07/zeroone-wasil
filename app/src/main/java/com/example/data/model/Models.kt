package com.example.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val storeId: String? = null
)

data class Store(
    val id: String,
    val ownerId: String,
    val name: String,
    val nameAr: String,
    val description: String,
    val category: String,
    val phone: String,
    val address: String,
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val status: StoreStatus = StoreStatus.OPEN,
    val openingHours: String = "08:00 AM - 11:00 PM",
    val minimumOrder: Double = 15.0,
    val deliveryFee: Double = 3.5,
    val currency: String = "USD",
    val rating: Double = 4.8,
    val totalOrdersCount: Int = 1420,
    val autoAcceptOrders: Boolean = false,
    val soundAlertsEnabled: Boolean = true
)

data class Category(
    val id: String,
    val storeId: String,
    val name: String,
    val nameAr: String,
    val sortOrder: Int = 0
)

data class Product(
    val id: String,
    val storeId: String,
    val categoryId: String,
    val categoryName: String = "",
    val name: String,
    val nameAr: String = "",
    val description: String,
    val price: Double,
    val image: String = "",
    val isAvailable: Boolean = true,
    val preparationTimeMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val stockQuantity: Int = 0
)

data class OrderItem(
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val productNameAr: String = "",
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double = unitPrice * quantity,
    val specialInstructions: String = "",
    val optionsSummary: String = ""
)

data class Order(
    val id: String,
    val orderNumber: String,
    val storeId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val status: OrderStatus,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double = 0.0,
    val total: Double,
    val items: List<OrderItem> = emptyList(),
    val notes: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedReadyTimeMinutes: Int = 20
) {
    val totalItemCount: Int
        get() = items.sumOf { it.quantity }
}
