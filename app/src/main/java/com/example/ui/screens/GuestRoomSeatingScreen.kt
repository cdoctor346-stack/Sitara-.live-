package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.GiftItem
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
    modifier: Modifier = Modifier
) {
    // 1 Host + 8 Guest Seats
    val seats = remember {
        mutableStateListOf(
            RoomSeat(
                seatIndex = 0,
                label = "Host",
                occupantName = "Host",
                occupantAvatarRes = R.drawable.creator_moin,
                isOccupied = true,
                isMuted = false,
                diamondsReceived = 0,
                isSpeaking = true
            ),
            RoomSeat(seatIndex = 1, label = "Guest 1", isOccupied = false),
            RoomSeat(seatIndex = 2, label = "Guest 2", isOccupied = false),
            RoomSeat(seatIndex = 3, label = "Guest 3", isOccupied = false),
            RoomSeat(seatIndex = 4, label = "Guest 4", isOccupied = false),
            RoomSeat(seatIndex = 5, label = "Guest 5", isOccupied = false),
            RoomSeat(seatIndex = 6, label = "Guest 6", isOccupied = false),
            RoomSeat(seatIndex = 7, label = "Guest 7", isOccupied = false),
            RoomSeat(seatIndex = 8, label = "Guest 8", isOccupied = false)
        )
    }

    var selectedSeatForAction by remember { mutableStateOf<RoomSeat?>(null) }
    var userCurrentSeatIndex by remember { mutableStateOf<Int?>(null) }
    var isUserMuted by remember { mutableStateOf(false) }
    var showGiftDrawerForSeat by remember { mutableStateOf<RoomSeat?>(null) }
    var celebrationGiftText by remember { mutableStateOf<String?>(null) }
    var isEditingGuestName by remember { mutableStateOf(false) }
    var newGuestNameInput by remember { mutableStateOf("") }
    var isCameraOn by remember { mutableStateOf(false) }
    var isPkActive by remember { mutableStateOf(false) }
    var pkScoreBlue by remember { mutableStateOf(1250) }
    var pkScoreRed by remember { mutableStateOf(1420) }
    var activeScreenWideGift by remember { mutableStateOf<ActiveGiftAnimation?>(null) }

    // Floating reactions & Live Chat
    val particles = remember { mutableStateListOf<ReactionParticle>() }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(UUID.randomUUID().toString(), "LivePK", "Welcome to the 8-Guest Audio/Video Lounge! 🎙️", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "Maya Plays", "Hey everyone! Seats 3, 4, 6, and 8 are open. Tap to take seat!", isStreamer = true)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF091224))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with PK Battle toggle & title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(36.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Guest Room Seating Plan",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                Button(
                    onClick = { isPkActive = !isPkActive },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPkActive) Color(0xFFEF4444) else Color(0xFF7C3AED)),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isPkActive) "🛑 End PK" else "⚔️ PK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // PK Battle Score Banner if active
            if (isPkActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔵 Blue: $pkScoreBlue", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("PK BATTLE ⚔️", color = NeonPink, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Text("🔴 Red: $pkScoreRed", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // Cyan decorative line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0x0000C896), Color(0xFF00C896), Color(0x0000C896))
                        )
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Room Canvas / Architectural Seating Layout
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C172F)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1B2F58))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    // Room Flooring (Warm ivory beige floor from screenshot)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEFE9DB))
                            .border(1.5.dp, Color(0xFFD6CEBD), RoundedCornerShape(14.dp))
                    ) {
                        // Center Aisle Aisle Dashed Line with Direction Arrow pointing to entrance
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val startY = size.height * 0.28f
                            val endY = size.height * 0.88f

                            // Dashed aisle line
                            drawLine(
                                color = Color(0xFF00BFA5),
                                start = Offset(centerX, startY),
                                end = Offset(centerX, endY),
                                strokeWidth = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )

                            // Arrowhead at bottom of aisle
                            val arrowSize = 16f
                            val arrowY = endY
                            drawLine(
                                color = Color(0xFF00BFA5),
                                start = Offset(centerX - arrowSize, arrowY - arrowSize),
                                end = Offset(centerX, arrowY),
                                strokeWidth = 4.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xFF00BFA5),
                                start = Offset(centerX + arrowSize, arrowY - arrowSize),
                                end = Offset(centerX, arrowY),
                                strokeWidth = 4.dp.toPx()
                            )
                        }

                        // Room Interior Elements
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Stage / Desk with Host and Corner Plants
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left Corner Potted Plant
                                PottedPlantIcon()

                                // Host Desk & Executive Chair (Seat 0)
                                val hostSeat = seats[0]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedSeatForAction = hostSeat }
                                        .testTag("host_seat")
                                ) {
                                    // Host Executive Chair
                                    ChairWidget(
                                        seat = hostSeat,
                                        isHost = true,
                                        isSpeaking = hostSeat.isSpeaking,
                                        scaleModifier = if (hostSeat.isSpeaking) pulseScale else 1f
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Host Wooden Desk with Documents & Plant
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF132242))
                                            .border(1.dp, Color(0xFF243B6B), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🌿", fontSize = 11.sp)
                                            // Document paper on desk
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp, 18.dp)
                                                    .background(Color.White)
                                                    .border(0.5.dp, Color.LightGray)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // "Host" Badge Pill
                                    SeatLabelPill(
                                        text = hostSeat.occupantName ?: "Host",
                                        isHost = true,
                                        isOccupied = hostSeat.isOccupied
                                    )
                                }

                                // Right Corner Potted Plant
                                PottedPlantIcon()
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 8 Guest Chairs (4 Left, 4 Right) facing center aisle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Column: Guest 1, Guest 2, Guest 3, Guest 4
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (i in 1..4) {
                                        val seat = seats[i]
                                        GuestSeatRowItem(
                                            seat = seat,
                                            onClick = { selectedSeatForAction = seat }
                                        )
                                    }
                                }

                                // Center Aisle Spacer
                                Spacer(modifier = Modifier.width(36.dp))

                                // Right Column: Guest 5, Guest 6, Guest 7, Guest 8
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (i in 5..8) {
                                        val seat = seats[i]
                                        GuestSeatRowItem(
                                            seat = seat,
                                            onClick = { selectedSeatForAction = seat }
                                        )
                                    }
                                }
                            }

                            // Entrance Door at bottom
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 20.dp)
                                    .border(1.5.dp, Color(0xFF1E355F), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(text = "🚪", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Interactive Bar (Chat, Mic, Gift, Actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat text input
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Chat in guest room...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF2A3D66),
                        focusedContainerColor = Color(0xFF0F1A34),
                        unfocusedContainerColor = Color(0xFF0F1A34)
                    ),
                    shape = RoundedCornerShape(23.dp)
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonPink)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mic toggle
                IconButton(
                    onClick = {
                        isUserMuted = !isUserMuted
                        userCurrentSeatIndex?.let { idx ->
                            seats[idx] = seats[idx].copy(isMuted = isUserMuted)
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
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

                // Camera On/Off toggle
                IconButton(
                    onClick = {
                        isCameraOn = !isCameraOn
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isCameraOn) Color(0xFF3B82F6) else Color(0xFF475569))
                ) {
                    Icon(
                        imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Camera",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Gift Button
                IconButton(
                    onClick = {
                        showGiftDrawerForSeat = seats[0] // Gift to host by default
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonPink, NeonAmber)))
                ) {
                    Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = "Gift", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Floating Hearts & Particles
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

        // Seat Interaction Dialog (Sit down / Mute / Send Gift / Leave Seat)
        if (selectedSeatForAction != null) {
            val seat = selectedSeatForAction!!
            AlertDialog(
                onDismissRequest = { selectedSeatForAction = null },
                title = {
                    Text(
                        text = "${seat.label} Details",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (seat.isOccupied) {
                            if (isEditingGuestName) {
                                Text(
                                    text = "Change Guest Name",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newGuestNameInput,
                                    onValueChange = { newGuestNameInput = it },
                                    label = { Text("New Guest Name") },
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
                                                seats[seat.seatIndex] = seat.copy(occupantName = newGuestNameInput.trim())
                                            }
                                            isEditingGuestName = false
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF132242)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "👤", fontSize = 24.sp)
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
                                            text = "Diamonds Received: ${seat.diamondsReceived} 💎",
                                            color = NeonAmber,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action buttons
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
                                            Text("Send Gift 🎁", fontSize = 12.sp)
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
                                            Text("Change Guest ✏️", fontSize = 12.sp)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                seats[seat.seatIndex] = seat.copy(isMuted = !seat.isMuted)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) Color(0xFFEF4444) else Color(0xFF2A4275)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (seat.isMuted) "Unmute 🔊" else "Mute 🔇", fontSize = 12.sp)
                                        }

                                        if (userCurrentSeatIndex == seat.seatIndex || seat.seatIndex != 0) {
                                            Button(
                                                onClick = {
                                                    seats[seat.seatIndex] = seat.copy(
                                                        isOccupied = false,
                                                        occupantName = null
                                                    )
                                                    if (userCurrentSeatIndex == seat.seatIndex) {
                                                        userCurrentSeatIndex = null
                                                    }
                                                    selectedSeatForAction = null
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(if (userCurrentSeatIndex == seat.seatIndex) "Leave Seat" else "Remove Guest", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Seat is empty: Option to sit down
                            Text(
                                text = "This chair is currently empty. Would you like to take this seat and speak?",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    // Vacate previous seat if any
                                    userCurrentSeatIndex?.let { prevIdx ->
                                        seats[prevIdx] = seats[prevIdx].copy(isOccupied = false, occupantName = null)
                                    }
                                    // Sit on new seat
                                    seats[seat.seatIndex] = seat.copy(
                                        isOccupied = true,
                                        occupantName = "You",
                                        isSpeaking = true
                                    )
                                    userCurrentSeatIndex = seat.seatIndex
                                    selectedSeatForAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Take Seat & Speak 🎙️", color = Color.White, fontWeight = FontWeight.Bold)
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

        // Gift Drawer for sending to specific seat occupant
        if (showGiftDrawerForSeat != null) {
            val targetSeat = showGiftDrawerForSeat!!
            GiftDrawer(
                gifts = availableGifts,
                coinsBalance = coinsBalance,
                onGiftSelected = { gift ->
                    if (coinsBalance >= gift.coinCost) {
                        onCoinsDeducted(gift.coinCost)
                        // Update target seat diamonds
                        seats[targetSeat.seatIndex] = targetSeat.copy(
                            diamondsReceived = targetSeat.diamondsReceived + (gift.coinCost / 2)
                        )
                        celebrationGiftText = "You sent ${gift.name} ${gift.iconEmoji} to ${targetSeat.occupantName ?: targetSeat.label}!"
                        
                        // Add gift message to chat
                        chatMessages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                senderName = "You",
                                text = "🎁 Sent ${gift.name} ${gift.iconEmoji} to ${targetSeat.occupantName ?: targetSeat.label}!"
                            )
                        )

                        // Trigger screen-wide overlay animation
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

// Visual Chair Widget matching the dark navy armchairs in the screenshot
@Composable
private fun ChairWidget(
    seat: RoomSeat,
    isHost: Boolean = false,
    isSpeaking: Boolean = false,
    scaleModifier: Float = 1f
) {
    Box(
        modifier = Modifier
            .size(if (isHost) 48.dp else 40.dp)
            .scale(scaleModifier),
        contentAlignment = Alignment.Center
    ) {
        // Chair Armchair Shape (Dark Navy with rounded backrest)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF132242))
                .border(
                    width = if (isSpeaking) 2.dp else 1.dp,
                    color = if (isSpeaking) Color(0xFF00C896) else Color(0xFF2A4275),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (seat.isOccupied) {
                // If occupied, show avatar or initials / shield for co-host
                Text(
                    text = if (isHost) "👑" else if (seat.isCoHost) "🛡️" else "👤",
                    fontSize = if (isHost) 20.sp else 16.sp
                )
            } else {
                // Empty chair silhouette
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
            }
        }

        // Speaking indicator green dot
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00FF88))
                    .align(Alignment.TopEnd)
            )
        }
    }
}

// Seat Row Item with Chair and Teal Pill Label
@Composable
private fun GuestSeatRowItem(
    seat: RoomSeat,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag("seat_${seat.seatIndex}")
    ) {
        ChairWidget(seat = seat, isSpeaking = seat.isSpeaking)
        Spacer(modifier = Modifier.height(2.dp))
        SeatLabelPill(
            text = seat.occupantName ?: seat.label,
            isHost = false,
            isOccupied = seat.isOccupied
        )
    }
}

// Teal / Cyan Label Pill matching the screenshot
@Composable
private fun SeatLabelPill(
    text: String,
    isHost: Boolean,
    isOccupied: Boolean
) {
    Box(
        modifier = Modifier
            .width(66.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isHost) Color(0xFF132242) else Color(0xFF008E8A))
            .border(
                width = 0.5.dp,
                color = if (isHost) Color(0xFF2A4275) else Color(0xFF00BFA5),
                shape = RoundedCornerShape(9.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// Corner Potted Plant Widget
@Composable
private fun PottedPlantIcon() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0x22132242)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🪴", fontSize = 20.sp)
    }
}

private fun RoomSeat(seatIndex: Int, label: String, labelText: String) = RoomSeat(
    seatIndex = seatIndex,
    label = label,
    isOccupied = false
)
