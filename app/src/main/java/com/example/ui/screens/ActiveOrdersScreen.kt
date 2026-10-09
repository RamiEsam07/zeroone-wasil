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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusIndigoBg

@Composable
fun ActiveOrdersScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var activeSubFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val activeOrders = orders.filter { it.status.isActive }
    val filteredOrders = if (activeSubFilter != null) {
        activeOrders.filter { it.status == activeSubFilter }
    } else {
        activeOrders
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_orders_screen")
    ) {
        // Operational Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = null,
                            tint = StatusIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) "طابور المطبخ والطلبات النشطة" else "Active Kitchen & Preparation Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (isArabic) "${activeOrders.size} طلبات قيد الإعداد أو جاهزة" else "${activeOrders.size} orders currently in progress",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stage Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = activeSubFilter == null,
                            onClick = { activeSubFilter = null },
                            label = {
                                Text(
                                    if (isArabic) "الكل (${activeOrders.size})" else "All Active (${activeOrders.size})",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MerchantBlue,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                    item {
                        val count = activeOrders.count { it.status == OrderStatus.ACCEPTED }
                        FilterChip(
                            selected = activeSubFilter == OrderStatus.ACCEPTED,
                            onClick = { activeSubFilter = OrderStatus.ACCEPTED },
                            label = {
                                Text(
                                    if (isArabic) "مقبول ($count)" else "Accepted ($count)",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                    item {
                        val count = activeOrders.count { it.status == OrderStatus.PREPARING }
                        FilterChip(
                            selected = activeSubFilter == OrderStatus.PREPARING,
                            onClick = { activeSubFilter = OrderStatus.PREPARING },
                            label = {
                                Text(
                                    if (isArabic) "قيد التحضير ($count)" else "Cooking ($count)",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                    item {
                        val count = activeOrders.count { it.status == OrderStatus.READY }
                        FilterChip(
                            selected = activeSubFilter == OrderStatus.READY,
                            onClick = { activeSubFilter = OrderStatus.READY },
                            label = {
                                Text(
                                    if (isArabic) "جاهز للتسليم ($count)" else "Ready for Pickup ($count)",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            EmptyStateView(
                title = if (isArabic) "لا توجد طلبات في هذه المرحلة" else "No Orders In This Stage",
                description = if (isArabic) "يمكنك مراجعة الطلبات الجديدة وقبولها للبدء بالتحضير."
                              else "New orders can be accepted from the New Orders tab to enter the preparation queue.",
                icon = Icons.Default.Kitchen
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
                        onNextStatus = {
                            when (order.status) {
                                OrderStatus.ACCEPTED -> viewModel.markOrderPreparing(order.id)
                                OrderStatus.PREPARING -> viewModel.markOrderReady(order.id)
                                OrderStatus.READY -> viewModel.markOrderCompleted(order.id)
                                else -> {}
                            }
                        },
                        onClick = { viewModel.selectOrder(order.id) }
                    )
                }
            }
        }
    }
}
