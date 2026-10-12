package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.GiftItem
import com.example.data.model.StreamItem
import com.example.ui.components.ActiveGiftAnimation
import com.example.ui.components.CelebrationGiftBanner
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.GiftDrawer
import com.example.ui.components.LiveChatOverlay
import com.example.ui.components.ReactionParticle
import com.example.ui.components.ScreenWideGiftAnimationOverlay
import com.example.ui.theme.BrandDarkCard
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LiveViewerScreen(
    stream: StreamItem,
    messages: List<ChatMessage>,
    gifts: List<GiftItem>,
    coinsBalance: Int,
    onSendMessage: (String) -> Unit,
    onSendGift: (GiftItem) -> Boolean,
    onToggleFollow: (String) -> Unit,
    onTopUpCoins: () -> Unit,
    onReportUser: (reportedUser: String, messageText: String) -> Unit = { _, _ -> },
    onOpenAgoraAudience: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chatInput by remember { mutableStateOf("") }
    var showGiftDrawer by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("1080p60 (Source)") }
    var celebrationGiftName by remember { mutableStateOf<String?>(null) }
    var activeScreenWideGift by remember { mutableStateOf<ActiveGiftAnimation?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedMessageToReport by remember { mutableStateOf<ChatMessage?>(null) }

    var dynamicViewerCount by remember(stream.viewerCount) { mutableIntStateOf(stream.viewerCount) }
    val particles = remember { mutableStateListOf<ReactionParticle>() }

    // Fluctuating viewers & ambient background hearts
    LaunchedEffect(stream.id) {
        while (true) {
            delay(3500)
            dynamicViewerCount = (dynamicViewerCount + Random.nextInt(-15, 25)).coerceAtLeast(100)
            if (Random.nextBoolean()) {
                particles.add(
                    ReactionParticle(
                        id = System.currentTimeMillis() + Random.nextLong(1000),
                        emoji = listOf("❤️", "🔥", "💖", "⭐", "🚀").random(),
                        startXOffset = Random.nextFloat() * 120f - 60f,
                        swayAmount = Random.nextFloat() * 25f + 10f
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Launch heart on double tap anywhere on stream
                        particles.add(
                            ReactionParticle(
                                id = System.currentTimeMillis(),
                                emoji = "💖",
                                startXOffset = 0f,
                                swayAmount = 30f
                            )
                        )
                    }
                )
            }
    ) {
        // Live Stream Video Feed Canvas / Backdrop
        Image(
            painter = painterResource(id = stream.bannerRes ?: R.drawable.stream_hero),
            contentDescription = "Live Video Feed",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient lighting overlay for maximum immersion
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color.Transparent,
                            Color(0xDD090814)
                        )
                    )
                )
        )

        // Floating Reaction Hearts particles
        FloatingReactionsContainer(
            particles = particles,
            onParticleFinished = { id -> particles.removeAll { it.id == id } }
        )

        // Celebration Gift Announcement Banner (Top-Center)
        CelebrationGiftBanner(
            giftTitle = celebrationGiftName,
            onDismiss = { celebrationGiftName = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // Top Streamer & Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 14.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streamer Info Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xB3111827))
                    .border(1.dp, Color(0xFF2E2C4D), RoundedCornerShape(24.dp))
                    .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = stream.streamerAvatarRes ?: R.drawable.creator_moin),
                    contentDescription = stream.streamerName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = stream.streamerName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiveRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${dynamicViewerCount} viewers",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Follow / Unfollow button
                Button(
                    onClick = { onToggleFollow(stream.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stream.isFollowing) Color(0xFF25243E) else NeonPink
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("follow_streamer_button")
                ) {
                    Text(
                        text = if (stream.isFollowing) "Following" else "+ Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Top Action Controls (Quality, Agora RTC, & Close)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Agora RTC Live Stream toggle
                IconButton(
                    onClick = onOpenAgoraAudience,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC111827))
                        .border(1.dp, NeonAmber.copy(alpha = 0.6f), CircleShape)
                        .testTag("agora_rtc_live_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Agora Live SD-RTN",
                        tint = NeonAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quality setting
                IconButton(
                    onClick = { showQualityDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC111827))
                        .border(1.dp, NeonCyan.copy(alpha = 0.6f), CircleShape)
                        .testTag("quality_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hd,
                        contentDescription = "Video Quality",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Close stream
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                        .testTag("close_stream_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Player",
                        tint = Color.White
                    )
                }
            }
        }

        // Stream Tag & Category pill (Upper Left)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 76.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x80000000))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stream.category,
                    color = NeonAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x80000000))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Ultra Low Latency (0.9s)",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Live Chat Overlay & Action Controls (Bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            // Chat messages
            LiveChatOverlay(
                messages = messages,
                onMessageClick = { clickedMessage ->
                    // Set state to show report dialog
                    selectedMessageToReport = clickedMessage
                    showReportDialog = true
                },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat input
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = {
                        Text(
                            text = "Send a comment...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("viewer_chat_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xCC0F172A),
                        unfocusedContainerColor = Color(0x990F172A)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    trailingIcon = {
                        if (chatInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSendMessage(chatInput)
                                    chatInput = ""
                                },
                                modifier = Modifier.testTag("send_comment_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = NeonPink
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Gift Button
                IconButton(
                    onClick = { showGiftDrawer = true },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF007A), Color(0xFF7928CA))
                            )
                        )
                        .testTag("open_gift_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Send Gift",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Heart Reaction Burst Button
                IconButton(
                    onClick = {
                        particles.add(
                            ReactionParticle(
                                id = System.currentTimeMillis(),
                                emoji = listOf("❤️", "🔥", "💖", "⭐", "🚀").random(),
                                startXOffset = Random.nextFloat() * 60f - 30f,
                                swayAmount = 25f
                            )
                        )
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1E1B4B))
                        .border(1.5.dp, NeonPink, CircleShape)
                        .testTag("burst_heart_reaction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Send Heart",
                        tint = NeonPink
                    )
                }
            }
        }

        // Gift Drawer Sheet
        if (showGiftDrawer) {
            GiftDrawer(
                gifts = gifts,
                coinsBalance = coinsBalance,
                onGiftSelected = { gift ->
                    val success = onSendGift(gift)
                    if (success) {
                        celebrationGiftName = "You sent ${gift.name} ${gift.iconEmoji} to ${stream.streamerName}!"
                        activeScreenWideGift = ActiveGiftAnimation(
                            gift = gift,
                            senderName = "You",
                            receiverName = stream.streamerName,
                            coinValue = gift.coinCost
                        )
                        // Burst particles
                        repeat(8) {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis() + it * 50,
                                    emoji = gift.iconEmoji,
                                    startXOffset = Random.nextFloat() * 100f - 50f,
                                    swayAmount = 30f
                                )
                            )
                        }
                    }
                },
                onTopUpCoins = onTopUpCoins,
                onDismiss = { showGiftDrawer = false }
            )
        }

        // Quality Switcher Dialog
        if (showQualityDialog) {
            val qualityOptions = listOf("1080p60 (Source)", "720p60 (High)", "480p30 (Data Saver)", "Auto (Adaptive)")
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text(text = "Stream Quality", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        qualityOptions.forEach { quality ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuality = quality
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedQuality == quality,
                                    onClick = {
                                        selectedQuality = quality
                                        showQualityDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonPink)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = quality,
                                    color = if (selectedQuality == quality) NeonPink else TextPrimary,
                                    fontWeight = if (selectedQuality == quality) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showQualityDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Close")
                    }
                },
                containerColor = Color(0xFF16152B)
            )
        }
        // Report User Dialog
        if (showReportDialog && selectedMessageToReport != null) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report User", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Are you sure you want to report this message?", color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("\"${selectedMessageToReport?.text}\"", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedMessageToReport?.let { msg ->
                                onReportUser(msg.senderName, msg.text)
                            }
                            showReportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                    ) {
                        Text("Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF132242)
            )
        }

        // Screen-wide gift animation overlay
        ScreenWideGiftAnimationOverlay(
            activeGift = activeScreenWideGift,
            onDismiss = { activeScreenWideGift = null }
        )
    }
}
