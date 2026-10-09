package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.StoreStatus
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusCyanBg
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusEmeraldBg
import com.example.ui.theme.StatusIndigo
import com.example.ui.theme.StatusIndigoBg
import com.example.ui.theme.StatusRose
import com.example.ui.theme.StatusRoseBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        OrderStatus.PENDING -> Triple(StatusAmberBg, StatusAmber, StatusAmber)
        OrderStatus.ACCEPTED -> Triple(StatusCyanBg, StatusCyan, StatusCyan)
        OrderStatus.PREPARING -> Triple(StatusIndigoBg, StatusIndigo, StatusIndigo)
        OrderStatus.READY -> Triple(StatusEmeraldBg, StatusEmerald, StatusEmerald)
        OrderStatus.COMPLETED -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFF64748B))
        OrderStatus.REJECTED -> Triple(StatusRoseBg, StatusRose, StatusRose)
        OrderStatus.CANCELLED -> Triple(StatusRoseBg, StatusRose, StatusRose)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by if (status == OrderStatus.PENDING) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alphaAnim)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = status.getDisplayName(isArabic),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StoreStatusPill(
    status: StoreStatus,
    isArabic: Boolean,
    onStatusChange: (StoreStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val (pillBg, pillTextColor, pillDotColor) = when (status) {
        StoreStatus.OPEN -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF22C55E))
        StoreStatus.BUSY -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFF59E0B))
        StoreStatus.CLOSED -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Color(0xFFEF4444))
    }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .border(1.dp, pillDotColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            color = pillBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(pillDotColor, CircleShape)
                )
                Text(
                    text = status.getDisplayName(isArabic),
                    color = pillTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Change Status",
                    tint = pillTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            StoreStatus.entries.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dot = when (opt) {
                                StoreStatus.OPEN -> Color(0xFF22C55E)
                                StoreStatus.BUSY -> Color(0xFFF59E0B)
                                StoreStatus.CLOSED -> Color(0xFFEF4444)
                            }
                            Box(modifier = Modifier.size(8.dp).background(dot, CircleShape))
                            Text(opt.getDisplayName(isArabic), fontWeight = FontWeight.SemiBold)
                        }
                    },
                    onClick = {
                        onStatusChange(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    order: Order,
    isArabic: Boolean,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onNextStatus: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(order.createdAt) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(order.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (order.status == OrderStatus.PENDING) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (order.status == OrderStatus.PENDING) Color(0xFFFDE68A) else Slate200
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Number + Status Badge + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${order.orderNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    OrderStatusBadge(status = order.status, isArabic = isArabic)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Slate500
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 12.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Text(
                        text = if (isArabic) "${order.totalItemCount} أصناف" else "${order.totalItemCount} items",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
                Text(
                    text = "$${String.format(Locale.US, "%.2f", order.total)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MerchantBlue
                )
            }

            // Preview items line
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val itemsSummary = order.items.joinToString(", ") { item ->
                    val name = if (isArabic && item.productNameAr.isNotBlank()) item.productNameAr else item.productName
                    "${item.quantity}x $name"
                }
                Text(
                    text = itemsSummary,
                    fontSize = 12.sp,
                    color = Slate700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Actions based on status
            if (order.status == OrderStatus.PENDING && (onAccept != null || onReject != null)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject?.invoke() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_order_${order.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRose),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "رفض" else "Reject", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAccept?.invoke() },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("accept_order_${order.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "قبول الطلب" else "Accept Order", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (order.status.isActive && onNextStatus != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val (actionTitle, buttonColor) = when (order.status) {
                    OrderStatus.ACCEPTED -> Pair(
                        if (isArabic) "بدء التحضير 👨‍🍳" else "Start Preparing 👨‍🍳",
                        StatusIndigo
                    )
                    OrderStatus.PREPARING -> Pair(
                        if (isArabic) "تحديد كجاهز للتسليم 📦" else "Mark Ready for Pickup 📦",
                        StatusEmerald
                    )
                    OrderStatus.READY -> Pair(
                        if (isArabic) "اكتمل الطلب والتسليم ✅" else "Complete Order & Hand Off ✅",
                        MerchantBlue
                    )
                    else -> Pair("", MerchantBlue)
                }

                Button(
                    onClick = { onNextStatus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_order_${order.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    isArabic: Boolean,
    onToggleAvailability: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Product details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic && product.nameAr.isNotBlank()) product.nameAr else product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.categoryName,
                        fontSize = 11.sp,
                        color = MerchantBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        fontSize = 12.sp,
                        color = Slate500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price and prep time badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(10.dp), tint = Slate500)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${product.preparationTimeMinutes} min",
                                fontSize = 10.sp,
                                color = Slate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Availability switch + Edit + Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Availability toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = { onToggleAvailability(it) },
                        modifier = Modifier.testTag("toggle_avail_${product.id}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusEmerald
                        )
                    )
                    Text(
                        text = if (product.isAvailable) {
                            if (isArabic) "متوفر" else "Available"
                        } else {
                            if (isArabic) "غير متوفر" else "Unavailable"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (product.isAvailable) StatusEmerald else Slate500
                    )
                }

                // Edit & Delete icons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = StatusRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.RestaurantMenu,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Slate100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionTitle != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(onClick = onAction) {
                Text(actionTitle, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    isArabic: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRose)
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = { Text(message, color = Slate700) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRose)
            ) {
                Text(if (isArabic) "حذف" else "Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
