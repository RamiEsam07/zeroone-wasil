package com.example.data.repository

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.api.CreateProductRequest
import com.example.data.api.UpdateProductRequest
import com.example.data.local.CategoryEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.ProductEntity
import com.example.data.local.StoreDatabase
import com.example.data.local.StoreEntity
import com.example.data.local.UserEntity
import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class StoreRepository(
    private val context: Context,
    private val database: StoreDatabase = StoreDatabase.getInstance(context)
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Current Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentStoreId = MutableStateFlow("str_001")
    val currentStoreId: StateFlow<String> = _currentStoreId.asStateFlow()

    init {
        coroutineScope.launch {
            // Ensure DB seeded and initial user logged in
            val user = database.userDao().getUserById("usr_owner_01")
            if (user == null) {
                database.populateInitialData()
                val loaded = database.userDao().getUserById("usr_owner_01")
                _currentUser.value = loaded?.toDomain()
                _currentStoreId.value = loaded?.storeId ?: "str_001"
            } else {
                _currentUser.value = user.toDomain()
                _currentStoreId.value = user.storeId ?: "str_001"
            }
        }
    }

    suspend fun getAvailableDemoAccounts(): List<Pair<User, Store>> = withContext(Dispatchers.IO) {
        val users = database.userDao().getAllStoreUsers().map { it.toDomain() }
        val stores = database.storeDao().getAllStores().map { it.toDomain() }.associateBy { it.id }
        users.mapNotNull { user ->
            val store = user.storeId?.let { stores[it] }
            if (store != null) Pair(user, store) else null
        }
    }

    suspend fun loginAsUser(user: User): Boolean = withContext(Dispatchers.IO) {
        _currentUser.value = user
        if (user.storeId != null) {
            _currentStoreId.value = user.storeId
            true
        } else {
            false
        }
    }

    suspend fun loginWithEmail(email: String): Result<User> = withContext(Dispatchers.IO) {
        val entity = database.userDao().getUserByEmail(email.trim())
        if (entity != null) {
            val user = entity.toDomain()
            _currentUser.value = user
            if (user.storeId != null) {
                _currentStoreId.value = user.storeId
            }
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Invalid email or merchant account not found"))
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Reactive store observation scoped strictly to active store
    val currentStore: Flow<Store?> = _currentStoreId.flatMapLatest { storeId ->
        database.storeDao().getStoreByIdFlow(storeId).map { it?.toDomain() }
    }

    val storeCategories: Flow<List<Category>> = _currentStoreId.flatMapLatest { storeId ->
        database.categoryDao().getCategoriesForStore(storeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    val storeProducts: Flow<List<Product>> = _currentStoreId.flatMapLatest { storeId ->
        combine(
            database.productDao().getProductsForStore(storeId),
            database.categoryDao().getCategoriesForStore(storeId)
        ) { products, categories ->
            val catMap = categories.associate { it.id to it.name }
            products.map { p ->
                p.toDomain(categoryName = catMap[p.categoryId] ?: "General")
            }
        }
    }

    // Orders Flow with hydrated order items, scoped strictly to active store
    val storeOrders: Flow<List<Order>> = _currentStoreId.flatMapLatest { storeId ->
        database.orderDao().getOrdersForStore(storeId).flatMapLatest { orderEntities ->
            val orderIds = orderEntities.map { it.id }
            if (orderIds.isEmpty()) {
                flowOf(emptyList())
            } else {
                database.orderItemDao().getItemsForOrdersFlow(orderIds).map { items ->
                    val groupedItems = items.groupBy { it.orderId }
                    orderEntities.map { entity ->
                        val orderItems = groupedItems[entity.id]?.map { it.toDomain() } ?: emptyList()
                        entity.toDomain(orderItems)
                    }
                }
            }
        }
    }

    suspend fun getOrderById(orderId: String): Order? = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val entity = database.orderDao().getOrderById(orderId, storeId) ?: return@withContext null
        val items = database.orderItemDao().getItemsForOrder(orderId).map { it.toDomain() }
        entity.toDomain(items)
    }

    // Status transition enforcement
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val order = database.orderDao().getOrderById(orderId, storeId)
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found or unauthorized for this store"))

        val currentStatus = try {
            OrderStatus.valueOf(order.status)
        } catch (e: Exception) {
            OrderStatus.PENDING
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            return@withContext Result.failure(
                IllegalStateException("Invalid order transition from $currentStatus to $newStatus")
            )
        }

        database.orderDao().updateOrderStatus(orderId, storeId, newStatus.name)
        Result.success(Unit)
    }

    suspend fun updateStoreStatus(status: StoreStatus) = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        database.storeDao().updateStoreStatus(storeId, status.name)
    }

    suspend fun updateStore(store: Store) = withContext(Dispatchers.IO) {
        database.storeDao().updateStore(store.toEntity())
    }

    // Product CRUD operations
    suspend fun createProduct(request: CreateProductRequest): Result<Product> = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val productId = "prd_" + UUID.randomUUID().toString().take(8)
        val entity = ProductEntity(
            id = productId,
            storeId = storeId,
            categoryId = request.categoryId,
            name = request.name.trim(),
            nameAr = request.nameAr.trim(),
            description = request.description.trim(),
            price = request.price,
            image = request.image.trim(),
            isAvailable = request.isAvailable,
            preparationTimeMinutes = request.preparationTimeMinutes,
            createdAt = System.currentTimeMillis()
        )
        database.productDao().insertProduct(entity)
        Result.success(entity.toDomain())
    }

    suspend fun updateProduct(productId: String, request: UpdateProductRequest): Result<Unit> = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val existing = database.productDao().getProductById(productId, storeId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found"))

        val updated = existing.copy(
            name = request.name ?: existing.name,
            nameAr = request.nameAr ?: existing.nameAr,
            description = request.description ?: existing.description,
            price = request.price ?: existing.price,
            categoryId = request.categoryId ?: existing.categoryId,
            image = request.image ?: existing.image,
            isAvailable = request.isAvailable ?: existing.isAvailable,
            preparationTimeMinutes = request.preparationTimeMinutes ?: existing.preparationTimeMinutes
        )
        database.productDao().updateProduct(updated)
        Result.success(Unit)
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        database.productDao().deleteProduct(productId, storeId)
        Result.success(Unit)
    }

    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        database.productDao().updateProductAvailability(productId, storeId, isAvailable)
    }

    // Category CRUD
    suspend fun createCategory(name: String, nameAr: String): Result<Category> = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val id = "cat_" + UUID.randomUUID().toString().take(6)
        val entity = CategoryEntity(
            id = id,
            storeId = storeId,
            name = name.trim(),
            nameAr = nameAr.trim(),
            sortOrder = 99
        )
        database.categoryDao().insertCategory(entity)
        Result.success(entity.toDomain())
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        database.categoryDao().updateCategory(
            CategoryEntity(
                id = category.id,
                storeId = _currentStoreId.value,
                name = category.name,
                nameAr = category.nameAr,
                sortOrder = category.sortOrder
            )
        )
    }

    suspend fun deleteCategory(categoryId: String) = withContext(Dispatchers.IO) {
        database.categoryDao().deleteCategory(categoryId, _currentStoreId.value)
    }

    // Live Incoming Order Simulation
    suspend fun simulateIncomingOrder(): Order = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val randomOrderNumber = "ORD-" + (9103..9999).random()
        val orderId = "ord_" + UUID.randomUUID().toString().take(8)
        val customerNames = listOf(
            "Layla Al-Sayed", "Ahmed Mansoor", "Fatima Al-Hadi", "Yousef Qasim", "Salma Barakat", "Ibrahim Khalil"
        )
        val customerName = customerNames.random()
        val addresses = listOf(
            "124 Palm View Heights, Apt 8",
            "55 Orchid Residence, Al-Rawdah",
            "19 Emerald Coast Villas",
            "89 King Abdulaziz Road, Suite 210"
        )
        val sampleItems = database.productDao().getProductById("prd_01", storeId)
        val sampleItems2 = database.productDao().getProductById("prd_04", storeId)

        val item1 = sampleItems?.let {
            OrderItemEntity(
                id = "item_" + UUID.randomUUID().toString().take(6),
                orderId = orderId,
                productId = it.id,
                productName = it.name,
                productNameAr = it.nameAr,
                unitPrice = it.price,
                quantity = (1..2).random(),
                totalPrice = it.price * 1,
                specialInstructions = "Please pack cutlery and napkins",
                optionsSummary = "Hot & Fresh"
            )
        } ?: OrderItemEntity(
            id = "item_" + UUID.randomUUID().toString().take(6),
            orderId = orderId,
            productId = "custom_prd",
            productName = "Chef Special Grill Platter",
            productNameAr = "طبق مشاوي الشيف الخاص",
            unitPrice = 28.0,
            quantity = 1,
            totalPrice = 28.0,
            specialInstructions = "Extra bread",
            optionsSummary = "Signature combo"
        )

        val subtotal = item1.totalPrice
        val deliveryFee = 3.99
        val tax = (subtotal * 0.08 * 100).toInt() / 100.0
        val total = ((subtotal + deliveryFee + tax) * 100).toInt() / 100.0

        val orderEntity = OrderEntity(
            id = orderId,
            orderNumber = randomOrderNumber,
            storeId = storeId,
            customerId = "cust_" + (300..999).random(),
            customerName = customerName,
            customerPhone = "+1 (555) 789-${(1000..9999).random()}",
            deliveryAddress = addresses.random(),
            status = OrderStatus.PENDING.name,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            tax = tax,
            total = total,
            notes = "Please confirm fast! Hungry customer waiting.",
            paymentMethod = PaymentMethod.CREDIT_CARD.name,
            paymentStatus = PaymentStatus.PAID.name,
            driverId = null,
            driverName = null,
            driverPhone = null,
            createdAt = System.currentTimeMillis(),
            estimatedReadyTimeMinutes = 20
        )

        database.orderDao().insertOrder(orderEntity)
        database.orderItemDao().insertOrderItems(listOf(item1))

        // Trigger vibration
        triggerHapticAlert()

        orderEntity.toDomain(listOf(item1.toDomain()))
    }

    private fun triggerHapticAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 200), -1)
            }
        } catch (_: Exception) {
            // Ignore if permissions or device lacks vibrator
        }
    }
}

