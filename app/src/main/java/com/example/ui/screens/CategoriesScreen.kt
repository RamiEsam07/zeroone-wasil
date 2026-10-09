package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Category
import com.example.ui.StoreViewModel
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusIndigoBg
import com.example.ui.theme.StatusRose

@Composable
fun CategoriesScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("categories_screen")
    ) {
        // Operational Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StatusIndigoBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = StatusIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) "تصنيفات القائمة" else "Menu Categories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (isArabic) "${categories.size} تصنيفات نشطة" else "${categories.size} active categories",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_category_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "إضافة تصنيف" else "Add Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (categories.isEmpty()) {
            EmptyStateView(
                title = if (isArabic) "لا توجد تصنيفات" else "No Categories Found",
                description = if (isArabic) "أضف تصنيفات لتنظيم أصناف المتجر (مثل: مشاوي، برجر، مشروبات)."
                              else "Add categories to organize your menu items for customers and kitchen staff.",
                icon = Icons.Default.Category,
                actionTitle = if (isArabic) "إضافة تصنيف جديد" else "Add Category",
                onAction = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("category_card_${cat.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic && cat.nameAr.isNotBlank()) cat.nameAr else cat.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                if (cat.nameAr.isNotBlank()) {
                                    Text(
                                        text = if (isArabic) cat.name else cat.nameAr,
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { categoryToEdit = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = Slate700,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { categoryToDelete = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Category",
                                        tint = StatusRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || categoryToEdit != null) {
        var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
        var nameAr by remember { mutableStateOf(categoryToEdit?.nameAr ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                categoryToEdit = null
            },
            title = {
                Text(
                    text = if (categoryToEdit != null) {
                        if (isArabic) "تعديل التصنيف" else "Edit Category"
                    } else {
                        if (isArabic) "إضافة تصنيف جديد" else "Add New Category"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isArabic) "اسم التصنيف (إنجليزي)" else "Category Name (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم التصنيف (عربي)" else "Category Name (Arabic)") },
                        modifier = Modifier.fillMaxWidth().testTag("cat_name_ar_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (categoryToEdit != null) {
                                viewModel.updateCategory(categoryToEdit!!.copy(name = name, nameAr = nameAr))
                            } else {
                                viewModel.addCategory(name, nameAr)
                            }
                            showAddDialog = false
                            categoryToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue)
                ) {
                    Text(if (isArabic) "حفظ" else "Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        categoryToEdit = null
                    }
                ) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Delete Confirmation
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        DeleteConfirmDialog(
            title = if (isArabic) "حذف التصنيف؟" else "Delete Category?",
            message = if (isArabic) "هل أنت متأكد من حذف تصنيف \"${cat.name}\"؟"
                      else "Are you sure you want to delete \"${cat.name}\"?",
            isArabic = isArabic,
            onConfirm = {
                viewModel.deleteCategory(cat.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}
