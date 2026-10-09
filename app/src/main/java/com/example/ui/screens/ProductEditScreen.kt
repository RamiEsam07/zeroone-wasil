package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StoreViewModel
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusEmerald

@Composable
fun ProductEditScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val editingProductId by viewModel.editingProductId.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val isEditing = editingProductId != null
    val existingProduct = products.find { it.id == editingProductId }

    var name by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var prepMinutesText by remember { mutableStateOf("15") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingProduct) {
        if (existingProduct != null) {
            name = existingProduct.name
            nameAr = existingProduct.nameAr
            description = existingProduct.description
            priceText = existingProduct.price.toString()
            selectedCategoryId = existingProduct.categoryId
            image = existingProduct.image
            isAvailable = existingProduct.isAvailable
            prepMinutesText = existingProduct.preparationTimeMinutes.toString()
        } else if (categories.isNotEmpty() && selectedCategoryId.isEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("product_edit_screen")
    ) {
        // App bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("product_edit_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }
                    Text(
                        text = if (isEditing) {
                            if (isArabic) "تعديل بيانات المنتج" else "Edit Product"
                        } else {
                            if (isArabic) "إضافة صنف جديد" else "Add New Item"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                }
            }
        }

        // Form Fields
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "المعلومات الأساسية" else "Basic Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // English Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isArabic) "اسم المنتج (بالإنجليزية)" else "Product Name (English)") },
                        leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arabic Name
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم المنتج (بالعربية)" else "Product Name (Arabic)") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_ar_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (isArabic) "وصف الصنف والمكونات" else "Item Description & Ingredients") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("product_desc_input"),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // Category & Pricing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "التصنيف والسعر والتوقيت" else "Category & Pricing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name
                        ?: if (isArabic) "اختر التصنيف" else "Select Category"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isArabic) "تصنيف القائمة" else "Menu Category") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Slate500) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = true }
                                .testTag("product_category_dropdown"),
                            enabled = false,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Slate900,
                                disabledBorderColor = Slate200,
                                disabledLabelColor = Slate700
                            )
                        )

                        // Transparent overlay to catch tap
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { categoryDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(if (isArabic && cat.nameAr.isNotBlank()) cat.nameAr else cat.name)
                                    },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Price
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text(if (isArabic) "السعر ($)" else "Price ($)") },
                            leadingIcon = { Icon(Icons.Default.PriceCheck, contentDescription = null, tint = Slate500) },
                            modifier = Modifier.weight(1f).testTag("product_price_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        // Prep Time Minutes
                        OutlinedTextField(
                            value = prepMinutesText,
                            onValueChange = { prepMinutesText = it },
                            label = { Text(if (isArabic) "دقائق التحضير" else "Prep (min)") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate500) },
                            modifier = Modifier.weight(1f).testTag("product_prep_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Availability Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "حالة توفر الصنف في المتجر" else "Item Availability Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate900
                            )
                            Text(
                                text = if (isAvailable) {
                                    if (isArabic) "متاح للطلب من قبل العملاء" else "Ready to be ordered by customers"
                                } else {
                                    if (isArabic) "مخفي أو غير متوفر مؤقتاً" else "Hidden or temporarily out of stock"
                                },
                                fontSize = 11.sp,
                                color = if (isAvailable) StatusEmerald else Slate500
                            )
                        }

                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it },
                            modifier = Modifier.testTag("product_avail_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusEmerald
                            )
                        )
                    }
                }
            }

            if (validationError != null) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Bottom Bar Action
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }

                Button(
                    onClick = {
                        val parsedPrice = priceText.toDoubleOrNull()
                        val parsedPrep = prepMinutesText.toIntOrNull() ?: 15

                        if (name.isBlank()) {
                            validationError = if (isArabic) "يرجى كتابة اسم المنتج" else "Please enter product name"
                            return@Button
                        }
                        if (parsedPrice == null || parsedPrice <= 0.0) {
                            validationError = if (isArabic) "يرجى إدخال سعر صحيح أكبر من صفر" else "Please enter a valid price"
                            return@Button
                        }
                        if (selectedCategoryId.isBlank() && categories.isNotEmpty()) {
                            selectedCategoryId = categories.first().id
                        }

                        validationError = null
                        viewModel.saveProduct(
                            name = name,
                            nameAr = nameAr.ifBlank { name },
                            description = description,
                            price = parsedPrice,
                            categoryId = selectedCategoryId,
                            image = image,
                            isAvailable = isAvailable,
                            prepMinutes = parsedPrep
                        )
                    },
                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("product_save_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "حفظ التغييرات" else "Save Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
