package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.StoreNavScreen
import com.example.ui.StoreViewModel
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductItemCard
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@Composable
fun ProductsScreen(
    viewModel: StoreViewModel,
    onNavigateToAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyAvailable by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Filtering logic
    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategoryFilter == null || product.categoryId == selectedCategoryFilter
        val matchesAvailability = !showOnlyAvailable || product.isAvailable
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.nameAr.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesAvailability && matchesSearch
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("products_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Title + Add Product Button + Search & Filters
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "إدارة قائمة المنتجات" else "Store Catalog & Menu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Slate900
                            )
                            Text(
                                text = if (isArabic) "${products.size} أصناف مسجلة بالمتجر" else "${products.size} total items in menu",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        Button(
                            onClick = onNavigateToAddProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_product_top_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "إضافة منتج" else "Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isArabic) "بحث بالاسم أو الوصف..." else "Search by name or description...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("product_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filters & Availability Filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text(if (isArabic) "الكل" else "All Categories", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MerchantBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        categories.forEach { cat ->
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == cat.id,
                                    onClick = { selectedCategoryFilter = cat.id },
                                    label = {
                                        Text(
                                            if (isArabic && cat.nameAr.isNotBlank()) cat.nameAr else cat.name,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }

                        item {
                            FilterChip(
                                selected = showOnlyAvailable,
                                onClick = { showOnlyAvailable = !showOnlyAvailable },
                                label = { Text(if (isArabic) "المتوفر فقط" else "Available Only", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    title = if (isArabic) "لم يتم العثور على منتجات" else "No Products Found",
                    description = if (isArabic) "جرّب تغيير كلمات البحث أو تصفية التصنيف، أو أضف صنفاً جديداً للمتجر."
                                  else "Try adjusting your search query, clearing filters, or adding a new menu item.",
                    icon = Icons.Default.Fastfood,
                    actionTitle = if (isArabic) "إضافة منتج جديد" else "Add New Product",
                    onAction = onNavigateToAddProduct
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            isArabic = isArabic,
                            onToggleAvailability = { isAvail ->
                                viewModel.toggleProductAvailability(product.id, isAvail)
                            },
                            onEdit = {
                                viewModel.startEditProduct(product.id)
                            },
                            onDelete = {
                                productToDelete = product
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val prod = productToDelete!!
        val name = if (isArabic && prod.nameAr.isNotBlank()) prod.nameAr else prod.name
        DeleteConfirmDialog(
            title = if (isArabic) "حذف الصنف؟" else "Delete Product?",
            message = if (isArabic) "هل أنت متأكد من رغبتك في حذف \"$name\"؟ لن يتمكن العملاء من طلبه بعد الآن."
                      else "Are you sure you want to permanently delete \"$name\"? Customers won't be able to order it.",
            isArabic = isArabic,
            onConfirm = {
                viewModel.deleteProduct(prod.id)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}
