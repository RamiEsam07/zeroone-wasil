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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.OrderStatus
import com.example.ui.StoreViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OrderSummaryCard
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusEmeraldBg
import java.util.Locale

@Composable
fun CompletedOrdersScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
    val filteredOrders = if (searchQuery.isBlank()) {
        completedOrders
    } else {
        completedOrders.filter {
            it.orderNumber.contains(searchQuery, ignoreCase = true) ||
            it.customerName.contains(searchQuery, ignoreCase = true) ||
            it.deliveryAddress.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalCompletedRevenue = completedOrders.sumOf { it.total }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("completed_orders_screen")
    ) {
        // Operational Header
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StatusEmeraldBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isArabic) "سجل الطلبات المكتملة" else "Completed Orders History",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate900
                            )
                            Text(
                                text = if (isArabic) "${completedOrders.size} طلبات تم تسليمها بنجاح" else "${completedOrders.size} delivered successfully",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isArabic) "إجمالي المبيعات" else "Total Revenue",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalCompletedRevenue)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isArabic) "بحث برقم الطلب أو اسم العميل..." else "Search order # or customer...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        if (filteredOrders.isEmpty()) {
            EmptyStateView(
                title = if (isArabic) "لا توجد طلبات مكتملة" else "No Completed Orders",
                description = if (isArabic) "ستظهر هنا الطلبات بعد تسليمها وإتمام العملية بنجاح."
                              else "Completed and delivered orders will appear in this history log.",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderSummaryCard(
                        order = order,
                        isArabic = isArabic,
                        onClick = { viewModel.selectOrder(order.id) }
                    )
                }
            }
        }
    }
}
