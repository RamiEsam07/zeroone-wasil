package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.ui.components.StoreStatusPill
import com.example.ui.screens.ActiveOrdersScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CompletedOrdersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NewOrdersScreen
import com.example.ui.screens.OrderDetailsScreen
import com.example.ui.screens.ProductEditScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.StoreLoginScreen
import com.example.ui.screens.StoreProfileScreen
import com.example.ui.screens.StoreSettingsScreen
import com.example.ui.theme.MerchantBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber

data class NavItem(
    val screen: StoreNavScreen,
    val titleEn: String,
    val titleAr: String,
    val icon: ImageVector,
    val hasBadge: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePortalApp(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier,
    showSimulationControls: Boolean = true,
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentStore by viewModel.currentStore.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        if (snackbarMsg != null) {
            snackbarHostState.showSnackbar(snackbarMsg!!)
            viewModel.clearSnackbar()
        }
    }

    // Direction Provider for natural Arabic RTL support
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (currentScreen == StoreNavScreen.LOGIN || currentUser == null) {
            StoreLoginScreen(viewModel = viewModel, modifier = modifier)
            return@CompositionLocalProvider
        }

        // BackHandler for secondary screens
        BackHandler(enabled = currentScreen != StoreNavScreen.DASHBOARD) {
            when (currentScreen) {
                StoreNavScreen.ORDER_DETAILS -> viewModel.navigateTo(StoreNavScreen.DASHBOARD)
                StoreNavScreen.ADD_PRODUCT, StoreNavScreen.EDIT_PRODUCT -> viewModel.navigateTo(StoreNavScreen.PRODUCTS)
                else -> viewModel.navigateTo(StoreNavScreen.DASHBOARD)
            }
        }

        val primaryNavItems = listOf(
            NavItem(StoreNavScreen.DASHBOARD, "Dashboard", "الرئيسية", Icons.Default.Dashboard),
            NavItem(StoreNavScreen.NEW_ORDERS, "New Orders", "جديدة", Icons.Default.NotificationsActive, hasBadge = true),
            NavItem(StoreNavScreen.ACTIVE_ORDERS, "Kitchen", "المطبخ", Icons.Default.Kitchen),
            NavItem(StoreNavScreen.PRODUCTS, "Catalog", "المنتجات", Icons.Default.Fastfood),
            NavItem(StoreNavScreen.STORE_SETTINGS, "Settings", "الإعدادات", Icons.Default.Settings)
        )

        val moreNavItems = listOf(
            NavItem(StoreNavScreen.COMPLETED_ORDERS, "Completed", "مكتملة", Icons.Default.CheckCircle),
            NavItem(StoreNavScreen.CATEGORIES, "Categories", "التصنيفات", Icons.Default.Category),
            NavItem(StoreNavScreen.STORE_PROFILE, "Profile", "الملف", Icons.Default.Storefront)
        )

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isTabletOrDesktop = maxWidth >= 640.dp

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MerchantBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(
                                        text = if (isArabic && currentStore?.nameAr?.isNotBlank() == true) currentStore!!.nameAr else (currentStore?.name ?: "Store Portal"),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Slate900,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = currentUser?.name ?: "Merchant Operator",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        },
                        actions = {
                            // Store status pill in header
                            StoreStatusPill(
                                status = metrics.storeStatus,
                                isArabic = isArabic,
                                onStatusChange = { viewModel.updateStoreStatus(it) }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (showSimulationControls) {
                                // Demo-only test order button; disabled in the live merchant portal.
                                IconButton(
                                    onClick = { viewModel.simulateIncomingOrder() },
                                    modifier = Modifier.testTag("appbar_simulate_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAlert,
                                        contentDescription = "Simulate Order",
                                        tint = MerchantBlue
                                    )
                                }
                            }

                            // Language switcher
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.toggleLanguage() }
                                    .testTag("topbar_language_btn"),
                                color = Slate100
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate700)
                                    Text(
                                        text = if (isArabic) "EN" else "عربي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Slate900
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                },
                bottomBar = {
                    if (!isTabletOrDesktop && currentScreen != StoreNavScreen.ORDER_DETAILS &&
                        currentScreen != StoreNavScreen.ADD_PRODUCT && currentScreen != StoreNavScreen.EDIT_PRODUCT
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            primaryNavItems.forEach { item ->
                                val selected = currentScreen == item.screen
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(item.screen) },
                                    icon = {
                                        if (item.hasBadge && metrics.newOrdersCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = StatusAmber) {
                                                        Text("${metrics.newOrdersCount}", color = Color.White)
                                                    }
                                                }
                                            ) {
                                                Icon(item.icon, contentDescription = item.titleEn)
                                            }
                                        } else {
                                            Icon(item.icon, contentDescription = item.titleEn)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isArabic) item.titleAr else item.titleEn,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MerchantBlue,
                                        selectedTextColor = MerchantBlue,
                                        indicatorColor = MerchantBlue.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Navigation Rail on tablet / desktop wide screens
                    if (isTabletOrDesktop) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        ) {
                            (primaryNavItems + moreNavItems).forEach { item ->
                                val selected = currentScreen == item.screen
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.navigateTo(item.screen) },
                                    icon = {
                                        if (item.hasBadge && metrics.newOrdersCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = StatusAmber) {
                                                        Text("${metrics.newOrdersCount}", color = Color.White)
                                                    }
                                                }
                                            ) {
                                                Icon(item.icon, contentDescription = item.titleEn)
                                            }
                                        } else {
                                            Icon(item.icon, contentDescription = item.titleEn)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (isArabic) item.titleAr else item.titleEn,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MerchantBlue,
                                        selectedTextColor = MerchantBlue,
                                        indicatorColor = MerchantBlue.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }

                    // Screen Body
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentScreen) {
                            StoreNavScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToScreen = { scr -> viewModel.navigateTo(scr) }
                            )
                            StoreNavScreen.NEW_ORDERS -> NewOrdersScreen(viewModel = viewModel)
                            StoreNavScreen.ORDER_DETAILS -> OrderDetailsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(StoreNavScreen.DASHBOARD) }
                            )
                            StoreNavScreen.ACTIVE_ORDERS -> ActiveOrdersScreen(viewModel = viewModel)
                            StoreNavScreen.COMPLETED_ORDERS -> CompletedOrdersScreen(viewModel = viewModel)
                            StoreNavScreen.PRODUCTS -> ProductsScreen(
                                viewModel = viewModel,
                                onNavigateToAddProduct = { viewModel.startAddProduct() }
                            )
                            StoreNavScreen.ADD_PRODUCT, StoreNavScreen.EDIT_PRODUCT -> ProductEditScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(StoreNavScreen.PRODUCTS) }
                            )
                            StoreNavScreen.CATEGORIES -> CategoriesScreen(viewModel = viewModel)
                            StoreNavScreen.STORE_PROFILE -> StoreProfileScreen(viewModel = viewModel)
                            StoreNavScreen.STORE_SETTINGS -> StoreSettingsScreen(viewModel = viewModel)
                            StoreNavScreen.LOGIN -> StoreLoginScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
