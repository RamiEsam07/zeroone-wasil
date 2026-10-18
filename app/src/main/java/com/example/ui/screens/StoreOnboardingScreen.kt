package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.ui.StoreViewModel
import kotlinx.coroutines.launch

@Composable
fun StoreOnboardingScreen(
    viewModel: StoreViewModel,
    onStoreCreated: () -> Unit,
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val scope = rememberCoroutineScope()
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var loadingCategories by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        loadingCategories = true
        viewModel.loadStoreCategories()
            .onSuccess { categories = it }
            .onFailure { error = it.message ?: "تعذر تحميل تصنيفات المتاجر." }
        loadingCategories = false
    }

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val title = if (isArabic) "إنشاء متجر وصّل" else "Create your WASIL store"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (isArabic) {
                    "أكمل بيانات المتجر. سيظل المتجر والمنتجات غير ظاهرين للعملاء حتى تعتمدها الإدارة."
                } else {
                    "Complete your store details. The store and products stay hidden from customers until admin approval."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = nameAr,
                onValueChange = { nameAr = it; error = null },
                label = { Text(if (isArabic) "اسم المتجر بالعربية" else "Store name in Arabic") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = nameEn,
                onValueChange = { nameEn = it; error = null },
                label = { Text(if (isArabic) "اسم المتجر بالإنجليزية" else "Store name in English") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))

            Column {
                OutlinedTextField(
                    value = selectedCategory?.let { if (isArabic) it.nameAr else it.name }
                        .orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isArabic) "تصنيف المتجر" else "Store category") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryMenuOpen = true },
                    singleLine = true,
                )
                DropdownMenu(
                    expanded = categoryMenuOpen,
                    onDismissRequest = { categoryMenuOpen = false },
                    modifier = Modifier.fillMaxWidth(0.85f),
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(if (isArabic) category.nameAr else category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryMenuOpen = false
                                error = null
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; error = null },
                label = { Text(if (isArabic) "رقم هاتف المتجر" else "Store phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; error = null },
                label = { Text(if (isArabic) "العنوان بالتفصيل" else "Physical address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = latitudeText,
                onValueChange = { latitudeText = it; error = null },
                label = { Text(if (isArabic) "خط العرض (اختياري)" else "Latitude (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = longitudeText,
                onValueChange = { longitudeText = it; error = null },
                label = { Text(if (isArabic) "خط الطول (اختياري)" else "Longitude (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (isArabic) "أدخل الإحداثيات معاً لتفعيل الإسناد التلقائي حسب القرب." else "Enter both coordinates to enable nearest-driver auto-assignment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    val latRaw = latitudeText.trim()
                    val lonRaw = longitudeText.trim()
                    val latitude = latRaw.toDoubleOrNull()
                    val longitude = lonRaw.toDoubleOrNull()
                    if (nameAr.isBlank() || nameEn.isBlank() || phone.isBlank() ||
                        address.isBlank() || selectedCategoryId.isBlank()
                    ) {
                        error = if (isArabic) "أكمل بيانات المتجر المطلوبة." else "Complete all required store fields."
                        return@Button
                    }
                    if (latRaw.isBlank() != lonRaw.isBlank()) {
                        error = if (isArabic) "أدخل خط العرض والطول معاً، أو اتركهما فارغين." else "Enter both coordinates or leave both blank."
                        return@Button
                    }
                    if (latRaw.isNotBlank() &&
                        (latitude == null || longitude == null ||
                            latitude !in -90.0..90.0 || longitude !in -180.0..180.0)
                    ) {
                        error = if (isArabic) "الإحداثيات غير صحيحة." else "Coordinates are invalid."
                        return@Button
                    }
                    scope.launch {
                        submitting = true
                        error = null
                        val result = viewModel.createStoreForMerchant(
                            nameAr = nameAr.trim(),
                            nameEn = nameEn.trim(),
                            categoryId = selectedCategoryId,
                            phone = phone.trim(),
                            address = address.trim(),
                            latitude = latitude,
                            longitude = longitude,
                        )
                        result.onSuccess { onStoreCreated() }
                            .onFailure { error = it.message ?: "تعذر إنشاء المتجر." }
                        submitting = false
                    }
                },
                enabled = !submitting && !loadingCategories && categories.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (submitting || loadingCategories) {
                    CircularProgressIndicator()
                } else {
                    Text(if (isArabic) "إرسال المتجر للاعتماد" else "Submit store for approval")
                }
            }
        }
    }
}
