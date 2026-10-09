package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role IN ('STORE_OWNER', 'STORE_MANAGER', 'STORE_STAFF')")
    suspend fun getAllStoreUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
    fun getStoreByIdFlow(storeId: String): Flow<StoreEntity?>

    @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
    suspend fun getStoreById(storeId: String): StoreEntity?

    @Query("SELECT * FROM stores")
    suspend fun getAllStores(): List<StoreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Query("UPDATE stores SET status = :status WHERE id = :storeId")
    suspend fun updateStoreStatus(storeId: String, status: String)

    @Update
    suspend fun updateStore(store: StoreEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE storeId = :storeId ORDER BY sortOrder ASC")
    fun getCategoriesForStore(storeId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND storeId = :storeId")
    suspend fun deleteCategory(id: String, storeId: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getProductsForStore(storeId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND storeId = :storeId LIMIT 1")
    suspend fun getProductById(id: String, storeId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isAvailable = :isAvailable WHERE id = :id AND storeId = :storeId")
    suspend fun updateProductAvailability(id: String, storeId: String, isAvailable: Boolean)

    @Query("DELETE FROM products WHERE id = :id AND storeId = :storeId")
    suspend fun deleteProduct(id: String, storeId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getOrdersForStore(storeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId AND status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(storeId: String, status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId AND status IN ('ACCEPTED', 'PREPARING', 'READY') ORDER BY createdAt DESC")
    fun getActiveOrders(storeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId AND status = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedOrders(storeId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId AND storeId = :storeId LIMIT 1")
    suspend fun getOrderById(orderId: String, storeId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId AND storeId = :storeId LIMIT 1")
    fun getOrderByIdFlow(orderId: String, storeId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId AND storeId = :storeId")
    suspend fun updateOrderStatus(orderId: String, storeId: String, newStatus: String)
}

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrderFlow(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId IN (:orderIds)")
    suspend fun getItemsForOrders(orderIds: List<String>): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId IN (:orderIds)")
    fun getItemsForOrdersFlow(orderIds: List<String>): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)
}
