package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.CreateProductRequest
import com.example.data.api.UpdateProductRequest
import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.Store
import com.example.data.model.StoreStatus
import com.example.data.model.User
import com.example.data.repository.StoreRepository
import com.example.data.supabase.SupabaseSessionProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StoreNavScreen {
    LOGIN,
    DASHBOARD,
    NEW_ORDERS,
    ORDER_DETAILS,
    ACTIVE_ORDERS,
    COMPLETED_ORDERS,
    PRODUCTS,
    ADD_PRODUCT,
    EDIT_PRODUCT,
    CATEGORIES,
    STORE_PROFILE,
    STORE_SETTINGS
}

data class DashboardMetrics(
    val newOrdersCount: Int = 0,
    val acceptedOrdersCount: Int = 0,
    val preparingOrdersCount: Int = 0,
    val readyOrdersCount: Int = 0,
    val completedOrdersCount: Int = 0,
    val todaySalesAmount: Double = 0.0,
    val storeStatus: StoreStatus = StoreStatus.OPEN
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StoreRepository(application)

    val currentUser: StateFlow<User?> = repository.currentUser
    val currentStore: StateFlow<Store?> = repository.currentStore.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val categories: StateFlow<List<Category>> = repository.storeCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val products: StateFlow<List<Product>> = repository.storeProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val orders: StateFlow<List<Order>> = repository.storeOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI State
    private val _currentScreen = MutableStateFlow(StoreNavScreen.DASHBOARD)
    val currentScreen: StateFlow<StoreNavScreen> = _currentScreen.asStateFlow()

    private val _selectedOrderId = MutableStateFlow<String?>(null)
    val selectedOrderId: StateFlow<String?> = _selectedOrderId.asStateFlow()

    private val _editingProductId = MutableStateFlow<String?>(null)
    val editingProductId: StateFlow<String?> = _editingProductId.asStateFlow()

    private val _isArabic = MutableStateFlow(false)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedProductCategory = MutableStateFlow<String?>(null)
    val selectedProductCategory: StateFlow<String?> = _selectedProductCategory.asStateFlow()

    private val _onlyAvailableFilter = MutableStateFlow(false)
    val onlyAvailableFilter: StateFlow<Boolean> = _onlyAvailableFilter.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _demoAccounts = MutableStateFlow<List<Pair<User, Store>>>(emptyList())
    val demoAccounts: StateFlow<List<Pair<User, Store>>> = _demoAccounts.asStateFlow()

    // Calculated Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        orders,
        currentStore
    ) { ordersList, store ->
        val newCount = ordersList.count { it.status == OrderStatus.PENDING }
        val acceptedCount = ordersList.count { it.status == OrderStatus.ACCEPTED }
        val preparingCount = ordersList.count { it.status == OrderStatus.PREPARING }
        val readyCount = ordersList.count { it.status == OrderStatus.READY }
        val completedList = ordersList.filter { it.status == OrderStatus.COMPLETED }
        val completedCount = completedList.size
        val todaySales = completedList.sumOf { it.total }

        DashboardMetrics(
            newOrdersCount = newCount,
            acceptedOrdersCount = acceptedCount,
            preparingOrdersCount = preparingCount,
            readyOrdersCount = readyCount,
            completedOrdersCount = completedCount,
            todaySalesAmount = todaySales,
            storeStatus = store?.status ?: StoreStatus.OPEN
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    init {
        loadDemoAccounts()
    }

    private fun loadDemoAccounts() {
        viewModelScope.launch {
            _demoAccounts.value = repository.getAvailableDemoAccounts()
        }
    }

    fun navigateTo(screen: StoreNavScreen) {
        _currentScreen.value = screen
    }

    fun selectOrder(orderId: String?) {
        _selectedOrderId.value = orderId
        if (orderId != null) {
            _currentScreen.value = StoreNavScreen.ORDER_DETAILS
        }
    }

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun setLanguage(arabic: Boolean) {
        _isArabic.value = arabic
    }

    fun setProductSearch(query: String) {
        _productSearchQuery.value = query
    }

    fun setProductCategoryFilter(categoryId: String?) {
        _selectedProductCategory.value = categoryId
    }

    fun toggleAvailableFilter() {
        _onlyAvailableFilter.value = !_onlyAvailableFilter.value
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // Authentication
    fun loginAsDemoAccount(user: User) {
        viewModelScope.launch {
            val success = repository.loginAsUser(user)
            if (success) {
                _currentScreen.value = StoreNavScreen.DASHBOARD
                showMessage(if (_isArabic.value) "مرحباً ${user.name}" else "Welcome, ${user.name}")
            }
        }
    }

    fun loginWithEmail(email: String) {
        viewModelScope.launch {
            val result = repository.loginWithEmail(email)
            result.onSuccess { user ->
                _currentScreen.value = StoreNavScreen.DASHBOARD
                showMessage(if (_isArabic.value) "تم تسجيل الدخول بنجاح" else "Logged in successfully as ${user.name}")
            }.onFailure { err ->
                showMessage(err.message ?: "Authentication failed")
            }
        }
    }

    suspend fun loginWithSupabase(userId: String, email: String): Result<User> {
        val result = repository.loginWithSupabaseUser(userId, email)
        result.onSuccess { user ->
            _currentScreen.value = StoreNavScreen.DASHBOARD
            showMessage(if (_isArabic.value) "تم تسجيل الدخول إلى المتجر" else "Signed in to store")
        }.onFailure { error ->
            showMessage(error.message ?: "تعذر تحميل المتجر.")
        }
        return result
    }

    suspend fun loadStoreCategories(): Result<List<Category>> = repository.getActiveStoreCategories()

    suspend fun createStoreForMerchant(
        nameAr: String,
        nameEn: String,
        categoryId: String,
        phone: String,
        address: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Result<User> {
        val session = SupabaseSessionProvider.session
            ?: return Result.failure(IllegalStateException("سجّل الدخول أولاً."))
        val created = repository.createStoreForMerchant(nameAr, nameEn, categoryId, phone, address, latitude, longitude)
        if (created.isFailure) return Result.failure(created.exceptionOrNull() ?: IllegalStateException("تعذر إنشاء المتجر."))
        return loginWithSupabase(session.userId, session.email.orEmpty())
    }

    fun refreshRemoteData() {
        viewModelScope.launch { repository.refreshRemoteData() }
    }

    fun logout() {
        repository.logout()
        _currentScreen.value = StoreNavScreen.LOGIN
    }

    // Store Status
    fun updateStoreStatus(status: StoreStatus) {
        viewModelScope.launch {
            repository.updateStoreStatus(status)
            val msg = if (_isArabic.value) "تم تغيير حالة المتجر إلى ${status.getDisplayName(true)}"
                      else "Store status updated to ${status.name}"
            showMessage(msg)
        }
    }

    fun updateStoreSettings(store: Store) {
        viewModelScope.launch {
            repository.updateStore(store)
            showMessage(if (_isArabic.value) "تم حفظ الإعدادات" else "Store settings saved")
        }
    }

    // Order Transitions
    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, OrderStatus.ACCEPTED)
            result.onSuccess {
                showMessage(if (_isArabic.value) "تم قبول الطلب بنجاح" else "Order accepted")
            }.onFailure { e ->
                showMessage(e.message ?: "Failed to accept order")
            }
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, OrderStatus.REJECTED)
            result.onSuccess {
                showMessage(if (_isArabic.value) "تم رفض الطلب" else "Order rejected")
                if (_currentScreen.value == StoreNavScreen.ORDER_DETAILS) {
                    _currentScreen.value = StoreNavScreen.NEW_ORDERS
                }
            }.onFailure { e ->
                showMessage(e.message ?: "Failed to reject order")
            }
        }
    }

    fun markOrderPreparing(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, OrderStatus.PREPARING)
            result.onSuccess {
                showMessage(if (_isArabic.value) "بدأ تحضير الطلب في المطبخ" else "Order marked as Preparing")
            }.onFailure { e ->
                showMessage(e.message ?: "Error updating order status")
            }
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, OrderStatus.READY)
            result.onSuccess {
                showMessage(if (_isArabic.value) "الطلب جاهز للتسليم الآن" else "Order marked Ready for Pickup")
            }.onFailure { e ->
                showMessage(e.message ?: "Error updating order status")
            }
        }
    }

    fun markOrderCompleted(orderId: String) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            result.onSuccess {
                showMessage(if (_isArabic.value) "تم تسليم الطلب بنجاح" else "Order completed successfully")
                if (_currentScreen.value == StoreNavScreen.ORDER_DETAILS) {
                    _currentScreen.value = StoreNavScreen.ACTIVE_ORDERS
                }
            }.onFailure { e ->
                showMessage(e.message ?: "Error updating order status")
            }
        }
    }

    // Product CRUD
    fun toggleProductAvailability(productId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.toggleProductAvailability(productId, isAvailable)
            val msg = if (isAvailable) {
                if (_isArabic.value) "تم تفعيل توفر المنتج" else "Product is now Available"
            } else {
                if (_isArabic.value) "تم إيقاف توفر المنتج مؤقتاً" else "Product marked Unavailable"
            }
            showMessage(msg)
        }
    }

    fun startAddProduct() {
        _editingProductId.value = null
        _currentScreen.value = StoreNavScreen.ADD_PRODUCT
    }

    fun startEditProduct(productId: String) {
        _editingProductId.value = productId
        _currentScreen.value = StoreNavScreen.EDIT_PRODUCT
    }

    fun saveProduct(
        name: String,
        nameAr: String,
        description: String,
        price: Double,
        categoryId: String,
        image: String,
        isAvailable: Boolean,
        prepMinutes: Int,
        stockQuantity: Int = 0
    ) {
        viewModelScope.launch {
            val editId = _editingProductId.value
            if (editId == null) {
                val req = CreateProductRequest(
                    name = name,
                    nameAr = nameAr,
                    description = description,
                    price = price,
                    categoryId = categoryId,
                    image = image,
                    isAvailable = isAvailable,
                    preparationTimeMinutes = prepMinutes,
                    stockQuantity = stockQuantity
                )
                repository.createProduct(req).onSuccess {
                    showMessage(if (_isArabic.value) "تمت إضافة المنتج بنجاح" else "Product created successfully")
                    _currentScreen.value = StoreNavScreen.PRODUCTS
                }.onFailure { e ->
                    showMessage(e.message ?: "Error creating product")
                }
            } else {
                val req = UpdateProductRequest(
                    name = name,
                    nameAr = nameAr,
                    description = description,
                    price = price,
                    categoryId = categoryId,
                    image = image,
                    isAvailable = isAvailable,
                    preparationTimeMinutes = prepMinutes
                )
                repository.updateProduct(editId, req).onSuccess {
                    showMessage(if (_isArabic.value) "تم تحديث المنتج بنجاح" else "Product updated successfully")
                    _currentScreen.value = StoreNavScreen.PRODUCTS
                }.onFailure { e ->
                    showMessage(e.message ?: "Error updating product")
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId).onSuccess {
                showMessage(if (_isArabic.value) "تم حذف المنتج" else "Product deleted")
                if (_editingProductId.value == productId) {
                    _editingProductId.value = null
                    _currentScreen.value = StoreNavScreen.PRODUCTS
                }
            }.onFailure { e ->
                showMessage(e.message ?: "Error deleting product")
            }
        }
    }

    // Category CRUD
    fun addCategory(name: String, nameAr: String) {
        viewModelScope.launch {
            repository.createCategory(name, nameAr).onSuccess {
                showMessage(if (_isArabic.value) "تمت إضافة التصنيف" else "Category added")
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
            showMessage(if (_isArabic.value) "تم تحديث التصنيف" else "Category updated")
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            showMessage(if (_isArabic.value) "تم حذف التصنيف" else "Category deleted")
        }
    }

    // Live Order Simulation
    fun simulateIncomingOrder() {
        viewModelScope.launch {
            val order = repository.simulateIncomingOrder()
            val text = if (_isArabic.value) "وصل طلب جديد! #${order.orderNumber} بقيمة $${order.total}"
                       else "New order received! #${order.orderNumber} ($${order.total})"
            showMessage(text)
        }
    }
}