// Mapper extensions
private fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.STORE_OWNER },
    storeId = storeId
)

private fun StoreEntity.toDomain(): Store = Store(
    id = id,
    ownerId = ownerId,
    name = name,
    nameAr = nameAr,
    description = description,
    category = category,
    phone = phone,
    address = address,
    logoUrl = logoUrl,
    bannerUrl = bannerUrl,
    status = try { StoreStatus.valueOf(status) } catch (_: Exception) { StoreStatus.OPEN },
    openingHours = openingHours,
    minimumOrder = minimumOrder,
    deliveryFee = deliveryFee,
    currency = currency,
    rating = rating,
    totalOrdersCount = totalOrdersCount,
    autoAcceptOrders = autoAcceptOrders,
    soundAlertsEnabled = soundAlertsEnabled
)

private fun Store.toEntity(): StoreEntity = StoreEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    nameAr = nameAr,
    description = description,
    category = category,
    phone = phone,
    address = address,
    logoUrl = logoUrl,
    bannerUrl = bannerUrl,
    status = status.name,
    openingHours = openingHours,
    minimumOrder = minimumOrder,
    deliveryFee = deliveryFee,
    currency = currency,
    rating = rating,
    totalOrdersCount = totalOrdersCount,
    autoAcceptOrders = autoAcceptOrders,
    soundAlertsEnabled = soundAlertsEnabled
)

private fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    storeId = storeId,
    name = name,
    nameAr = nameAr,
    sortOrder = sortOrder
)

private fun ProductEntity.toDomain(categoryName: String = ""): Product = Product(
    id = id,
    storeId = storeId,
    categoryId = categoryId,
    categoryName = categoryName,
    name = name,
    nameAr = nameAr,
    description = description,
    price = price,
    image = image,
    isAvailable = isAvailable,
    preparationTimeMinutes = preparationTimeMinutes,
    createdAt = createdAt
)

private fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    id = id,
    orderId = orderId,
    productId = productId,
    productName = productName,
    productNameAr = productNameAr,
    unitPrice = unitPrice,
    quantity = quantity,
    totalPrice = totalPrice,
    specialInstructions = specialInstructions,
    optionsSummary = optionsSummary
)

private fun OrderEntity.toDomain(items: List<OrderItem>): Order = Order(
    id = id,
    orderNumber = orderNumber,
    storeId = storeId,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    deliveryAddress = deliveryAddress,
    status = try { OrderStatus.valueOf(status) } catch (_: Exception) { OrderStatus.PENDING },
    subtotal = subtotal,
    deliveryFee = deliveryFee,
    tax = tax,
    total = total,
    items = items,
    notes = notes,
    paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (_: Exception) { PaymentMethod.CREDIT_CARD },
    paymentStatus = try { PaymentStatus.valueOf(paymentStatus) } catch (_: Exception) { PaymentStatus.PAID },
    driverId = driverId,
    driverName = driverName,
    driverPhone = driverPhone,
    createdAt = createdAt,
    estimatedReadyTimeMinutes = estimatedReadyTimeMinutes
)
