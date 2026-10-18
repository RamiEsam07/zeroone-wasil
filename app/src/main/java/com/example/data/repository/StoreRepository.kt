package com.example.data.repository

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.api.CreateProductRequest
import com.example.data.api.UpdateProductRequest
import com.example.data.supabase.SupabaseRestClient
import com.example.data.supabase.SupabaseSessionProvider
import org.json.JSONArray
import org.json.JSONObject
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
import kotlinx.coroutines.flow.first
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

    private val _currentStoreId = MutableStateFlow("")
    val currentStoreId: StateFlow<String> = _currentStoreId.asStateFlow()

    private val supabase = SupabaseRestClient()

    init {
        coroutineScope.launch {
            // Seed local demo fixtures for previews only; never auto-authenticate a demo merchant.
            if (database.userDao().getUserById("usr_owner_01") == null) {
                database.populateInitialData()
            }
        }
    }

    private fun requireSession() =
        SupabaseSessionProvider.session
            ?: throw IllegalStateException("سجّل الدخول أولاً.")

    private suspend fun rows(path: String): JSONArray {
        val session = requireSession()
        return JSONArray(supabase.rest(path, accessToken = session.accessToken).getOrThrow())
    }

    suspend fun getActiveStoreCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        runCatching {
            val categoryRows = rows("rest/v1/categories?select=id,name_en,name_ar,sort_order&is_active=eq.true&order=sort_order.asc")
            (0 until categoryRows.length()).map { i ->
                val row = categoryRows.getJSONObject(i)
                Category(
                    id = row.optString("id"),
                    name = row.optString("name_en"),
                    nameAr = row.optString("name_ar"),
                    sortOrder = row.optInt("sort_order"),
                )
            }
        }
    }

    suspend fun createStoreForMerchant(
        nameAr: String,
        nameEn: String,
        categoryId: String,
        phone: String,
        address: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val session = requireSession()
            require(nameAr.isNotBlank() && nameEn.isNotBlank() && categoryId.isNotBlank() &&
                phone.isNotBlank() && address.isNotBlank()) { "أكمل بيانات المتجر المطلوبة." }
            val response = supabase.rest(
                "rest/v1/stores?select=id",
                method = "POST",
                body = JSONObject()
                    .put("owner_id", session.userId)
                    .put("name_ar", nameAr.trim())
                    .put("name_en", nameEn.trim())
                    .put("category_id", categoryId)
                    .put("phone", phone.trim())
                    .put("address_text", address.trim())
                    .put("description", "")
                    .put("is_open", false)
                    .toString(),
                accessToken = session.accessToken,
                prefer = "return=representation",
            ).getOrThrow()
            val created = JSONArray(response)
            check(created.length() > 0) { "لم يرجع الخادم المتجر بعد إنشائه." }
            created.getJSONObject(0).getString("id")
        }
    }

    suspend fun loginWithSupabaseUser(userId: String, email: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val session = requireSession()
                check(session.userId == userId) { "جلسة الحساب غير متطابقة." }
                val profiles = rows("rest/v1/profiles?select=full_name,phone,role,is_active&id=eq.$userId")
                check(profiles.length() > 0 && profiles.getJSONObject(0).optBoolean("is_active", false)) {
                    "حساب التاجر غير مفعّل."
                }
                val profile = profiles.getJSONObject(0)
                check(profile.optString("role") == "merchant") { "هذا التطبيق مخصص لحسابات التجار فقط." }

                val stores = rows("rest/v1/stores?select=*,category:categories(slug)&owner_id=eq.$userId&order=created_at.asc")
                check(stores.length() > 0) {
                    "تم تسجيل الدخول، لكن لا يوجد متجر مرتبط بالحساب بعد. يلزم إنشاء المتجر واعتماده أولاً."
                }
                val remoteStore = stores.getJSONObject(0)
                val storeId = remoteStore.getString("id")
                val user = User(
                    id = userId,
                    name = profile.optString("full_name").ifBlank { email.substringBefore("@") },
                    email = email,
                    phone = profile.optString("phone"),
                    role = UserRole.STORE_OWNER,
                    storeId = storeId,
                )
                database.userDao().insertUsers(
                    listOf(
                        UserEntity(
                            id = user.id,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            role = user.role.name,
                            storeId = user.storeId,
                        )
                    )
                )
                database.storeDao().insertStores(listOf(remoteStore.toStoreEntity()))
                _currentUser.value = user
                _currentStoreId.value = storeId
                syncRemoteData()
                user
            }
        }

    suspend fun refreshRemoteData() = withContext(Dispatchers.IO) {
        if (SupabaseSessionProvider.session != null && _currentStoreId.value.isNotBlank()) {
            syncRemoteData()
        }
    }

    private suspend fun syncRemoteData() {
        val storeId = _currentStoreId.value
        if (storeId.isBlank()) return
        val storeRows = rows("rest/v1/stores?select=*,category:categories(slug)&id=eq.$storeId")
        if (storeRows.length() > 0) database.storeDao().insertStores(listOf(storeRows.getJSONObject(0).toStoreEntity()))

        val categoryRows = rows("rest/v1/categories?select=id,name_ar,name_en,sort_order&order=sort_order.asc")
        val categoryEntities = (0 until categoryRows.length()).map { i ->
            val row = categoryRows.getJSONObject(i)
            CategoryEntity(
                id = row.optString("id"),
                storeId = storeId,
                name = row.optString("name_en"),
                nameAr = row.optString("name_ar"),
                sortOrder = row.optInt("sort_order"),
            )
        }
        if (categoryEntities.isNotEmpty()) database.categoryDao().insertCategories(categoryEntities)

        val productRows = rows("rest/v1/products?select=*&store_id=eq.$storeId&order=created_at.desc")
        val productEntities = (0 until productRows.length()).map { i ->
            val row = productRows.getJSONObject(i)
            ProductEntity(
                id = row.optString("id"),
                storeId = storeId,
                categoryId = row.optString("category"),
                name = row.optString("name_en"),
                nameAr = row.optString("name_ar"),
                description = row.optString("description"),
                price = row.optDouble("price"),
                image = row.optString("image_url"),
                isAvailable = row.optBoolean("is_available") && row.optInt("stock_quantity") > 0,
                preparationTimeMinutes = 15,
                createdAt = System.currentTimeMillis() - i,
            )
        }
        if (productEntities.isNotEmpty()) database.productDao().insertProducts(productEntities)

        val orderRows = rows("rest/v1/orders?select=*&store_id=eq.$storeId&order=placed_at.desc")
        val orderEntities = (0 until orderRows.length()).map { i ->
            val row = orderRows.getJSONObject(i)
            OrderEntity(
                id = row.optString("id"),
                orderNumber = "WAS-" + row.optLong("order_number"),
                storeId = storeId,
                customerId = row.optString("customer_id"),
                customerName = row.optString("customer_name_snapshot").ifBlank { "عميل وصّل" },
                customerPhone = row.optString("customer_phone_snapshot"),
                deliveryAddress = row.optString("delivery_address_snapshot"),
                status = row.optString("status").toMerchantOrderStatus(row.optString("cancellation_reason")),
                subtotal = row.optDouble("subtotal"),
                deliveryFee = row.optDouble("delivery_fee"),
                tax = row.optDouble("service_fee"),
                total = row.optDouble("total"),
                notes = row.optString("customer_notes"),
                paymentMethod = "CASH_ON_DELIVERY",
                paymentStatus = row.optString("payment_status").uppercase(),
                driverId = row.optString("driver_id").takeIf { it.isNotBlank() && it != "null" },
                driverName = null,
                driverPhone = null,
                createdAt = System.currentTimeMillis() - i,
                estimatedReadyTimeMinutes = 20,
            )
        }
        if (orderEntities.isNotEmpty()) database.orderDao().insertOrders(orderEntities)

        for (orderRow in orderRows.let { array -> (0 until array.length()).map { array.getJSONObject(it) } }) {
            val orderId = orderRow.optString("id")
            val itemRows = rows("rest/v1/order_items?select=*&order_id=eq.$orderId")
            val items = (0 until itemRows.length()).map { i ->
                val item = itemRows.getJSONObject(i)
                val quantity = item.optInt("quantity", 1)
                val price = item.optDouble("unit_price")
                OrderItemEntity(
                    id = item.optString("id"),
                    orderId = orderId,
                    productId = item.optString("product_id").takeIf { it != "null" }.orEmpty(),
                    productName = item.optString("product_name_snapshot"),
                    productNameAr = item.optString("product_name_snapshot"),
                    unitPrice = price,
                    quantity = quantity,
                    totalPrice = price * quantity,
                    specialInstructions = item.optString("special_instructions"),
                    optionsSummary = "",
                )
            }
            if (items.isNotEmpty()) database.orderItemDao().insertOrderItems(items)
        }
    }

    private fun JSONObject.toProductEntity(
        storeId: String,
        categories: List<CategoryEntity>,
        createdAt: Long = System.currentTimeMillis(),
    ): ProductEntity {
        val remoteCategory = optString("category")
        val category = categories.firstOrNull { it.nameAr == remoteCategory || it.name == remoteCategory }
        val stock = optInt("stock_quantity", 0)
        return ProductEntity(
            id = optString("id"),
            storeId = storeId,
            categoryId = category?.id ?: remoteCategory,
            name = optString("name_en"),
            nameAr = optString("name_ar"),
            description = optString("description"),
            price = optDouble("price"),
            image = optString("image_url"),
            isAvailable = optBoolean("is_active", true) && optBoolean("is_available") && stock > 0,
            preparationTimeMinutes = 15,
            createdAt = createdAt,
            stockQuantity = stock,
        )
    }

    private fun JSONObject.toStoreEntity(): StoreEntity = StoreEntity(
        id = optString("id"),
        ownerId = optString("owner_id"),
        name = optString("name_en"),
        nameAr = optString("name_ar"),
        description = optString("description"),
        category = optJSONObject("category")?.optString("slug").orEmpty(),
        phone = optString("phone"),
        address = optString("address_text"),
        logoUrl = optString("image_url"),
        bannerUrl = optString("image_url"),
        status = if (optBoolean("is_active", false) && optBoolean("is_open", false)) "OPEN" else "CLOSED",
        openingHours = "حسب ساعات عمل المتجر",
        minimumOrder = 0.0,
        deliveryFee = 0.0,
        currency = "SDG",
        rating = 0.0,
        totalOrdersCount = 0,
        autoAcceptOrders = false,
        soundAlertsEnabled = true,
    )

    private fun String.toMerchantOrderStatus(reason: String = ""): String = when (this) {
        "confirmed" -> "ACCEPTED"
        "preparing" -> "PREPARING"
        "ready_for_pickup", "driver_assigned", "picked_up", "on_the_way" -> "READY"
        "delivered" -> "COMPLETED"
        "cancelled" -> if (reason.contains("رفض")) "REJECTED" else "CANCELLED"
        else -> "PENDING"
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
        _currentStoreId.value = ""
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
        runCatching {
            val storeId = _currentStoreId.value
            val order = database.orderDao().getOrderById(orderId, storeId)
                ?: throw IllegalArgumentException("Order not found or unauthorized for this store")
            val currentStatus = runCatching { OrderStatus.valueOf(order.status) }.getOrDefault(OrderStatus.PENDING)
            check(currentStatus.canTransitionTo(newStatus)) {
                "Invalid order transition from $currentStatus to $newStatus"
            }

            val session = requireSession()
            val response = when (newStatus) {
                OrderStatus.ACCEPTED -> supabase.rest(
                    "rest/v1/rpc/wasil_merchant_update_order_status",
                    method = "POST",
                    body = JSONObject()
                        .put("p_order_id", orderId)
                        .put("p_next_status", "confirmed")
                        .toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                OrderStatus.PREPARING -> supabase.rest(
                    "rest/v1/rpc/wasil_merchant_update_order_status",
                    method = "POST",
                    body = JSONObject()
                        .put("p_order_id", orderId)
                        .put("p_next_status", "preparing")
                        .toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                OrderStatus.READY -> supabase.rest(
                    "rest/v1/rpc/wasil_merchant_update_order_status",
                    method = "POST",
                    body = JSONObject()
                        .put("p_order_id", orderId)
                        .put("p_next_status", "ready_for_pickup")
                        .toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                OrderStatus.REJECTED, OrderStatus.CANCELLED -> supabase.rest(
                    "rest/v1/rpc/wasil_cancel_order",
                    method = "POST",
                    body = JSONObject()
                        .put("p_order_id", orderId)
                        .put("p_reason", if (newStatus == OrderStatus.REJECTED) "رفض الطلب من المتجر" else "إلغاء الطلب من المتجر")
                        .toString(),
                    accessToken = session.accessToken,
                ).getOrThrow().trim()
                else -> throw IllegalArgumentException("هذه الحالة لا يحدّثها المتجر؛ يتولاها المندوب أو النظام.")
            }
            check(response == "true") { "لم يقبل الخادم تحديث حالة الطلب." }
            syncRemoteData()
        }
    }

    suspend fun updateStoreStatus(status: StoreStatus) = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val session = requireSession()
        supabase.rest(
            "rest/v1/stores?id=eq.$storeId",
            method = "PATCH",
            body = JSONObject().put("is_open", status == StoreStatus.OPEN).toString(),
            accessToken = session.accessToken,
        ).getOrThrow()
        syncRemoteData()
    }

    suspend fun updateStore(store: Store) = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val session = requireSession()
        supabase.rest(
            "rest/v1/stores?id=eq.$storeId",
            method = "PATCH",
            body = JSONObject()
                .put("name_en", store.name)
                .put("name_ar", store.nameAr)
                .put("description", store.description)
                .put("phone", store.phone)
                .put("address_text", store.address)
                .put("image_url", store.logoUrl)
                .put("is_open", store.status == "OPEN")
                .toString(),
            accessToken = session.accessToken,
        ).getOrThrow()
        syncRemoteData()
    }

    // Product CRUD: writes are sent to Supabase; local Room is a cache for the current store.
    suspend fun createProduct(request: CreateProductRequest): Result<Product> = withContext(Dispatchers.IO) {
        runCatching {
            val storeId = _currentStoreId.value
            val session = requireSession()
            val categories = database.categoryDao().getCategoriesForStore(storeId).first()
            val category = categories.firstOrNull { it.id == request.categoryId }
            val categoryName = category?.let { it.nameAr.ifBlank { it.name } } ?: request.categoryId
            val body = JSONObject()
                .put("store_id", storeId)
                .put("name_en", request.name.trim())
                .put("name_ar", request.nameAr.trim().ifBlank { request.name.trim() })
                .put("description", request.description.trim())
                .put("price", request.price)
                .put("category", categoryName)
                .put("image_url", request.image.trim())
                .put("stock_quantity", request.stockQuantity)
                .put("is_available", request.isAvailable && request.stockQuantity > 0)
            val response = supabase.rest(
                "rest/v1/products?select=*",
                method = "POST",
                body = body.toString(),
                accessToken = session.accessToken,
                prefer = "return=representation",
            ).getOrThrow()
            val rows = JSONArray(response)
            check(rows.length() > 0) { "لم يرجع الخادم المنتج بعد حفظه." }
            val entity = rows.getJSONObject(0).toProductEntity(storeId, categories)
            database.productDao().insertProduct(entity)
            entity.toDomain(categoryName)
        }
    }

    suspend fun updateProduct(productId: String, request: UpdateProductRequest): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val storeId = _currentStoreId.value
            val session = requireSession()
            val existingRows = rows("rest/v1/products?select=*&id=eq.$productId&store_id=eq.$storeId")
            check(existingRows.length() > 0) { "المنتج غير موجود أو لا يتبع متجرك." }
            val existing = existingRows.getJSONObject(0)
            val currentStock = existing.optInt("stock_quantity", 0)
            val nextStock = request.stockQuantity ?: currentStock
            val body = JSONObject()
            request.name?.let { body.put("name_en", it.trim()) }
            request.nameAr?.let { body.put("name_ar", it.trim()) }
            request.description?.let { body.put("description", it.trim()) }
            request.price?.let { body.put("price", it) }
            request.image?.let { body.put("image_url", it.trim()) }
            request.stockQuantity?.let { body.put("stock_quantity", it) }
            request.categoryId?.let { categoryId ->
                val categories = database.categoryDao().getCategoriesForStore(storeId).first()
                val category = categories.firstOrNull { it.id == categoryId }
                body.put("category", category?.let { it.nameAr.ifBlank { it.name } } ?: categoryId)
            }
            request.isAvailable?.let { body.put("is_available", it && nextStock > 0) }
            if (body.length() > 0) {
                supabase.rest(
                    "rest/v1/products?id=eq.$productId&store_id=eq.$storeId",
                    method = "PATCH",
                    body = body.toString(),
                    accessToken = session.accessToken,
                ).getOrThrow()
            }
            syncRemoteData()
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val storeId = _currentStoreId.value
            val session = requireSession()
            supabase.rest(
                "rest/v1/products?id=eq.$productId&store_id=eq.$storeId",
                method = "DELETE",
                accessToken = session.accessToken,
            ).getOrThrow()
            database.productDao().deleteProduct(productId, storeId)
        }
    }

    suspend fun toggleProductAvailability(productId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        val storeId = _currentStoreId.value
        val session = requireSession()
        val rows = rows("rest/v1/products?select=stock_quantity&id=eq.$productId&store_id=eq.$storeId")
        check(rows.length() > 0) { "المنتج غير موجود." }
        val stock = rows.getJSONObject(0).optInt("stock_quantity", 0)
        check(!isAvailable || stock > 0) { "لا يمكن إتاحة منتج دون كمية مخزون أكبر من صفر." }
        supabase.rest(
            "rest/v1/products?id=eq.$productId&store_id=eq.$storeId",
            method = "PATCH",
            body = JSONObject().put("is_available", isAvailable).toString(),
            accessToken = session.accessToken,
        ).getOrThrow()
        syncRemoteData()
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
    createdAt = createdAt,
    stockQuantity = stockQuantity
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
