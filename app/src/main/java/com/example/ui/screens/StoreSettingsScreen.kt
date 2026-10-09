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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.StoreStatus
import com.example.ui.StoreViewModel
import com.example.ui.components.StoreStatusPill
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusRose

@Composable
fun StoreSettingsScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val store by viewModel.currentStore.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("store_settings_screen")
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MerchantBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "إعدادات المتجر والعمليات" else "Store Operations & Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                    Text(
                        text = if (isArabic) "التحكم في تنبيهات الطلبات واللغة وصلاحيات المستخدم"
                               else "Configure order alerts, RTL language, and user sessions",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        }

        // Settings Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Store Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "حالة استقبال الطلبات في المتجر" else "Store Availability & Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StoreStatus.entries.forEach { st ->
                            val isSelected = store?.status == st
                            val (btnBg, btnText) = when (st) {
                                StoreStatus.OPEN -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
                                StoreStatus.BUSY -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
                                StoreStatus.CLOSED -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateStoreStatus(st) }
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) btnText else Slate200,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .testTag("status_button_${st.name}"),
                                color = if (isSelected) btnBg else Slate100
                            ) {
                                Text(
                                    text = st.getDisplayName(isArabic),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) btnText else Slate700,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Operations Automation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "أتمتة العمليات والتنبيهات" else "Operations & Alerts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sound alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MerchantBlue, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "تنبيهات صوتية واهتزاز للطلبات" else "Sound & Haptic Alerts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = if (isArabic) "إصدار رنين عند استلام طلب جديد" else "Alert loudly when new customer order arrives",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Switch(
                            checked = store?.soundAlertsEnabled ?: true,
                            onCheckedChange = {
                                if (store != null) {
                                    viewModel.updateStoreSettings(store!!.copy(soundAlertsEnabled = it))
                                }
                            },
                            modifier = Modifier.testTag("sound_alerts_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MerchantBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-accept toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "قبول الطلبات تلقائياً" else "Auto-Accept Orders",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = if (isArabic) "إرسال الطلبات إلى المطبخ مباشرة" else "Directly transition new orders into cooking queue",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Switch(
                            checked = store?.autoAcceptOrders ?: false,
                            onCheckedChange = {
                                if (store != null) {
                                    viewModel.updateStoreSettings(store!!.copy(autoAcceptOrders = it))
                                }
                            },
                            modifier = Modifier.testTag("auto_accept_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusEmerald
                            )
                        )
                    }
                }
            }

            // Language & RTL Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "لغة الواجهة ودعم RTL" else "Interface Language & Direction",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MerchantBlue, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isArabic) "العربية (RTL)" else "English (LTR)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleLanguage() },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("language_switch_btn")
                        ) {
                            Text(
                                text = if (isArabic) "Switch to English" else "التبديل إلى العربية",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Authenticated User Session Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "حساب المستخدم النشط" else "Active Operator Session",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Slate100, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Slate700)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.name ?: "Store Staff",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = "${user?.email ?: ""} • Role: ${user?.role?.name ?: "STORE_OWNER"}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth().testTag("logout_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRose),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "تسجيل الخروج والتبديل" else "Sign Out / Switch Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
