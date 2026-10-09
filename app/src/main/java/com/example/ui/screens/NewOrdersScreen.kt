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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg

@Composable
fun NewOrdersScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("new_orders_screen")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            .background(StatusAmberBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = StatusAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) "طابور الطلبات الجديدة" else "New Orders Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (isArabic) "${pendingOrders.size} طلبات تنتظر الموافقة" else "${pendingOrders.size} pending store response",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Button(
                    onClick = { viewModel.simulateIncomingOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("simulate_order_button_queue")
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "+ طلب جديد" else "+ Test Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (pendingOrders.isEmpty()) {
            EmptyStateView(
                title = if (isArabic) "لا توجد طلبات جديدة معلقة" else "No New Orders Waiting",
                description = if (isArabic) "جميع الطلبات الواردة تمت مراجعتها وقبولها أو رفضها. ستتلقى تنبيهاً فورياً عند وصول طلب جديد."
                              else "All incoming customer orders have been reviewed. New orders will appear here automatically with audio alert.",
                icon = Icons.Default.HourglassTop,
                actionTitle = if (isArabic) "محاكاة طلب جديد" else "Simulate Incoming Order",
                onAction = { viewModel.simulateIncomingOrder() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingOrders, key = { it.id }) { order ->
                    OrderSummaryCard(
                        order = order,
                        isArabic = isArabic,
                        onAccept = { viewModel.acceptOrder(order.id) },
                        onReject = { viewModel.rejectOrder(order.id) },
                        onClick = { viewModel.selectOrder(order.id) }
                    )
                }
            }
        }
    }
}
