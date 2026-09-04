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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DashboardData
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardTab(
    dashboardData: DashboardData,
    onLowStockClick: () -> Unit,
    onOpenPosClick: () -> Unit,
    onOpenProductsClick: () -> Unit,
    onOpenSalesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action POS Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPosClick() }
                    .testTag("banner_quick_pos"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HeaderHeroWarm),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(DarkGold, PrimaryGold)),
                    width = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "POINT OF SALE READY",
                                color = PrimaryGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Boutique Quick Register",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to launch instant checkout & M-Pesa sale",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddShoppingCart,
                            contentDescription = "Open POS",
                            tint = AppBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Metrics Overview
        item {
            Text(
                text = "Performance Overview",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // 4 Stat Cards in 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🟢 Today's Revenue
                    StatCard(
                        title = "Today's Revenue",
                        value = formatKSh(dashboardData.today.revenue),
                        subtitle = "${dashboardData.today.salesCount} boutique sales",
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconColor = StatusSuccess,
                        gradientColors = listOf(StatusSuccess, DarkGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_today_revenue"),
                        onClick = onOpenSalesClick
                    )

                    // 🔵 Today's Sales Count
                    StatCard(
                        title = "Today's Sales",
                        value = "${dashboardData.today.salesCount}",
                        subtitle = "Transactions completed",
                        icon = Icons.Filled.ReceiptLong,
                        iconColor = StatusInfo,
                        gradientColors = listOf(StatusInfo, Color(0xFF673AB7)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_today_sales"),
                        onClick = onOpenSalesClick
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🟡 This Month Revenue
                    StatCard(
                        title = "This Month",
                        value = formatKSh(dashboardData.thisMonth.revenue),
                        subtitle = "${dashboardData.thisMonth.salesCount} total sales",
                        icon = Icons.Filled.TrendingUp,
                        iconColor = PrimaryGold,
                        gradientColors = listOf(PrimaryGold, DarkGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_month_revenue"),
                        onClick = onOpenSalesClick
                    )

                    // 🔴 Low Stock Items Count
                    StatCard(
                        title = "Low Stock Alert",
                        value = "${dashboardData.lowStockCount} items",
                        subtitle = "Tap to review stock",
                        icon = Icons.Filled.WarningAmber,
                        iconColor = if (dashboardData.lowStockCount > 0) StatusDanger else StatusSuccess,
                        gradientColors = listOf(StatusDanger, StatusWarning),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_low_stock"),
                        onClick = onLowStockClick
                    )
                }
            }
        }

        // Top Selling Products Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Selling Products",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All",
                    color = PrimaryGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onOpenProductsClick() }
                        .padding(4.dp)
                )
            }
        }

        if (dashboardData.topProducts.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCard
                ) {
                    Text(
                        text = "No sales recorded yet",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dashboardData.topProducts) { topProd ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SurfaceCardBorder, Color.Transparent)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = topProd.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${topProd.totalSold} units sold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = formatKSh(topProd.totalRevenue),
                            color = PrimaryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "All Sales",
                    color = PrimaryGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onOpenSalesClick() }
                        .padding(4.dp)
                )
            }
        }

        if (dashboardData.recentSales.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceCard
                ) {
                    Text(
                        text = "No recent transactions found",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dashboardData.recentSales) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sale.saleNumber,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PaymentMethodBadge(method = sale.paymentMethod)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sale.createdAt ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = formatKShExact(sale.totalAmount),
                            color = PrimaryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
