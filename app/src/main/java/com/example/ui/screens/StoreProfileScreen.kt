package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.StatusAmber

@Composable
fun StoreProfileScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val store by viewModel.currentStore.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var name by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf("") }
    var minOrderText by remember { mutableStateOf("") }
    var deliveryFeeText by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(store) {
        if (store != null) {
            name = store!!.name
            nameAr = store!!.nameAr
            description = store!!.description
            category = store!!.category
            phone = store!!.phone
            address = store!!.address
            openingHours = store!!.openingHours
            minOrderText = store!!.minimumOrder.toString()
            deliveryFeeText = store!!.deliveryFee.toString()
            latitudeText = store!!.latitude?.toString().orEmpty()
            longitudeText = store!!.longitude?.toString().orEmpty()
            locationError = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("store_profile_screen")
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MerchantBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = MerchantBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "الملف التعريفي للمتجر" else "Store Profile & Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                    Text(
                        text = if (isArabic) "بيانات المتجر الظاهرة في تطبيق العملاء وسائقي التوصيل"
                               else "Store information visible to customers and delivery drivers",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        }

        // Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rating and Stats Overview
            if (store != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                            Text("${store!!.rating} / 5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("•", color = Slate500)
                        Text(
                            text = if (isArabic) "${store!!.totalOrdersCount} طلب منفذ" else "${store!!.totalOrdersCount} Lifetime Orders",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Slate700
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "معلومات المتجر" else "Store Identity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (isArabic) "اسم المتجر (إنجليزي)" else "Store Name (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم المتجر (عربي)" else "Store Name (Arabic)") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_ar_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(if (isArabic) "نوع المطبخ / التصنيف" else "Cuisine / Category") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_category_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (isArabic) "نبذة عن المتجر" else "Description") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_desc_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "التواصل والموقع وساعات العمل" else "Contact & Logistics",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isArabic) "هاتف المتجر" else "Store Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isArabic) "العنوان الفعلي" else "Physical Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_address_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = latitudeText,
                            onValueChange = { latitudeText = it; locationError = null },
                            label = { Text(if (isArabic) "خط العرض (Latitude)" else "Latitude") },
                            modifier = Modifier.weight(1f).testTag("profile_latitude_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = longitudeText,
                            onValueChange = { longitudeText = it; locationError = null },
                            label = { Text(if (isArabic) "خط الطول (Longitude)" else "Longitude") },
                            modifier = Modifier.weight(1f).testTag("profile_longitude_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                    Text(
                        text = if (isArabic) "الإحداثيات مطلوبة للإسناد التلقائي حسب القرب." else "Coordinates are required for nearest-driver auto-assignment.",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    locationError?.let { error ->
                        Text(error, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = openingHours,
                        onValueChange = { openingHours = it },
                        label = { Text(if (isArabic) "ساعات العمل" else "Opening Hours") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate500) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_hours_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minOrderText,
                            onValueChange = { minOrderText = it },
                            label = { Text(if (isArabic) "الحد الأدنى للطلب (ج.س)" else "Min Order (SDG)") },
                            modifier = Modifier.weight(1f).testTag("profile_min_order_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = deliveryFeeText,
                            onValueChange = { deliveryFeeText = it },
                            label = { Text(if (isArabic) "رسوم التوصيل (ج.س)" else "Delivery Fee (SDG)") },
                            modifier = Modifier.weight(1f).testTag("profile_fee_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Save Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (store != null) {
                            val latitudeRaw = latitudeText.trim()
                            val longitudeRaw = longitudeText.trim()
                            val latitude = latitudeRaw.toDoubleOrNull()
                            val longitude = longitudeRaw.toDoubleOrNull()
                            if (latitudeRaw.isBlank() != longitudeRaw.isBlank()) {
                                locationError = if (isArabic) "أدخل خط العرض والطول معاً، أو اتركهما فارغين." else "Enter both coordinates or leave both blank."
                                return@Button
                            }
                            if (latitudeRaw.isNotBlank() &&
                                (latitude == null || longitude == null ||
                                    latitude !in -90.0..90.0 || longitude !in -180.0..180.0)
                            ) {
                                locationError = if (isArabic) "الإحداثيات غير صحيحة." else "Coordinates are invalid."
                                return@Button
                            }
                            locationError = null
                            val minOrder = minOrderText.toDoubleOrNull() ?: store!!.minimumOrder
                            val deliveryFee = deliveryFeeText.toDoubleOrNull() ?: store!!.deliveryFee
                            if (minOrder < 0 || deliveryFee < 0) {
                                locationError = if (isArabic) "لا يمكن أن تكون الرسوم أو قيمة الحد الأدنى سالبة." else "Fees and minimum order must be non-negative."
                                return@Button
                            }
                            val updated = store!!.copy(
                                name = name,
                                nameAr = nameAr,
                                description = description,
                                category = category,
                                phone = phone,
                                address = address,
                                openingHours = openingHours,
                                minimumOrder = minOrder,
                                deliveryFee = deliveryFee,
                                latitude = latitude,
                                longitude = longitude
                            )
                            viewModel.updateStoreSettings(updated)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "حفظ بيانات المتجر" else "Save Store Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
