package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StoreEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stores ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE stores ADD COLUMN longitude REAL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN serviceFee REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE orders ADD COLUMN platformCommissionAmount REAL NOT NULL DEFAULT 0.0")
            }
        }

        @Volatile
        private var INSTANCE: StoreDatabase? = null

        fun getInstance(context: Context): StoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoreDatabase::class.java,
                    "store_operations_portal.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).populateInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        val users = listOf(
            UserEntity(
                id = "usr_owner_01",
                name = "Karim Al-Hassan",
                email = "karim@almadinagrill.com",
                phone = "+1 (555) 234-5678",
                role = "STORE_OWNER",
                storeId = "str_001"
            ),
            UserEntity(
                id = "usr_mgr_01",
                name = "Laila Tariq",
                email = "laila@almadinagrill.com",
                phone = "+1 (555) 345-6789",
                role = "STORE_MANAGER",
                storeId = "str_001"
            ),
            UserEntity(
                id = "usr_staff_01",
                name = "Omar Zaid",
                email = "omar@almadinagrill.com",
                phone = "+1 (555) 456-7890",
                role = "STORE_STAFF",
                storeId = "str_001"
            ),
            UserEntity(
                id = "usr_owner_02",
                name = "Nour Mansour",
                email = "nour@saffronoasis.com",
                phone = "+1 (555) 987-6543",
                role = "STORE_OWNER",
                storeId = "str_002"
            )
        )
        userDao().insertUsers(users)

        val stores = listOf(
            StoreEntity(
                id = "str_001",
                ownerId = "usr_owner_01",
                name = "Al-Madina Gourmet Grill",
                nameAr = "مشاوي المدينة الفاخرة",
                description = "Authentic charcoal grilled meats, artisanal kebabs & mezze platters",
                category = "Grill & Middle Eastern",
                phone = "+1 (555) 019-2831",
                address = "424 Crescent Blvd, Downtown Central",
                logoUrl = "",
                bannerUrl = "",
                status = "OPEN",
                openingHours = "10:30 AM - 11:30 PM",
                minimumOrder = 18.0,
                deliveryFee = 3.99,
                currency = "USD",
                rating = 4.9,
                totalOrdersCount = 2840,
                autoAcceptOrders = false,
                soundAlertsEnabled = true
            ),
            StoreEntity(
                id = "str_002",
                ownerId = "usr_owner_02",
                name = "Saffron Oasis Bakery",
                nameAr = "مخبز واحة الزعفران",
                description = "Freshly baked artisan pastries, baklava, and cardamom espresso",
                category = "Bakery & Cafe",
                phone = "+1 (555) 782-9912",
                address = "88 Palm Promenade, Suite 12",
                logoUrl = "",
                bannerUrl = "",
                status = "OPEN",
                openingHours = "07:00 AM - 09:00 PM",
                minimumOrder = 12.0,
                deliveryFee = 2.99,
                currency = "USD",
                rating = 4.8,
                totalOrdersCount = 1910,
                autoAcceptOrders = true,
                soundAlertsEnabled = true
            )
        )
        storeDao().insertStores(stores)

        // Categories for store 1
        val categoriesStore1 = listOf(
            CategoryEntity("cat_g1", "str_001", "Signature Grills", "مشاوي مميزة", 1),
            CategoryEntity("cat_g2", "str_001", "Gourmet Burgers & Wraps", "برجر ولفائف فاخرة", 2),
            CategoryEntity("cat_g3", "str_001", "Hot & Cold Mezza", "مقبلات ومزة", 3),
            CategoryEntity("cat_g4", "str_001", "Fresh Mocktails & Drinks", "مشروبات وعصائر طازجة", 4)
        )
        // Categories for store 2
        val categoriesStore2 = listOf(
            CategoryEntity("cat_b1", "str_002", "Artisan Breads & Croissants", "مخبوزات وكرواسون", 1),
            CategoryEntity("cat_b2", "str_002", "Baklava & Kunafa", "بقلاوة وكنافة", 2),
            CategoryEntity("cat_b3", "str_002", "Specialty Coffees", "قهوة مختصة", 3)
        )
        categoryDao().insertCategories(categoriesStore1 + categoriesStore2)

        // Products for Store 1
        val productsStore1 = listOf(
            ProductEntity(
                id = "prd_01",
                storeId = "str_001",
                categoryId = "cat_g1",
                name = "Wagyu Beef Kebab Platter",
                nameAr = "طبق كباب واغيو فاخر",
                description = "Minced prime Australian Wagyu, sumac onion salad, grilled tomatoes, and warm saffron rice.",
                price = 24.50,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 18,
                createdAt = System.currentTimeMillis() - 86400000L * 10
            ),
            ProductEntity(
                id = "prd_02",
                storeId = "str_001",
                categoryId = "cat_g1",
                name = "Citrus Garlic Shish Tawook",
                nameAr = "شيش طاووق بالثوم والحمضيات",
                description = "Tender marinated chicken breast cubes charbroiled on skewers with house toum garlic dip.",
                price = 19.90,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 15,
                createdAt = System.currentTimeMillis() - 86400000L * 9
            ),
            ProductEntity(
                id = "prd_03",
                storeId = "str_001",
                categoryId = "cat_g1",
                name = "Rosemary Lamb Chops (4 pcs)",
                nameAr = "ريش ضأن بالروزماري",
                description = "Grass-fed lamb chops seasoned with fresh herbs and roasted baby potatoes.",
                price = 29.00,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 22,
                createdAt = System.currentTimeMillis() - 86400000L * 8
            ),
            ProductEntity(
                id = "prd_04",
                storeId = "str_001",
                categoryId = "cat_g2",
                name = "Smoked Shawarma Truffle Brioche",
                nameAr = "برجر شاورما بالكمأة وبريوش",
                description = "Slow-roasted beef shavings, black truffle mayo, pickled cucumbers, and melted provolone in brioche.",
                price = 16.50,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 12,
                createdAt = System.currentTimeMillis() - 86400000L * 7
            ),
            ProductEntity(
                id = "prd_05",
                storeId = "str_001",
                categoryId = "cat_g2",
                name = "Crispy Falafel Pita Pocket",
                nameAr = "سندويش فلافل مقرمشة",
                description = "Golden herb falafel patties, tahini drizzle, mint, turnip pickles, and chopped village salad.",
                price = 11.00,
                image = "",
                isAvailable = false, // unavailable sample to test availability switch!
                preparationTimeMinutes = 10,
                createdAt = System.currentTimeMillis() - 86400000L * 6
            ),
            ProductEntity(
                id = "prd_06",
                storeId = "str_001",
                categoryId = "cat_g3",
                name = "Charred Eggplant Mutabal",
                nameAr = "متبل باذنجان مشوي",
                description = "Smoky eggplant dip whipped with sesame paste, extra virgin olive oil, and pomegranate pearls.",
                price = 8.50,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 6,
                createdAt = System.currentTimeMillis() - 86400000L * 5
            ),
            ProductEntity(
                id = "prd_07",
                storeId = "str_001",
                categoryId = "cat_g3",
                name = "Crispy Halloumi Sticks",
                nameAr = "أصابع حلوم مقرمشة",
                description = "Crusted halloumi fries served with spicy za'atar honey dipping sauce.",
                price = 9.50,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 8,
                createdAt = System.currentTimeMillis() - 86400000L * 4
            ),
            ProductEntity(
                id = "prd_08",
                storeId = "str_001",
                categoryId = "cat_g4",
                name = "Iced Mint Lemonade Freeze",
                nameAr = "ليمون بالنعناع مثلج",
                description = "Freshly squeezed lemon juice crushed with fragrant garden mint and crushed ice.",
                price = 5.50,
                image = "",
                isAvailable = true,
                preparationTimeMinutes = 4,
                createdAt = System.currentTimeMillis() - 86400000L * 3
            )
        )
        productDao().insertProducts(productsStore1)

        // Seed Orders for Store 1
        val now = System.currentTimeMillis()
        val ordersStore1 = listOf(
            // 1. PENDING (New Order)
            OrderEntity(
                id = "ord_9101",
                orderNumber = "ORD-9101",
                storeId = "str_001",
                customerId = "cust_201",
                customerName = "Zaid Al-Mutawa",
                customerPhone = "+1 (555) 712-3849",
                deliveryAddress = "158 King Fahd Way, Apt 4B, North District",
                status = "PENDING",
                subtotal = 44.40,
                deliveryFee = 3.99,
                tax = 3.55,
                total = 51.94,
                notes = "Extra garlic dip please! Door buzzer #42.",
                paymentMethod = "CREDIT_CARD",
                paymentStatus = "PAID",
                driverId = null,
                driverName = null,
                driverPhone = null,
                createdAt = now - 4 * 60 * 1000L, // 4 mins ago
                estimatedReadyTimeMinutes = 20
            ),
            // 2. PENDING (New Order 2)
            OrderEntity(
                id = "ord_9102",
                orderNumber = "ORD-9102",
                storeId = "str_001",
                customerId = "cust_202",
                customerName = "Maya Khouri",
                customerPhone = "+1 (555) 621-9870",
                deliveryAddress = "72 Horizon Towers, Floor 14",
                status = "PENDING",
                subtotal = 38.00,
                deliveryFee = 3.99,
                tax = 3.04,
                total = 45.03,
                notes = "Cut skewers into individual portions if possible.",
                paymentMethod = "APPLE_PAY",
                paymentStatus = "PAID",
                driverId = null,
                driverName = null,
                driverPhone = null,
                createdAt = now - 9 * 60 * 1000L, // 9 mins ago
                estimatedReadyTimeMinutes = 18
            ),
            // 3. ACCEPTED
            OrderEntity(
                id = "ord_9099",
                orderNumber = "ORD-9099",
                storeId = "str_001",
                customerId = "cust_203",
                customerName = "Farhan Salem",
                customerPhone = "+1 (555) 341-8899",
                deliveryAddress = "304 Palm Grove Villas",
                status = "ACCEPTED",
                subtotal = 49.00,
                deliveryFee = 3.99,
                tax = 3.92,
                total = 56.91,
                notes = "Ring bell on arrival",
                paymentMethod = "CREDIT_CARD",
                paymentStatus = "PAID",
                driverId = "drv_55",
                driverName = "Sami Courier",
                driverPhone = "+1 (555) 991-2244",
                createdAt = now - 18 * 60 * 1000L,
                estimatedReadyTimeMinutes = 15
            ),
            // 4. PREPARING
            OrderEntity(
                id = "ord_9098",
                orderNumber = "ORD-9098",
                storeId = "str_001",
                customerId = "cust_204",
                customerName = "Rania Haddad",
                customerPhone = "+1 (555) 882-1133",
                deliveryAddress = "99 Marina Walk, Gate 2",
                status = "PREPARING",
                subtotal = 25.40,
                deliveryFee = 3.99,
                tax = 2.03,
                total = 31.42,
                notes = "Medium well cooked kebabs",
                paymentMethod = "WALLET",
                paymentStatus = "PAID",
                driverId = "drv_56",
                driverName = "Bilal Delivery",
                driverPhone = "+1 (555) 773-8822",
                createdAt = now - 28 * 60 * 1000L,
                estimatedReadyTimeMinutes = 10
            ),
            // 5. READY
            OrderEntity(
                id = "ord_9095",
                orderNumber = "ORD-9095",
                storeId = "str_001",
                customerId = "cust_205",
                customerName = "Tariq Abboud",
                customerPhone = "+1 (555) 440-9922",
                deliveryAddress = "510 Financial Harbor St",
                status = "READY",
                subtotal = 54.00,
                deliveryFee = 3.99,
                tax = 4.32,
                total = 62.31,
                notes = "Driver is already at pickup bay #3",
                paymentMethod = "CREDIT_CARD",
                paymentStatus = "PAID",
                driverId = "drv_55",
                driverName = "Sami Courier",
                driverPhone = "+1 (555) 991-2244",
                createdAt = now - 42 * 60 * 1000L,
                estimatedReadyTimeMinutes = 0
            ),
            // 6. COMPLETED
            OrderEntity(
                id = "ord_9088",
                orderNumber = "ORD-9088",
                storeId = "str_001",
                customerId = "cust_206",
                customerName = "Huda Al-Nuaimi",
                customerPhone = "+1 (555) 332-1100",
                deliveryAddress = "81 Al-Bustan Residence",
                status = "COMPLETED",
                subtotal = 68.50,
                deliveryFee = 3.99,
                tax = 5.48,
                total = 77.97,
                notes = "Completed successfully. Customer left 5 star rating.",
                paymentMethod = "APPLE_PAY",
                paymentStatus = "PAID",
                driverId = "drv_58",
                driverName = "Hassan Express",
                driverPhone = "+1 (555) 662-7711",
                createdAt = now - 3 * 3600 * 1000L,
                estimatedReadyTimeMinutes = 0
            ),
            // 7. COMPLETED
            OrderEntity(
                id = "ord_9082",
                orderNumber = "ORD-9082",
                storeId = "str_001",
                customerId = "cust_207",
                customerName = "Adel Mansour",
                customerPhone = "+1 (555) 221-8844",
                deliveryAddress = "12 Coral Bay Rd",
                status = "COMPLETED",
                subtotal = 34.00,
                deliveryFee = 3.99,
                tax = 2.72,
                total = 40.71,
                notes = "",
                paymentMethod = "CASH_ON_DELIVERY",
                paymentStatus = "PAID",
                driverId = "drv_58",
                driverName = "Hassan Express",
                driverPhone = "+1 (555) 662-7711",
                createdAt = now - 5 * 3600 * 1000L,
                estimatedReadyTimeMinutes = 0
            )
        )
        orderDao().insertOrders(ordersStore1)

        // Order Items for the orders
        val orderItems = listOf(
            // Items for ord_9101
            OrderItemEntity("oit_01", "ord_9101", "prd_01", "Wagyu Beef Kebab Platter", "طبق كباب واغيو فاخر", 24.50, 1, 24.50, "Extra sumac onions", "1x Platter"),
            OrderItemEntity("oit_02", "ord_9101", "prd_02", "Citrus Garlic Shish Tawook", "شيش طاووق بالثوم والحمضيات", 19.90, 1, 19.90, "Well done charred", "1x Plate"),
            
            // Items for ord_9102
            OrderItemEntity("oit_03", "ord_9102", "prd_03", "Rosemary Lamb Chops (4 pcs)", "ريش ضأن بالروزماري", 29.00, 1, 29.00, "", "1x 4-pieces"),
            OrderItemEntity("oit_04", "ord_9102", "prd_07", "Crispy Halloumi Sticks", "أصابع حلوم مقرمشة", 9.50, 1, 9.50, "Dip on the side", "1x Order"),

            // Items for ord_9099
            OrderItemEntity("oit_05", "ord_9099", "prd_01", "Wagyu Beef Kebab Platter", "طبق كباب واغيو فاخر", 24.50, 2, 49.00, "Spicy bread included", "2x Platters"),

            // Items for ord_9098
            OrderItemEntity("oit_06", "ord_9098", "prd_04", "Smoked Shawarma Truffle Brioche", "برجر شاورما بالكمأة وبريوش", 16.50, 1, 16.50, "", "1x Brioche"),
            OrderItemEntity("oit_07", "ord_9098", "prd_06", "Charred Eggplant Mutabal", "متبل باذنجان مشوي", 8.50, 1, 8.50, "", "1x Cold Dip"),

            // Items for ord_9095
            OrderItemEntity("oit_08", "ord_9095", "prd_03", "Rosemary Lamb Chops (4 pcs)", "ريش ضأن بالروزماري", 29.00, 1, 29.00, "", "1x Chops"),
            OrderItemEntity("oit_09", "ord_9095", "prd_01", "Wagyu Beef Kebab Platter", "طبق كباب واغيو فاخر", 24.50, 1, 24.50, "", "1x Platter"),

            // Items for ord_9088
            OrderItemEntity("oit_10", "ord_9088", "prd_01", "Wagyu Beef Kebab Platter", "طبق كباب واغيو فاخر", 24.50, 2, 49.00, "", "2x Platters"),
            OrderItemEntity("oit_11", "ord_9088", "prd_02", "Citrus Garlic Shish Tawook", "شيش طاووق بالثوم والحمضيات", 19.90, 1, 19.90, "", "1x Tawook"),

            // Items for ord_9082
            OrderItemEntity("oit_12", "ord_9082", "prd_04", "Smoked Shawarma Truffle Brioche", "برجر شاورما بالكمأة وبريوش", 16.50, 2, 33.00, "", "2x Brioche")
        )
        orderItemDao().insertOrderItems(orderItems)
    }
}
