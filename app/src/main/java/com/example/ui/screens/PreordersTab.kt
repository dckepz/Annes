package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.models.Preorder
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PreordersTab(
    preorders: List<Preorder>,
    isGlobalPreorderEnabled: Boolean,
    onToggleGlobalPreorder: (Boolean) -> Unit,
    onUpdatePreorderStatus: (Long, String) -> Unit,
    onCreatePreorderClick: () -> Unit,
    userIsAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedStatusFilter by remember { mutableStateOf("All") }
    val statusFilters = listOf("All", "Pending", "Confirmed", "Fulfilling", "Completed", "Cancelled")

    val filteredPreorders = remember(preorders, selectedStatusFilter) {
        if (selectedStatusFilter == "All") {
            preorders
        } else {
            preorders.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Global Pre-Order Control Card (Admin controlled)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (isGlobalPreorderEnabled) listOf(DarkGold, PrimaryGold) else listOf(SurfaceCardBorder, SurfaceCardBorder)
                    ),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = if (isGlobalPreorderEnabled) PrimaryGold else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Website Pre-Order System",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isGlobalPreorderEnabled) "STOREFRONT ACCEPTING PRE-ORDERS" else "PRE-ORDERS PAUSED STOREWIDE",
                                color = if (isGlobalPreorderEnabled) PrimaryGold else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Switch(
                            checked = isGlobalPreorderEnabled,
                            onCheckedChange = { if (userIsAdmin) onToggleGlobalPreorder(it) },
                            enabled = userIsAdmin,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryGold,
                                checkedTrackColor = DarkGold.copy(alpha = 0.5f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCardElevated
                            ),
                            modifier = Modifier.testTag("switch_global_preorder")
                        )
                    }

                    if (!userIsAdmin) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: Global switch is managed by Admin",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Action Header & New Preorder Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customer Pre-Orders (${filteredPreorders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track custom fits, deposits & expected deliveries",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onCreatePreorderClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = AppBlack),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_preorder")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Pre-Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatusFilter.equals(status, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) PrimaryGold else SurfaceCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedStatusFilter = status },
                        color = if (isSelected) GoldMuted else SurfaceCard
                    ) {
                        Text(
                            text = status,
                            color = if (isSelected) PrimaryGold else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Empty state
        if (filteredPreorders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedStatusFilter == "All") "No customer pre-orders recorded" else "No pre-orders with status '$selectedStatusFilter'",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pre-orders placed on the website or recorded in-store will show here.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onCreatePreorderClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = AppBlack)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Pre-Order Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredPreorders, key = { it.id }) { preorder ->
                PreorderCard(
                    preorder = preorder,
                    userIsAdmin = userIsAdmin,
                    onStatusChange = { newStatus -> onUpdatePreorderStatus(preorder.id, newStatus) }
                )
            }
        }
    }
}

@Composable
fun PreorderCard(
    preorder: Preorder,
    userIsAdmin: Boolean,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val remainingBalance = (preorder.totalAmount - preorder.depositAmount).coerceAtLeast(0.0)

    val (statusColor, statusBg) = when (preorder.status.lowercase()) {
        "pending" -> Color(0xFFF59E0B) to Color(0x29F59E0B)
        "confirmed" -> Color(0xFF3B82F6) to Color(0x293B82F6)
        "fulfilling" -> Color(0xFF8B5CF6) to Color(0x298B5CF6)
        "completed" -> Color(0xFF10B981) to Color(0x2910B981)
        "cancelled" -> Color(0xFFEF4444) to Color(0x29EF4444)
        else -> TextMuted to SurfaceCardBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(statusColor.copy(alpha = 0.4f), Color.Transparent)),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Preorder Number + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = statusBg,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (preorder.status.lowercase()) {
                                    "completed" -> Icons.Filled.CheckCircle
                                    "fulfilling" -> Icons.Filled.LocalShipping
                                    "confirmed" -> Icons.Filled.ThumbUp
                                    "cancelled" -> Icons.Filled.Cancel
                                    else -> Icons.Filled.HourglassTop
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRE-ORDER #${preorder.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = preorder.status.uppercase(),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product & Customer details
            Text(
                text = "${preorder.quantity}x ${preorder.productTitle}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = preorder.customerName,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${preorder.customerPhone}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                if (!preorder.expectedDate.isNullOrBlank()) {
                    Text(
                        text = "Due: ${preorder.expectedDate}",
                        fontSize = 11.sp,
                        color = PrimaryGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Financial breakdown: Deposit Paid & Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Deposit Paid", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatKSh(preorder.depositAmount),
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Remaining Balance", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatKSh(remainingBalance),
                        color = if (remainingBalance > 0) PrimaryGold else StatusSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Value", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = formatKSh(preorder.totalAmount),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Expanded Status Controller & Notes
            if (expanded) {
                if (!preorder.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: ${preorder.notes}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Update Status:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("pending", "confirmed", "fulfilling", "completed", "cancelled").forEach { st ->
                        val isCurrent = preorder.status.equals(st, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) PrimaryGold else SurfaceCardElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusChange(st) }
                        ) {
                            Text(
                                text = st.replaceFirstChar { it.uppercase() },
                                color = if (isCurrent) AppBlack else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
