package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.GiftItem
import com.example.ui.util.ShareHelper
import com.example.ui.components.ActiveGiftAnimation
import com.example.ui.components.CelebrationGiftBanner
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.GiftDrawer
import com.example.ui.components.ReactionParticle
import com.example.ui.components.ScreenWideGiftAnimationOverlay
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

data class RoomSeat(
    val seatIndex: Int,
    val label: String,
    val occupantName: String? = null,
    val occupantAvatarRes: Int? = null,
    val isOccupied: Boolean = false,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val diamondsReceived: Int = 0,
    val isSpeaking: Boolean = false,
    val isCoHost: Boolean = false
)

@Composable
fun GuestRoomSeatingScreen(
    coinsBalance: Int,
    onCoinsDeducted: (Int) -> Unit,
    onTopUpCoins: () -> Unit,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // All 8 Guest Seats initialized fresh and completely empty (old guests deleted)
    val seats = remember {
        mutableStateListOf(
            RoomSeat(seatIndex = 1, label = "Seat 1", isOccupied = false),
            RoomSeat(seatIndex = 2, label = "Seat 2", isOccupied = false),
            RoomSeat(seatIndex = 3, label = "Seat 3", isOccupied = false),
            RoomSeat(seatIndex = 4, label = "Seat 4", isOccupied = false),
            RoomSeat(seatIndex = 5, label = "Seat 5", isOccupied = false),
            RoomSeat(seatIndex = 6, label = "Seat 6", isOccupied = false),
            RoomSeat(seatIndex = 7, label = "Seat 7", isOccupied = false),
            RoomSeat(seatIndex = 8, label = "Seat 8", isOccupied = false)
        )
    }

    var selectedSeatForAction by remember { mutableStateOf<RoomSeat?>(null) }
    var userCurrentSeatIndex by remember { mutableStateOf<Int?>(null) }
    var isUserMuted by remember { mutableStateOf(false) }
    var showGiftDrawerForSeat by remember { mutableStateOf<RoomSeat?>(null) }
    var isAddingNewGuestToSeat by remember { mutableStateOf(false) }
    var customGuestNameInput by remember { mutableStateOf("") }
    var celebrationGiftText by remember { mutableStateOf<String?>(null) }
    var isEditingGuestName by remember { mutableStateOf(false) }
    var newGuestNameInput by remember { mutableStateOf("") }
    var isCameraOn by remember { mutableStateOf(false) }
    var isPkActive by remember { mutableStateOf(false) }
    var pkScoreBlue by remember { mutableStateOf(2450) }
    var pkScoreRed by remember { mutableStateOf(1890) }
    var activeScreenWideGift by remember { mutableStateOf<ActiveGiftAnimation?>(null) }
    var hostDiamonds by remember { mutableStateOf(42800) }

    // Floating reactions & Live Chat
    val particles = remember { mutableStateListOf<ReactionParticle>() }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(UUID.randomUUID().toString(), "LivePK", "Welcome to Sitara Live 8-Guest Room! 🎙️", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "Sitara Streamer", "Tap any '+' circle to sit or add a new guest!", isStreamer = true)
        )
    }
    var chatInput by remember { mutableStateOf("") }

    val availableGifts = listOf(
        GiftItem("gift_rose", "Neon Rose", "🌹", 5),
        GiftItem("gift_heart", "Cyber Heart", "💖", 20),
        GiftItem("gift_mic", "Golden Mic", "🎤", 50),
        GiftItem("gift_game", "Pro Controller", "🎮", 100),
        GiftItem("gift_crown", "Royal Crown", "👑", 500),
        GiftItem("gift_rocket", "Space Rocket", "🚀", 1000)
    )

    // Pulse animation for speaking guests
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse"
    )

    val speakingGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    // Audio Equalizer wave height animation
    val eqHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "eq1"
    )
    val eqHeight2 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "eq2"
    )
    val eqHeight3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "eq3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090A1A),
                        Color(0xFF0C1026),
                        Color(0xFF070B18)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= 1. TOP HEADER BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Host Room Capsule (Avatar + Name + ID + Follow + Diamonds)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x551E293B))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                        .padding(start = 3.dp, end = 10.dp, top = 3.dp, bottom = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.creator_moin),
                            contentDescription = "Host Avatar",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, NeonCyan, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Host Sitara ⭐",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonPink)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "+",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💎 ${(hostDiamonds / 1000f).let { String.format("%.1fk", it) }}",
                                    color = NeonAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ID: 882910",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // Top Right: Live Viewer Bubbles + PK Button + Close/Back
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Viewer Count Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x44000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Viewers",
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "528",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // PK Battle Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isPkActive) Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
                                else Brush.horizontalGradient(listOf(Color(0xFF7C3AED), NeonPink))
                            )
                            .clickable { isPkActive = !isPkActive }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isPkActive) "🛑 END PK" else "⚔️ PK",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Share Live Room & APK Button
                    IconButton(
                        onClick = {
                            ShareHelper.shareLiveStream(
                                context = context,
                                hostName = "Host Sitara",
                                streamTitle = "8-Guest Lounge & PK Battle",
                                isPk = isPkActive
                            )
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC1E1B4B))
                            .border(1.dp, NeonPink, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Room & APK",
                            tint = NeonPink,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Close/Back Button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0x44000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ================= 2. PK BATTLE BAR (IF ACTIVE) =================
            if (isPkActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13172E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔵 Blue: $pkScoreBlue", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPink)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PK 03:45", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔴 Red: $pkScoreRed", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ================= 3. HOST LIVE BROADCAST WINDOW (ROUNDED CARD) =================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x4400C896))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Host Video/Cover Image
                    Image(
                        painter = painterResource(id = R.drawable.creator_moin),
                        contentDescription = "Host Live Stream",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x55000000),
                                        Color(0x00000000),
                                        Color(0x99000000)
                                    )
                                )
                            )
                    )

                    // Top Left LIVE Badge inside Host Video
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Bottom Bar inside Host Video (Host Name, Audio Wave Equalizer, Mic)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Host Name Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x88000000))
                                    .border(0.5.dp, Color(0x55FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑 Host", color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sitara", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Equalizer Audio Wave bars
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(modifier = Modifier.width(3.dp).height(eqHeight1.dp).background(NeonCyan, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(3.dp).height(eqHeight2.dp).background(Color(0xFF00FF88), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(3.dp).height(eqHeight3.dp).background(NeonCyan, RoundedCornerShape(1.dp)))
                            }
                        }

                        // Host Mic Active Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0x8800C896)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Host Mic",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================= 4. GUEST SEATS GRID (EXACTLY AS IN VIDEO) =================
            // 8 Seats in 2 Rows of 4 Columns:
            // Row 1: Seat 1, Seat 2, Seat 3, Seat 4
            // Row 2: Seat 5, Seat 6, Seat 7, Seat 8
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0x221E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Bar inside Seats Card: Title + Add Guest + Clear All
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎙️ 8-GUEST SEATS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val occupiedCount = seats.count { it.isOccupied }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3300FF88))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$occupiedCount/8 Joined",
                                    color = Color(0xFF00FF88),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "Add Guest" Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF00C896))
                                    .clickable {
                                        // Find first empty seat
                                        val firstEmpty = seats.firstOrNull { !it.isOccupied }
                                        if (firstEmpty != null) {
                                            selectedSeatForAction = firstEmpty
                                            isAddingNewGuestToSeat = true
                                            customGuestNameInput = ""
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "+ Add Guest",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // "Delete All Guests" button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x44EF4444))
                                    .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                    .clickable {
                                        for (idx in seats.indices) {
                                            seats[idx] = seats[idx].copy(
                                                isOccupied = false,
                                                occupantName = null,
                                                occupantAvatarRes = null,
                                                diamondsReceived = 0,
                                                isSpeaking = false,
                                                isMuted = false
                                            )
                                        }
                                        userCurrentSeatIndex = null
                                        chatMessages.add(
                                            ChatMessage(
                                                id = UUID.randomUUID().toString(),
                                                senderName = "Host",
                                                text = "🧹 All guest seats have been reset & cleared!",
                                                isModerator = true
                                            )
                                        )
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🗑️ Clear All",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Row 1: Seats 1..4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (i in 0..3) {
                            val seat = seats[i]
                            ModernGuestSeatItem(
                                seat = seat,
                                pulseScale = if (seat.isSpeaking) pulseScale else 1f,
                                glowAlpha = if (seat.isSpeaking) speakingGlowAlpha else 0f,
                                onClick = { selectedSeatForAction = seat }
                            )
                        }
                    }

                    // Row 2: Seats 5..8
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (i in 4..7) {
                            val seat = seats[i]
                            ModernGuestSeatItem(
                                seat = seat,
                                pulseScale = if (seat.isSpeaking) pulseScale else 1f,
                                glowAlpha = if (seat.isSpeaking) speakingGlowAlpha else 0f,
                                onClick = { selectedSeatForAction = seat }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= 5. LIVE CHAT STREAM OVERLAY =================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 50.dp), // Leaves room for floating reaction hearts
                    verticalArrangement = Arrangement.Bottom,
                    reverseLayout = true
                ) {
                    items(chatMessages.reversed()) { msg ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x660F172A))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (msg.isStreamer) {
                                    Text(
                                        text = "HOST ",
                                        color = NeonAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else if (msg.isModerator) {
                                    Text(
                                        text = "SYS ",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(
                                    text = "${msg.senderName}: ",
                                    color = if (msg.isStreamer) NeonPink else NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= 6. BOTTOM CONTROL & ACTION BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat input pill
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Say something...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF2A3D66),
                        focusedContainerColor = Color(0x880F1A34),
                        unfocusedContainerColor = Color(0x880F1A34)
                    ),
                    shape = RoundedCornerShape(22.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send comment button
                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            chatMessages.add(
                                ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    senderName = "You",
                                    text = chatInput
                                )
                            )
                            chatInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonPink)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mic toggle button (controls user's seat mic)
                IconButton(
                    onClick = {
                        isUserMuted = !isUserMuted
                        userCurrentSeatIndex?.let { idx ->
                            val sIdx = idx - 1 // 1-based index mapped to seats array
                            if (sIdx in seats.indices) {
                                seats[sIdx] = seats[sIdx].copy(isMuted = isUserMuted, isSpeaking = !isUserMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isUserMuted) Color(0xFFEF4444) else Color(0xFF00C896))
                ) {
                    Icon(
                        imageVector = if (isUserMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Floating Hearts Reaction Button
                IconButton(
                    onClick = {
                        repeat(5) {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis() + it * 60,
                                    emoji = listOf("❤️", "💖", "🔥", "✨", "💎").random(),
                                    startXOffset = Random.nextFloat() * 80f - 40f,
                                    swayAmount = 30f
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x44FF2A85))
                        .border(1.dp, NeonPink, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = NeonPink,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Big Glowing Gift Box Button
                IconButton(
                    onClick = {
                        showGiftDrawerForSeat = seats[0] // Default target seat 1
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonPink, NeonAmber, NeonCyan)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Send Gift",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Floating Hearts & Reaction Particles
        FloatingReactionsContainer(
            particles = particles,
            onParticleFinished = { id -> particles.removeAll { it.id == id } }
        )

        // Celebration Announcement Banner
        CelebrationGiftBanner(
            giftTitle = celebrationGiftText,
            onDismiss = { celebrationGiftText = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        )

        // ================= 7. SEAT INTERACTION DIALOG =================
        if (selectedSeatForAction != null) {
            val seat = selectedSeatForAction!!
            AlertDialog(
                onDismissRequest = { selectedSeatForAction = null },
                title = {
                    Text(
                        text = if (seat.isOccupied) "${seat.occupantName} (${seat.label})" else "Take ${seat.label}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column {
                        if (seat.isOccupied) {
                            if (isEditingGuestName) {
                                Text(
                                    text = "Change Guest Name",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newGuestNameInput,
                                    onValueChange = { newGuestNameInput = it },
                                    label = { Text("Guest Name") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color(0xFF2A4275),
                                        focusedLabelColor = NeonCyan,
                                        unfocusedLabelColor = TextSecondary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (newGuestNameInput.isNotBlank()) {
                                                val idx = seat.seatIndex - 1
                                                if (idx in seats.indices) {
                                                    seats[idx] = seat.copy(occupantName = newGuestNameInput.trim())
                                                }
                                            }
                                            isEditingGuestName = false
                                            selectedSeatForAction = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save")
                                    }
                                    Button(
                                        onClick = { isEditingGuestName = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A4275)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(NeonCyan, Color(0xFF7C3AED))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🎙️", fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = seat.occupantName ?: "Guest",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Received: ${seat.diamondsReceived} 💎",
                                            color = NeonAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                showGiftDrawerForSeat = seat
                                                selectedSeatForAction = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Send Gift 🎁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                newGuestNameInput = seat.occupantName ?: ""
                                                isEditingGuestName = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008E8A)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Edit Name ✏️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val idx = seat.seatIndex - 1
                                                if (idx in seats.indices) {
                                                    seats[idx] = seat.copy(isMuted = !seat.isMuted)
                                                }
                                                selectedSeatForAction = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) Color(0xFF00C896) else Color(0xFF2A4275)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (seat.isMuted) "Unmute 🔊" else "Mute 🔇", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val idx = seat.seatIndex - 1
                                                if (idx in seats.indices) {
                                                    seats[idx] = seat.copy(
                                                        isOccupied = false,
                                                        occupantName = null,
                                                        isSpeaking = false
                                                    )
                                                }
                                                if (userCurrentSeatIndex == seat.seatIndex) {
                                                    userCurrentSeatIndex = null
                                                }
                                                selectedSeatForAction = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (userCurrentSeatIndex == seat.seatIndex) "Leave Seat" else "Remove", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Seat is empty: Options to sit down OR add a guest by name
                            Text(
                                text = "Take this seat yourself or add a new guest to ${seat.label}:",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customGuestNameInput,
                                onValueChange = { customGuestNameInput = it },
                                placeholder = { Text("Enter guest name (or blank for You)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0xFF2A4275),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "Sit Here (You)" button
                                Button(
                                    onClick = {
                                        // Vacate previous seat if any
                                        userCurrentSeatIndex?.let { prevIdx ->
                                            val pIdx = prevIdx - 1
                                            if (pIdx in seats.indices) {
                                                seats[pIdx] = seats[pIdx].copy(isOccupied = false, occupantName = null, isSpeaking = false)
                                            }
                                        }
                                        val assignedName = if (customGuestNameInput.isNotBlank()) customGuestNameInput.trim() else "You"
                                        val idx = seat.seatIndex - 1
                                        if (idx in seats.indices) {
                                            seats[idx] = seat.copy(
                                                isOccupied = true,
                                                occupantName = assignedName,
                                                isSpeaking = true,
                                                isMuted = isUserMuted
                                            )
                                        }
                                        userCurrentSeatIndex = seat.seatIndex
                                        selectedSeatForAction = null
                                        customGuestNameInput = ""

                                        chatMessages.add(
                                            ChatMessage(
                                                id = UUID.randomUUID().toString(),
                                                senderName = "LivePK",
                                                text = "🎉 $assignedName joined ${seat.label}!",
                                                isModerator = true
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sit as You 🎙️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // "Add New Guest" button
                                Button(
                                    onClick = {
                                        val guestName = if (customGuestNameInput.isNotBlank()) customGuestNameInput.trim() else "Guest ${seat.seatIndex}"
                                        val idx = seat.seatIndex - 1
                                        if (idx in seats.indices) {
                                            seats[idx] = seat.copy(
                                                isOccupied = true,
                                                occupantName = guestName,
                                                isSpeaking = false,
                                                isMuted = false,
                                                diamondsReceived = 100
                                            )
                                        }
                                        selectedSeatForAction = null
                                        customGuestNameInput = ""

                                        chatMessages.add(
                                            ChatMessage(
                                                id = UUID.randomUUID().toString(),
                                                senderName = "Host",
                                                text = "✨ Added new guest $guestName to ${seat.label}!",
                                                isModerator = true
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("+ Add Guest", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedSeatForAction = null }) {
                        Text("Close", color = Color.White)
                    }
                },
                containerColor = Color(0xFF132242)
            )
        }

        // ================= 8. GIFT DRAWER =================
        if (showGiftDrawerForSeat != null) {
            val targetSeat = showGiftDrawerForSeat!!
            GiftDrawer(
                gifts = availableGifts,
                coinsBalance = coinsBalance,
                onGiftSelected = { gift ->
                    if (coinsBalance >= gift.coinCost) {
                        onCoinsDeducted(gift.coinCost)
                        val idx = targetSeat.seatIndex - 1
                        if (idx in seats.indices) {
                            seats[idx] = targetSeat.copy(
                                diamondsReceived = targetSeat.diamondsReceived + (gift.coinCost / 2)
                            )
                        }
                        hostDiamonds += gift.coinCost / 2
                        celebrationGiftText = "You sent ${gift.name} ${gift.iconEmoji} to ${targetSeat.occupantName ?: targetSeat.label}!"

                        chatMessages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                senderName = "You",
                                text = "🎁 Sent ${gift.name} ${gift.iconEmoji} to ${targetSeat.occupantName ?: targetSeat.label}!"
                            )
                        )

                        activeScreenWideGift = ActiveGiftAnimation(
                            gift = gift,
                            senderName = "You",
                            receiverName = targetSeat.occupantName ?: targetSeat.label,
                            coinValue = gift.coinCost
                        )

                        repeat(8) {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis() + it * 50,
                                    emoji = gift.iconEmoji,
                                    startXOffset = Random.nextFloat() * 120f - 60f,
                                    swayAmount = 25f
                                )
                            )
                        }
                    }
                },
                onTopUpCoins = onTopUpCoins,
                onDismiss = { showGiftDrawerForSeat = null }
            )
        }

        // Screen-wide gift overlay animation
        ScreenWideGiftAnimationOverlay(
            activeGift = activeScreenWideGift,
            onDismiss = { activeScreenWideGift = null }
        )
    }
}

// ===================================================================
// MODERN GUEST SEAT ITEM (CIRCULAR SEAT WITH TEAL '+' & 'Seat X' PILL)
// Exactly matching the video style from live broadcast apps!
// ===================================================================
@Composable
private fun ModernGuestSeatItem(
    seat: RoomSeat,
    pulseScale: Float,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("seat_${seat.seatIndex}")
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            if (seat.isOccupied) {
                // Animated speaking ring when guest is speaking
                if (seat.isSpeaking) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF88).copy(alpha = glowAlpha * 0.35f))
                            .border(2.dp, Color(0xFF00FF88), CircleShape)
                    )
                }

                // Occupied Avatar Disc
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFEC4899)
                                )
                            )
                        )
                        .border(
                            width = if (seat.isSpeaking) 2.dp else 1.5.dp,
                            color = if (seat.isSpeaking) Color(0xFF00FF88) else Color(0x88FFFFFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (seat.occupantAvatarRes != null) {
                        Image(
                            painter = painterResource(id = seat.occupantAvatarRes),
                            contentDescription = seat.occupantName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = seat.occupantName?.take(1)?.uppercase() ?: "👤",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Top-Right Diamond Badge (e.g. 💎 520)
                if (seat.diamondsReceived > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${seat.diamondsReceived}💎",
                            color = NeonAmber,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom-Right Mic Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (seat.isMuted) Color(0xFFEF4444) else Color(0xFF00C896))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            } else {
                // ================= EMPTY SEAT =================
                // Dark glass circular seat with glowing teal/cyan border
                // and vibrant teal '+' icon in the center (AS SHOWN IN USER VIDEO!)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x33101E38))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF00C896),
                                    Color(0xFF00BFA5)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Glowing Teal '+' Icon
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Take Seat",
                        tint = Color(0xFF00FF88),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Seat Pill Label underneath (e.g., "Seat 1", "Seat 7", or Occupant Name)
        Box(
            modifier = Modifier
                .width(62.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    if (seat.isOccupied) Color(0xCC1E293B)
                    else Color(0x660B1528)
                )
                .border(
                    width = 0.5.dp,
                    color = if (seat.isOccupied) Color(0x8800E5FF) else Color(0x4400C896),
                    shape = RoundedCornerShape(9.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (seat.isOccupied) (seat.occupantName ?: seat.label) else seat.label,
                color = if (seat.isOccupied) Color.White else Color(0xFF00FF88),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
