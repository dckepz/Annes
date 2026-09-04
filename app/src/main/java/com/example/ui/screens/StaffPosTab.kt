package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.MpesaStkState
import com.example.data.models.Product
import com.example.ui.components.formatKSh
import com.example.ui.theme.*
import com.example.viewmodel.PosCartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffPosTab(
    products: List<Product>,
    cartItems: List<PosCartItem>,
    onAddToCart: (Product) -> Unit,
    onUpdateQuantity: (productId: Int, qty: Int) -> Unit,
    onClearCart: () -> Unit,
    onCompleteSale: (
        saleType: String,
        name: String?,
        phone: String?,
        paymentMethod: String,
        ref: String?,
        items: List<PosCartItem>,
        notes: String?,
        onDone: (Boolean) -> Unit
    ) -> Unit,
    mpesaStkState: MpesaStkState,
    onInitiateMpesaStkPush: (phone: String, amount: Double) -> Unit,
    cashierName: String,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCheckoutSheet by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category.replaceFirstChar { c -> c.uppercase() } }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products
            .sortedByDescending { it.createdAt ?: it.id.toString() }
            .filter { prod ->
                val matchesQuery = searchQuery.isBlank() ||
                        prod.title.contains(searchQuery, ignoreCase = true) ||
                        prod.sku.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == "All" ||
                        prod.category.equals(selectedCategory, ignoreCase = true)
                matchesQuery && matchesCategory
            }
    }

    val cartTotal = remember(cartItems) {
        cartItems.sumOf { it.product.price * it.quantity }
    }
    val cartCount = remember(cartItems) {
        cartItems.sumOf { it.quantity }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBlack)
    ) {
        // Staff Header Banner
        Surface(
            color = SurfaceDark,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Staff POS Terminal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cashier: $cashierName",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (cartItems.isNotEmpty()) {
                    Button(
                        onClick = { showCheckoutSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = AppBlack),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cart ($cartCount) • ${formatKSh(cartTotal)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search garment title, SKU, or style...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input")
            )

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryGold else SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) PrimaryGold else SurfaceCardBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AppBlack else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Product Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No catalog pieces match your search", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val inCartItem = cartItems.find { it.product.id == product.id }
                    PosProductCard(
                        product = product,
                        quantityInCart = inCartItem?.quantity ?: 0,
                        onAddToCart = { onAddToCart(product) },
                        onUpdateQuantity = { qty -> onUpdateQuantity(product.id, qty) }
                    )
                }
            }
        }
    }

    // Checkout Modal Bottom Sheet
    if (showCheckoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            containerColor = SurfaceDark,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            PosCheckoutSheetContent(
                cartItems = cartItems,
                totalAmount = cartTotal,
                onUpdateQuantity = onUpdateQuantity,
                onClearCart = onClearCart,
                mpesaStkState = mpesaStkState,
                onInitiateMpesaStkPush = onInitiateMpesaStkPush,
                onCompleteSale = { saleType, name, phone, method, ref, items, notes, onDone ->
                    onCompleteSale(saleType, name, phone, method, ref, items, notes) { success ->
                        if (success) {
                            showCheckoutSheet = false
                        }
                        onDone(success)
                    }
                },
                onClose = { showCheckoutSheet = false }
            )
        }
    }
}

@Composable
fun PosProductCard(
    product: Product,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, if (quantityInCart > 0) PrimaryGold else SurfaceCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
            ) {
                if (product.imageUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Checkroom, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                    }
                }

                // Stock Badge
                Surface(
                    color = if (product.totalStock > 3) FashionEmerald.copy(alpha = 0.85f) else Color(0xFFD97706),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${product.totalStock} in stock",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1
            )

            Text(
                text = formatKSh(product.price),
                color = PrimaryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (quantityInCart == 0) {
                Button(
                    onClick = onAddToCart,
                    enabled = product.totalStock > 0,
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = AppBlack),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .border(1.dp, PrimaryGold, RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onUpdateQuantity(quantityInCart - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = PrimaryGold, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "$quantityInCart",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    IconButton(
                        onClick = { onUpdateQuantity(quantityInCart + 1) },
                        enabled = quantityInCart < product.totalStock,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = PrimaryGold, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PosCheckoutSheetContent(
    cartItems: List<PosCartItem>,
    totalAmount: Double,
    onUpdateQuantity: (productId: Int, qty: Int) -> Unit,
    onClearCart: () -> Unit,
    mpesaStkState: MpesaStkState,
    onInitiateMpesaStkPush: (phone: String, amount: Double) -> Unit,
    onCompleteSale: (
        saleType: String,
        name: String?,
        phone: String?,
        paymentMethod: String,
        ref: String?,
        items: List<PosCartItem>,
        notes: String?,
        onDone: (Boolean) -> Unit
    ) -> Unit,
    onClose: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("mpesa") }
    var transactionRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Boutique Sale Checkout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${cartItems.size} items in cart • Total ${formatKSh(totalAmount)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            TextButton(
                onClick = onClearCart,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text("Clear Cart", fontSize = 12.sp)
            }
        }

        // Cart Items Summary
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            val variantDesc = listOfNotNull(item.selectedSize?.let { "Size $it" }, item.selectedColor).joinToString(" • ")
                            if (variantDesc.isNotBlank()) {
                                Text(variantDesc, fontSize = 10.sp, color = PrimaryGold, fontWeight = FontWeight.Medium)
                            }
                            Text("${formatKSh(item.product.price)} x ${item.quantity}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            text = formatKSh(item.product.price * item.quantity),
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Customer Details
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name (Optional)") },
            placeholder = { Text("e.g. Grace Njeri") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Customer M-Pesa Phone *") },
            placeholder = { Text("0712345678") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        )

        // Payment Method Selector
        Text("Payment Method:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("mpesa" to "M-Pesa", "cash" to "Cash", "card" to "Card").forEach { (method, label) ->
                val isSelected = paymentMethod == method
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) PrimaryGold else SurfaceCard,
                    border = BorderStroke(1.dp, if (isSelected) PrimaryGold else SurfaceCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { paymentMethod = method }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) AppBlack else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // M-Pesa STK Push Trigger
        if (paymentMethod == "mpesa") {
            OutlinedButton(
                onClick = {
                    if (customerPhone.isNotBlank() && totalAmount > 0) {
                        onInitiateMpesaStkPush(customerPhone, totalAmount)
                    }
                },
                enabled = customerPhone.isNotBlank() && totalAmount > 0 && !mpesaStkState.isInitiating,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FashionEmerald),
                border = BorderStroke(1.dp, FashionEmerald),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                if (mpesaStkState.isInitiating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FashionEmerald, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prompting Customer Phone...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send M-Pesa STK Push Prompt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (mpesaStkState.message != null) {
                Text(
                    text = mpesaStkState.message,
                    fontSize = 11.sp,
                    color = if (mpesaStkState.status == "completed") FashionEmerald else TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        OutlinedTextField(
            value = transactionRef,
            onValueChange = { transactionRef = it.uppercase() },
            label = { Text("Receipt / Ref Code (M-Pesa or Card)") },
            placeholder = { Text("e.g. QJH3X7Y9") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                isProcessing = true
                onCompleteSale(
                    "in-store",
                    customerName.ifBlank { null },
                    customerPhone.ifBlank { null },
                    paymentMethod,
                    transactionRef.ifBlank { null },
                    cartItems,
                    notes.ifBlank { null }
                ) {
                    isProcessing = false
                }
            },
            enabled = cartItems.isNotEmpty() && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = AppBlack)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppBlack)
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Checkout • ${formatKSh(totalAmount)}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
