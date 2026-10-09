package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.StoreStatus
import com.example.ui.StoreNavScreen
import com.example.ui.StoreViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.OrderSummaryCard
import com.example.ui.components.StoreStatusPill
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusCyanBg
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusEmeraldBg
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusIndigoBg
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: StoreViewModel,
    onNavigateToScreen: (StoreNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val currentStore by viewModel.currentStore.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }
    val activeOrders = orders.filter { it.status.isActive }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Operational Header Banner: Store Name + Status Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic && currentStore?.nameAr?.isNotBlank() == true) currentStore!!.nameAr else (currentStore?.name ?: "Store Dashboard"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentStore?.category ?: "Restaurant"} • ${currentStore?.openingHours ?: ""}",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        StoreStatusPill(
                            status = metrics.storeStatus,
                            isArabic = isArabic,
                            onStatusChange = { newStatus -> viewModel.updateStoreStatus(newStatus) },
                            modifier = Modifier.testTag("store_status_pill")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Simulation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, tint = MerchantBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isArabic) "محاكاة طلب جديد مباشر:" else "Incoming Orders Simulator:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        }

                        Button(
                            onClick = { viewModel.simulateIncomingOrder() },
                            modifier = Modifier.testTag("simulate_order_button"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MerchantBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isArabic) "إرسال طلب تجريبي" else "Send Test Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Today's Operational Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "المؤشرات التشغيلية لليوم" else "Today's Operational Metrics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                // Row 1: Today's Sales & New Orders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isArabic) "مبيعات اليوم" else "Today's Sales",
                        value = "$${String.format(Locale.US, "%.2f", metrics.todaySalesAmount)}",
                        subtitle = if (isArabic) "${metrics.completedOrdersCount} طلب مكتمل" else "${metrics.completedOrdersCount} completed",
                        icon = Icons.Default.AttachMoney,
                        color = StatusEmerald,
                        bgColor = StatusEmeraldBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(StoreNavScreen.COMPLETED_ORDERS) }
                    )

                    MetricCard(
                        title = if (isArabic) "طلبات جديدة" else "New Orders",
                        value = "${metrics.newOrdersCount}",
                        subtitle = if (metrics.newOrdersCount > 0) (if (isArabic) "تحتاج استجابة سريعة!" else "Needs response!") else null,
                        icon = Icons.Default.PendingActions,
                        color = StatusAmber,
                        bgColor = StatusAmberBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(StoreNavScreen.NEW_ORDERS) }
                    )
                }

                // Row 2: Kitchen Progress (Accepted, Preparing, Ready)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isArabic) "تم القبول" else "Accepted",
                        value = "${metrics.acceptedOrdersCount}",
                        subtitle = if (isArabic) "بانتظار التحضير" else "In Queue",
                        icon = Icons.Default.CheckCircle,
                        color = StatusCyan,
                        bgColor = StatusCyanBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(StoreNavScreen.ACTIVE_ORDERS) }
                    )

                    MetricCard(
                        title = if (isArabic) "قيد التحضير" else "Preparing",
                        value = "${metrics.preparingOrdersCount}",
                        subtitle = if (isArabic) "في المطبخ الآن" else "Cooking",
                        icon = Icons.Default.OutdoorGrill,
                        color = StatusIndigo,
                        bgColor = StatusIndigoBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(StoreNavScreen.ACTIVE_ORDERS) }
                    )

                    MetricCard(
                        title = if (isArabic) "جاهز للتسليم" else "Ready",
                        value = "${metrics.readyOrdersCount}",
                        subtitle = if (isArabic) "بانتظار السائق" else "Pickup",
                        icon = Icons.Default.DoneAll,
                        color = StatusEmerald,
                        bgColor = StatusEmeraldBg,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen(StoreNavScreen.ACTIVE_ORDERS) }
                    )
                }
            }
        }

        // Section: Urgent New Orders (Needs Accept/Reject)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isArabic) "الطلبات الجديدة الواردة" else "Incoming New Orders",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    if (pendingOrders.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = StatusAmber
                        ) {
                            Text(
                                text = "${pendingOrders.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (pendingOrders.isNotEmpty()) {
                    Text(
                        text = if (isArabic) "عرض الكل (${pendingOrders.size})" else "View All (${pendingOrders.size})",
                        color = MerchantBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToScreen(StoreNavScreen.NEW_ORDERS) }
                    )
                }
            }
        }

        if (pendingOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isArabic) "لا توجد طلبات جديدة معلقة حالياً 🎉" else "No pending orders right now 🎉",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "المطبخ يعمل بكفاءة تامة! سيظهر أي طلب جديد تلقائياً." else "All incoming orders have been processed! New orders will alert instantly.",
                            fontSize = 12.sp,
                            color = Slate500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingOrders.take(3)) { order ->
                OrderSummaryCard(
                    order = order,
                    isArabic = isArabic,
                    onAccept = { viewModel.acceptOrder(order.id) },
                    onReject = { viewModel.rejectOrder(order.id) },
                    onClick = { viewModel.selectOrder(order.id) }
                )
            }
        }

        // Section: Active Kitchen Orders
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الطلبات النشطة في المطبخ" else "Active Kitchen Orders",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = if (isArabic) "إدارة الطلبات النشطة" else "Manage Active",
                    color = MerchantBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToScreen(StoreNavScreen.ACTIVE_ORDERS) }
                )
            }
        }

        if (activeOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = if (isArabic) "لا توجد طلبات قيد التحضير حالياً." else "No active orders being cooked.",
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(activeOrders.take(4)) { order ->
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
