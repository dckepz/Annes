package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

fun formatKSh(amount: Double): String {
    return "KSh " + String.format(Locale.US, "%,.0f", amount)
}

fun formatKShExact(amount: Double): String {
    return "KSh " + String.format(Locale.US, "%,.2f", amount)
}

@Composable
fun RoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    val isAdmin = role.equals("admin", ignoreCase = true)
    val bgColor = if (isAdmin) GoldMuted else Color(0x334A90E2)
    val textColor = if (isAdmin) PrimaryGold else Color(0xFF70B0FF)
    val borderColor = if (isAdmin) DarkGold else Color(0xFF4A90E2)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isAdmin) Icons.Filled.Stars else Icons.Filled.Person,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = role.uppercase(),
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.35f) }),
            width = 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(gradientColors.first().copy(alpha = 0.12f), Color.Transparent),
                        radius = 240f
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (onClick != null) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StockBadge(
    stock: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        stock <= 0 -> Triple(StatusDangerBg, StatusDanger, "Out of Stock")
        stock <= 5 -> Triple(StatusWarningBg, StatusWarning, "Low: $stock left")
        else -> Triple(StatusSuccessBg, StatusSuccess, "$stock in stock")
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PaymentMethodBadge(
    method: String,
    reference: String? = null,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (method.lowercase()) {
        "mpesa" -> Color(0xFF4CAF50) to "M-Pesa"
        "card" -> Color(0xFF2196F3) to "Card"
        "cash" -> Color(0xFFFFB300) to "Cash"
        "bank-transfer" -> Color(0xFF9C27B0) to "Bank Transfer"
        else -> Color(0xFF9E9E9E) to method.replaceFirstChar { it.uppercase() }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.18f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (!reference.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = reference,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AnnesTopAppBar(
    title: String,
    userRole: String,
    onPosClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSyncClick: () -> Unit = onSettingsClick,
    isServerOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HeaderHeroWarm,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSyncClick() }
                ) {
                    AnnesGeometricLogo(
                        size = 32.dp,
                        animated = false,
                        variant = AnnesLogoVariant.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Anne's",
                                color = LightGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fashion Line",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isServerOnline) Color(0x2210B981) else Color(0x22F59E0B))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isServerOnline) StatusSuccess else StatusWarning)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isServerOnline) "Cloud Live ⚡" else "Tap to Sync ☁️",
                                color = if (isServerOnline) StatusSuccess else StatusWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RoleBadge(role = userRole)

                    // Cloud Sync Center Button
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isServerOnline) Color(0x2210B981) else Color(0x22F59E0B))
                            .testTag("btn_sync_center")
                    ) {
                        Icon(
                            imageVector = if (isServerOnline) Icons.Filled.CloudDone else Icons.Filled.CloudSync,
                            contentDescription = "Cloud Sync Center",
                            tint = if (isServerOnline) StatusSuccess else StatusWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick POS Action Button
                    IconButton(
                        onClick = onPosClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldMuted)
                            .testTag("btn_quick_pos")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PointOfSale,
                            contentDescription = "Quick POS",
                            tint = PrimaryGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
