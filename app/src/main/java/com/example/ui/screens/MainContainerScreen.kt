package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnnesTopAppBar
import com.example.ui.theme.*
import com.example.viewmodel.AnnesViewModel

data class BottomNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun MainContainerScreen(
    viewModel: AnnesViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isServerConnected by viewModel.isServerConnected.collectAsState()
    val globalPreorderMode by viewModel.globalPreorderMode.collectAsState()

    val products by viewModel.products.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val dashboardData by viewModel.dashboardData.collectAsState()

    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLowStockOnly by viewModel.inventoryOnlyLowStock.collectAsState()
    val salesTypeFilter by viewModel.salesTypeFilter.collectAsState()

    val showSettingsSheet by viewModel.showSettingsSheet.collectAsState()
    val showSyncCenterSheet by viewModel.showSyncCenterSheet.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val websiteUrl by viewModel.websiteUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showAddProductSheet by viewModel.showAddProductSheet.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val updatingStockProduct by viewModel.updatingStockProduct.collectAsState()
    val showRecordSaleSheet by viewModel.showRecordSaleSheet.collectAsState()
    val showQuickPos by viewModel.showQuickPos.collectAsState()
    val posCart by viewModel.posCart.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val preorders by viewModel.preorders.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()
    val mpesaStkState by viewModel.mpesaStkState.collectAsState()
    val showCreatePreorderSheet by viewModel.showCreatePreorderSheet.collectAsState()
    val showImportCsvSheet by viewModel.showImportCsvSheet.collectAsState()
    val selectedProductDetail by viewModel.selectedProductDetail.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val lowStockCount = remember(products) {
        products.count { it.totalStock <= 5 }
    }

    val unreadChatCount = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val isStaff = currentUser?.role?.lowercase() == "staff"

    val navTabs = remember(isStaff, lowStockCount, unreadChatCount) {
        if (isStaff) {
            listOf(
                BottomNavTab("POS", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
                BottomNavTab("Products", Icons.Filled.Checkroom, Icons.Outlined.Checkroom),
                BottomNavTab("Sales", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
                BottomNavTab("Pre-orders", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
                BottomNavTab("Support", Icons.Filled.Chat, Icons.Outlined.Chat, badgeCount = unreadChatCount)
            )
        } else {
            listOf(
                BottomNavTab("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                BottomNavTab("Products", Icons.Filled.Checkroom, Icons.Outlined.Checkroom),
                BottomNavTab("Sales", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
                BottomNavTab("Inventory", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, badgeCount = lowStockCount),
                BottomNavTab("Pre-orders", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
                BottomNavTab("Support", Icons.Filled.Chat, Icons.Outlined.Chat, badgeCount = unreadChatCount)
            )
        }
    }

    val activeTabIndex = if (currentTab >= navTabs.size) 0 else currentTab

    Scaffold(
        containerColor = AppBlack,
        topBar = {
            AnnesTopAppBar(
                title = navTabs[activeTabIndex].title,
                userRole = currentUser?.role ?: "admin",
                onPosClick = {
                    if (isStaff) {
                        viewModel.selectTab(0)
                    } else {
                        viewModel.openRecordSale()
                    }
                },
                onSettingsClick = { viewModel.openSettings() },
                onRefreshClick = { viewModel.refreshAll() },
                onSyncClick = { viewModel.openSyncCenter() },
                isServerOnline = syncState.isSupabaseOnline || isServerConnected
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                navTabs.forEachIndexed { index, tab ->
                    val isSelected = activeTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab.badgeCount > 0) {
                                        Badge(containerColor = StatusDanger) {
                                            Text("${tab.badgeCount}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) PrimaryGold else TextMuted
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) PrimaryGold else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGold,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = GoldMuted,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_${tab.title.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (!isStaff) {
                when (activeTabIndex) {
                    1 -> { // Products Tab -> Add Product FAB
                        FloatingActionButton(
                            onClick = { viewModel.openAddProduct() },
                            containerColor = PrimaryGold,
                            contentColor = AppBlack,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_add_product")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Product", tint = AppBlack)
                        }
                    }
                    2 -> { // Sales Tab -> Record Sale FAB
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.openRecordSale() },
                            containerColor = PrimaryGold,
                            contentColor = AppBlack,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_record_sale")
                        ) {
                            Icon(Icons.Filled.PointOfSale, contentDescription = null, tint = AppBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Record Sale", fontWeight = FontWeight.Bold)
                        }
                    }
                    4 -> { // Pre-orders Tab -> Create Pre-order FAB
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.openCreatePreorder() },
                            containerColor = PrimaryGold,
                            contentColor = AppBlack,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_create_preorder")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = AppBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Pre-order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                when (activeTabIndex) {
                    3 -> { // Staff Pre-orders Tab
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.openCreatePreorder() },
                            containerColor = PrimaryGold,
                            contentColor = AppBlack,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_staff_create_preorder")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = AppBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Pre-order", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceCardElevated,
                    contentColor = TextPrimary,
                    actionColor = PrimaryGold,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isStaff) {
                when (activeTabIndex) {
                    0 -> StaffPosTab(
                        products = products,
                        cartItems = posCart,
                        onAddToCart = { viewModel.addToPosCart(it) },
                        onUpdateQuantity = { id, qty -> viewModel.updatePosCartItemQuantity(id, qty) },
                        onClearCart = { viewModel.clearPosCart() },
                        onCompleteSale = { saleType, name, phone, method, ref, items, notes, onDone ->
                            viewModel.completeSale(saleType, name, phone, method, ref, items, notes, onDone)
                        },
                        mpesaStkState = mpesaStkState,
                        onInitiateMpesaStkPush = { phone, amount ->
                            viewModel.initiateMpesaStkPush(phone, amount, "STAFF_POS")
                        },
                        cashierName = currentUser?.fullName ?: "Staff"
                    )
                    1 -> ProductsTab(
                        products = products,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChange = { viewModel.setProductSearch(it) },
                        onCategoryChange = { viewModel.setCategoryFilter(it) },
                        onProductClick = { viewModel.openProductDetail(it) },
                        onEditProductClick = { /* Staff view only */ },
                        onAdjustStockClick = { /* Staff view only */ },
                        onAddToPosClick = {
                            viewModel.addToPosCart(it)
                            viewModel.showMessage("Added ${it.title} to POS cart")
                        },
                        userIsAdmin = false,
                        onImportProductsClick = null
                    )
                    2 -> SalesTab(
                        sales = sales,
                        selectedTypeFilter = salesTypeFilter,
                        onTypeFilterChange = { viewModel.setSalesTypeFilter(it) },
                        onRecordSaleClick = { viewModel.selectTab(0) },
                        userIsAdmin = false
                    )
                    3 -> PreordersTab(
                        preorders = preorders,
                        isGlobalPreorderEnabled = globalPreorderMode,
                        onToggleGlobalPreorder = { /* Staff cannot toggle global settings */ },
                        onUpdatePreorderStatus = { id, st -> viewModel.updatePreorderStatus(id, st) },
                        onCreatePreorderClick = { viewModel.openCreatePreorder() },
                        userIsAdmin = false
                    )
                    4 -> ChatScreen(
                        conversations = conversations,
                        activeMessages = activeChatMessages,
                        selectedConversation = selectedConversation,
                        onSelectConversation = { viewModel.selectConversation(it) },
                        onBackToList = { viewModel.clearSelectedConversation() },
                        onSendMessage = { convId, text -> viewModel.sendChatMessage(convId, text) },
                        onRefresh = { viewModel.fetchConversations() }
                    )
                }
            } else {
                when (activeTabIndex) {
                    0 -> DashboardTab(
                        dashboardData = dashboardData,
                        onLowStockClick = { viewModel.jumpToInventoryLowStock() },
                        onOpenPosClick = { viewModel.openRecordSale() },
                        onOpenProductsClick = { viewModel.selectTab(1) },
                        onOpenSalesClick = { viewModel.selectTab(2) }
                    )
                    1 -> ProductsTab(
                        products = products,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChange = { viewModel.setProductSearch(it) },
                        onCategoryChange = { viewModel.setCategoryFilter(it) },
                        onProductClick = { viewModel.openEditProduct(it) },
                        onEditProductClick = { viewModel.openEditProduct(it) },
                        onAdjustStockClick = { viewModel.openStockUpdate(it) },
                        onAddToPosClick = {
                            viewModel.addToPosCart(it)
                            viewModel.showMessage("Added ${it.title} to POS cart")
                        },
                        userIsAdmin = true,
                        onImportProductsClick = { viewModel.openImportCsv() }
                    )
                    2 -> SalesTab(
                        sales = sales,
                        selectedTypeFilter = salesTypeFilter,
                        onTypeFilterChange = { viewModel.setSalesTypeFilter(it) },
                        onRecordSaleClick = { viewModel.openRecordSale() },
                        userIsAdmin = true
                    )
                    3 -> InventoryTab(
                        products = products,
                        isLowStockOnly = isLowStockOnly,
                        onFilterChange = { viewModel.setInventoryFilter(it) },
                        onProductStockClick = { viewModel.openStockUpdate(it) }
                    )
                    4 -> PreordersTab(
                        preorders = preorders,
                        isGlobalPreorderEnabled = globalPreorderMode,
                        onToggleGlobalPreorder = { viewModel.toggleGlobalPreorderMode(it) },
                        onUpdatePreorderStatus = { id, st -> viewModel.updatePreorderStatus(id, st) },
                        onCreatePreorderClick = { viewModel.openCreatePreorder() },
                        userIsAdmin = true
                    )
                    5 -> ChatScreen(
                        conversations = conversations,
                        activeMessages = activeChatMessages,
                        selectedConversation = selectedConversation,
                        onSelectConversation = { viewModel.selectConversation(it) },
                        onBackToList = { viewModel.clearSelectedConversation() },
                        onSendMessage = { convId, text -> viewModel.sendChatMessage(convId, text) },
                        onRefresh = { viewModel.fetchConversations() }
                    )
                }
            }
        }
    }

    // ==================== BOTTOM SHEETS & MODALS ====================

    // Add Product Sheet
    if (showAddProductSheet) {
        AddEditProductBottomSheet(
            productToEdit = null,
            onDismiss = { viewModel.closeAddProduct() },
            onSaveNew = { title, desc, price, cat, stock, feat, pre, img, sz, col, wst, bst, shoe ->
                viewModel.createProduct(title, desc, price, cat, stock, feat, pre, img, sz, col, wst, bst, shoe)
            },
            onSaveEdit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onDelete = null,
            userIsAdmin = currentUser?.isAdmin == true
        )
    }

    // Edit Product Sheet
    editingProduct?.let { prod ->
        AddEditProductBottomSheet(
            productToEdit = prod,
            onDismiss = { viewModel.closeEditProduct() },
            onSaveNew = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onSaveEdit = { id, title, desc, price, cat, feat, pre, img, sz, col, wst, bst, shoe ->
                viewModel.updateProduct(id, title, desc, price, cat, feat, pre, img, sz, col, wst, bst, shoe)
            },
            onDelete = { id -> viewModel.deleteProduct(id) },
            userIsAdmin = currentUser?.isAdmin == true
        )
    }

    // Product Detail Sheet
    selectedProductDetail?.let { prod ->
        ProductDetailBottomSheet(
            product = prod,
            onDismiss = { viewModel.closeProductDetail() },
            onEdit = {
                viewModel.closeProductDetail()
                viewModel.openEditProduct(prod)
            },
            onAdjustStock = {
                viewModel.closeProductDetail()
                viewModel.openStockUpdate(prod)
            },
            onAddToCart = { sz, col ->
                viewModel.addToPosCart(prod, size = sz, color = col)
                val extraInfo = listOfNotNull(sz?.let { "Size $it" }, col).joinToString(", ")
                viewModel.showMessage("Added ${prod.title}${if (extraInfo.isNotBlank()) " ($extraInfo)" else ""} to POS cart")
            },
            userIsAdmin = currentUser?.isAdmin == true
        )
    }

    // Update Stock Sheet
    updatingStockProduct?.let { prod ->
        UpdateStockBottomSheet(
            product = prod,
            onDismiss = { viewModel.closeStockUpdate() },
            onConfirm = { id, change, actionType, reason ->
                viewModel.adjustStock(id, change, actionType, reason)
            }
        )
    }

    // Record Sale / POS Sheet
    if (showRecordSaleSheet || showQuickPos) {
        RecordSaleBottomSheet(
            products = products,
            cartItems = posCart,
            onAddToCart = { viewModel.addToPosCart(it) },
            onUpdateQuantity = { id, qty -> viewModel.updatePosCartItemQuantity(id, qty) },
            onCompleteSale = { saleType, name, phone, method, ref, items, notes, onDone ->
                viewModel.completeSale(saleType, name, phone, method, ref, items, notes, onDone)
            },
            onDismiss = {
                viewModel.closeRecordSale()
                viewModel.closeQuickPos()
            },
            mpesaStkState = mpesaStkState,
            onInitiateMpesaStkPush = { phone, amount ->
                viewModel.initiateMpesaStkPush(phone, amount, "POS")
            }
        )
    }

    // Create Pre-order Sheet
    if (showCreatePreorderSheet) {
        CreatePreorderBottomSheet(
            products = products,
            onDismiss = { viewModel.closeCreatePreorder() },
            onCreatePreorder = { prodId, title, custName, custPhone, qty, deposit, total, expDate, notes ->
                viewModel.createPreorder(
                    prodId, title, custName, custPhone, qty, deposit, total, expDate, notes
                ) { success ->
                    if (success) {
                        viewModel.fetchPreorders()
                    }
                }
            }
        )
    }

    // Admin-Only CSV / Excel Import Sheet
    if (showImportCsvSheet && currentUser?.isAdmin == true) {
        ImportProductsBottomSheet(
            onDismiss = { viewModel.closeImportCsv() },
            onImportCsv = { csvText ->
                viewModel.importProductsFromCsv(csvText) { report ->
                    if (report.successful > 0) {
                        viewModel.closeImportCsv()
                        viewModel.refreshAll()
                    }
                }
            }
        )
    }

    // Settings & Configuration Sheet
    if (showSettingsSheet) {
        val currentBaseUrl by viewModel.baseUrl.collectAsState()
        val themeMode by viewModel.themeMode.collectAsState()
        val supabaseUrl by viewModel.supabaseUrl.collectAsState()
        val supabaseAnonKey by viewModel.supabaseAnonKey.collectAsState()
        val supabaseServiceKey by viewModel.supabaseServiceKey.collectAsState()

        SettingsBottomSheet(
            currentUser = currentUser,
            currentBaseUrl = currentBaseUrl,
            isServerOnline = isServerConnected,
            themeMode = themeMode,
            onSetThemeMode = { viewModel.setThemeMode(it) },
            supabaseUrl = supabaseUrl,
            supabaseAnonKey = supabaseAnonKey,
            supabaseServiceKey = supabaseServiceKey,
            onUpdateSupabaseCredentials = { url, anon, serviceKey ->
                viewModel.updateSupabaseCredentials(url, anon, serviceKey)
            },
            onUpdateBaseUrl = { viewModel.updateServerUrl(it) },
            onPingServer = { viewModel.pingServer() },
            onSwitchRole = { viewModel.switchRole(it) },
            onLogout = { viewModel.logout() },
            onDismiss = { viewModel.closeSettings() }
        )
    }

    // Live Cloud & Website Sync Center Sheet
    if (showSyncCenterSheet) {
        val supabaseUrl by viewModel.supabaseUrl.collectAsState()
        val supabaseAnonKey by viewModel.supabaseAnonKey.collectAsState()
        val supabaseServiceKey by viewModel.supabaseServiceKey.collectAsState()

        SyncCenterBottomSheet(
            syncState = syncState,
            supabaseUrl = supabaseUrl,
            supabaseAnonKey = supabaseAnonKey,
            supabaseServiceKey = supabaseServiceKey,
            websiteUrl = websiteUrl,
            isSyncing = isLoading,
            onUpdateCredentials = { url, anon, service ->
                viewModel.updateSupabaseCredentials(url, anon, service)
            },
            onUpdateWebsiteUrl = { url ->
                viewModel.updateWebsiteUrl(url)
            },
            onRunDiagnostics = { customUrl ->
                viewModel.runCloudDiagnostics(customUrl)
            },
            onPullFromCloud = {
                viewModel.pullFromCloud()
            },
            onPushToCloud = {
                viewModel.pushToCloud()
            },
            sqlSchema = viewModel.getSupabaseSetupSql(),
            onDismiss = { viewModel.closeSyncCenter() }
        )
    }
}
