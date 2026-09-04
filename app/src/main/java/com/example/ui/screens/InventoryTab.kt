package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Product
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun InventoryTab(
    products: List<Product>,
    isLowStockOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onProductStockClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedProducts = remember(products, isLowStockOnly) {
        if (isLowStockOnly) {
            products.filter { it.totalStock <= 5 }
        } else {
            products
        }
    }

    val lowStockCount = remember(products) {
        products.count { it.totalStock <= 5 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Filter Tabs: All Items vs Low Stock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // All Items Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (!isLowStockOnly) PrimaryGold else SurfaceCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onFilterChange(false) },
                color = if (!isLowStockOnly) GoldMuted else SurfaceCard
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Inventory (${products.size})",
                        color = if (!isLowStockOnly) PrimaryGold else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (!isLowStockOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Low Stock Alert Tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isLowStockOnly) StatusDanger else SurfaceCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onFilterChange(true) },
                color = if (isLowStockOnly) StatusDangerBg else SurfaceCard
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = if (isLowStockOnly) StatusDanger else StatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Low Stock ($lowStockCount)",
                        color = if (isLowStockOnly) StatusDanger else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isLowStockOnly) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Inventory List
        if (displayedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircleOutline,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All boutique inventory levels are healthy!",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedProducts, key = { it.id }) { product ->
                    InventoryItemCard(
                        product = product,
                        onClick = { onProductStockClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    product: Product,
    onClick: () -> Unit
) {
    val (statusColor, statusBg, statusText) = when {
        product.totalStock <= 0 -> Triple(StatusDanger, StatusDangerBg, "Out of Stock")
        product.totalStock <= 5 -> Triple(StatusWarning, StatusWarningBg, "Low Stock")
        else -> Triple(StatusSuccess, StatusSuccessBg, "In Stock")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("inventory_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SurfaceCardBorder, Color.Transparent)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.sku,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${product.category.uppercase()}",
                        color = PrimaryGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Reorder point: 5 units",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Stock Count Box
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                color = statusBg
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${product.totalStock}",
                        color = statusColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "UNITS",
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
