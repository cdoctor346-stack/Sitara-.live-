package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun LiveChatOverlay(
    messages: List<ChatMessage>,
    onMessageClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            ChatItemBubble(message = message, onMessageClick = onMessageClick)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ChatItemBubble(message: ChatMessage, onMessageClick: (ChatMessage) -> Unit) {
    val isGift = message.giftNotice != null

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isGift) Color(0xD94A154B) else Color(0xB3121124)
            )
            .clickable { onMessageClick(message) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Badges
            if (message.isStreamer) {
                BadgeChip(text = "HOST", color = NeonPink)
                Spacer(modifier = Modifier.width(6.dp))
            } else if (message.isModerator) {
                BadgeChip(text = "MOD", color = NeonCyan)
                Spacer(modifier = Modifier.width(6.dp))
            } else if (message.isSub) {
                BadgeChip(text = "SUB ⭐", color = NeonAmber)
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = "${message.senderName}: ",
                color = if (message.isStreamer) NeonPink else Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message.text,
                color = if (isGift) NeonAmber else Color(0xFFF1F5F9),
                fontSize = 12.sp,
                fontWeight = if (isGift) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun BadgeChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.25f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
