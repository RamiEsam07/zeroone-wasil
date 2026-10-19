package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.StoreNavScreen
import com.example.ui.StoreViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailsScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedOrderId by viewModel.selectedOrderId.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    val order = orders.find { it.id == selectedOrderId }

    if (order == null) {
        EmptyStateView(
            title = if (isArabic) "الطلب غير موجود" else "Order Not Found",
            description = if (isArabic) "يرجى اختيار طلب من القائمة" else "Please select an order from the list",
            actionTitle = if (isArabic) "العودة للوحة التحكم" else "Back to Dashboard",
            onAction = onBack
        )
        return
    }

    val timeFormatted = remember(order.createdAt) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(order.createdAt))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("order_details_screen")
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
                        modifier = Modifier.testTag("order_details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }
                    Column {
                        Text(
                            text = "#${order.orderNumber}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Slate900
                        )
                        Text(
                            text = timeFormatted,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderStatusBadge(status = order.status, isArabic = isArabic)
                    IconButton(
                        onClick = {
                            viewModel.showMessage(
                                if (isArabic) "تم إرسال الإيصال إلى طابعة المطبخ 🖨️"
                                else "Order receipt sent to kitchen printer 🖨️"
                            )
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print Receipt", tint = Slate700)
                    }
                }
            }
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer & Delivery Address Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "بيانات العميل والتوصيل" else "Customer & Delivery Info",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MerchantBlue, modifier = Modifier.size(18.dp))
                        Text(
                            text = order.customerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                        Text(
                            text = order.customerPhone,
                            fontSize = 13.sp,
                            color = Slate700
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusRose, modifier = Modifier.size(18.dp))
                        Text(
                            text = order.deliveryAddress,
                            fontSize = 13.sp,
                            color = Slate700
                        )
                    }

                    // Special Notes
                    if (order.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "ملاحظات العميل:" else "Customer Notes:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = order.notes,
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Driver Card (if assigned)
            if (order.driverName != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color(0xFF0284C7))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "سائق التوصيل المخصص" else "Assigned Delivery Driver",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Text(
                                text = order.driverName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            if (order.driverPhone != null) {
                                Text(
                                    text = order.driverPhone,
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                }
            }

            // Products & Quantities Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "تفاصيل الأصناف (${order.totalItemCount})" else "Ordered Items (${order.totalItemCount})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    order.items.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${item.quantity}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MerchantBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isArabic && item.productNameAr.isNotBlank()) item.productNameAr else item.productName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Slate900
                                    )
                                    if (item.specialInstructions.isNotBlank()) {
                                        Text(
                                            text = item.specialInstructions,
                                            fontSize = 11.sp,
                                            color = StatusAmber,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "SDG ${String.format(Locale.US, "%.2f", item.unitPrice)} each",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            Text(
                                text = "SDG ${String.format(Locale.US, "%.2f", item.totalPrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        if (idx < order.items.size - 1) {
                            HorizontalDivider(color = Slate100)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtotal, Delivery fee, Tax, Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "المجموع الفرعي" else "Subtotal", fontSize = 13.sp, color = Slate500)
                        Text("SDG ${String.format(Locale.US, "%.2f", order.subtotal)}", fontSize = 13.sp, color = Slate700)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "رسوم التوصيل" else "Delivery Fee", fontSize = 13.sp, color = Slate500)
                        Text("SDG ${String.format(Locale.US, "%.2f", order.deliveryFee)}", fontSize = 13.sp, color = Slate700)
                    }

                    if (order.serviceFee > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "رسوم الخدمة" else "Service Fee", fontSize = 13.sp, color = Slate500)
                            Text("SDG ${String.format(Locale.US, "%.2f", order.serviceFee)}", fontSize = 13.sp, color = Slate700)
                        }
                    }

                    if (order.tax > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isArabic) "الضريبة المقدرة" else "Estimated Tax", fontSize = 13.sp, color = Slate500)
                            Text("SDG ${String.format(Locale.US, "%.2f", order.tax)}", fontSize = 13.sp, color = Slate700)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "الإجمالي الكلي" else "Total Amount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "${order.paymentMethod.getDisplayName(isArabic)} • ${order.paymentStatus.name}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Text(
                            text = "SDG ${String.format(Locale.US, "%.2f", order.total)}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MerchantBlue
                        )
                    }
                }
            }
        }

        // Operational Actions Bottom Bar (State Machine Enforcement)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                when (order.status) {
                    OrderStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rejectOrder(order.id) },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("details_reject_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRose),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "رفض الطلب" else "Reject", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.acceptOrder(order.id) },
                                modifier = Modifier.weight(1.5f).height(48.dp).testTag("details_accept_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "قبول الطلب" else "Accept Order", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OrderStatus.ACCEPTED -> {
                        Button(
                            onClick = { viewModel.markOrderPreparing(order.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("details_start_prep_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusIndigo),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "بدء التحضير في المطبخ" else "Start Cooking in Kitchen", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { viewModel.markOrderReady(order.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("details_mark_ready_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "الطلب جاهز للتسليم 📦" else "Mark Ready for Pickup 📦", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.READY -> {
                        Button(
                            onClick = { viewModel.markOrderCompleted(order.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("details_complete_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "تسليم الطلب واكتماله ✅" else "Complete & Hand Off to Driver ✅", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.COMPLETED -> {
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isArabic) "هذا الطلب مكتمل وتم تسليمه بنجاح ✅" else "This order is completed and delivered ✅",
                                color = StatusEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    OrderStatus.REJECTED, OrderStatus.CANCELLED -> {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isArabic) "تم رفض هذا الطلب أو إلغاؤه" else "This order was rejected or cancelled",
                                color = StatusRose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
