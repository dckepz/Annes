package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.data.models.Conversation
import com.example.ui.theme.*

@Composable
fun ChatScreen(
    conversations: List<Conversation>,
    activeMessages: List<ChatMessage>,
    selectedConversation: Conversation?,
    onSelectConversation: (Conversation) -> Unit,
    onBackToList: () -> Unit,
    onSendMessage: (conversationId: Long, text: String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var outgoingMessageText by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                        (it.customerPhone?.contains(searchQuery) == true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBlack)
    ) {
        if (selectedConversation == null) {
            // ==========================================
            // CONVERSATION LIST VIEW
            // ==========================================
            Surface(
                color = SurfaceDark,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Website Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${conversations.size} customer inquiries",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryGold, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search customer conversations...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("chat_search_input")
            )

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active customer chats", color = TextSecondary, fontSize = 14.sp)
                        Text("Online store inquiries will stream in here live", color = TextSecondary.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, if (conv.unreadCount > 0) PrimaryGold else SurfaceCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectConversation(conv) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = conv.customerName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.customerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        conv.customerPhone?.let { phone ->
                                            Text(phone, fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = conv.lastMessage.ifBlank { "New customer inquiry initiated" },
                                        fontSize = 12.sp,
                                        color = if (conv.unreadCount > 0) TextPrimary else TextSecondary,
                                        fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }

                                if (conv.unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${conv.unreadCount}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // ACTIVE THREAD VIEW
            // ==========================================
            val listState = rememberLazyListState()

            LaunchedEffect(activeMessages.size) {
                if (activeMessages.isNotEmpty()) {
                    listState.animateScrollToItem(activeMessages.size - 1)
                }
            }

            Surface(
                color = SurfaceDark,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToList) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedConversation.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = selectedConversation.customerPhone ?: "Online Customer",
                            fontSize = 12.sp,
                            color = FashionEmerald
                        )
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(activeMessages, key = { it.id }) { msg ->
                    val isFromAgent = msg.sender.equals("agent", ignoreCase = true) ||
                            msg.sender.equals("staff", ignoreCase = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isFromAgent) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isFromAgent) 14.dp else 2.dp,
                                bottomEnd = if (isFromAgent) 2.dp else 14.dp
                            ),
                            color = if (isFromAgent) PrimaryGold else SurfaceCard,
                            border = BorderStroke(1.dp, if (isFromAgent) PrimaryGold else SurfaceCardBorder),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                if (isFromAgent) {
                                    Text(
                                        text = msg.senderName.ifBlank { "You (Anne's Staff)" },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppBlack.copy(alpha = 0.7f)
                                    )
                                } else {
                                    Text(
                                        text = selectedConversation.customerName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGold
                                    )
                                }

                                Text(
                                    text = msg.messageText,
                                    fontSize = 13.sp,
                                    color = if (isFromAgent) AppBlack else TextPrimary
                                )

                                msg.createdAt?.let { time ->
                                    Text(
                                        text = time,
                                        fontSize = 9.sp,
                                        color = if (isFromAgent) AppBlack.copy(alpha = 0.6f) else TextSecondary,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = SurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = outgoingMessageText,
                        onValueChange = { outgoingMessageText = it },
                        placeholder = { Text("Reply to customer inquiry...", fontSize = 13.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = AppBlack,
                            unfocusedContainerColor = AppBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (outgoingMessageText.isNotBlank()) {
                                onSendMessage(selectedConversation.id, outgoingMessageText.trim())
                                outgoingMessageText = ""
                            }
                        },
                        enabled = outgoingMessageText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (outgoingMessageText.isNotBlank()) PrimaryGold else SurfaceCardBorder)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (outgoingMessageText.isNotBlank()) AppBlack else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
