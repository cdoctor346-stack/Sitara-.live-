package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneInTalk
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ConnectedGuest
import com.example.data.model.GiftItem
import com.example.data.model.LiveBroadcastMode
import com.example.data.model.PkBattleState
import com.example.data.model.StreamItem
import com.example.ui.components.ActiveGiftAnimation
import com.example.ui.components.CelebrationGiftBanner
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.GiftDrawer
import com.example.ui.components.LiveChatOverlay
import com.example.ui.components.ReactionParticle
import com.example.ui.components.ScreenWideGiftAnimationOverlay
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.ShareHelper
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
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var showGiftDrawer by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("1080p60 (Source)") }
    var celebrationGiftName by remember { mutableStateOf<String?>(null) }
    var activeScreenWideGift by remember { mutableStateOf<ActiveGiftAnimation?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedMessageToReport by remember { mutableStateOf<ChatMessage?>(null) }
    var showGuestCallRequestDialog by remember { mutableStateOf(false) }

    // Stream Mode in View (defaults to PK BATTLE for peak TikTok experience)
    var viewerStreamMode by remember { mutableStateOf(LiveBroadcastMode.PK_BATTLE) }

    // PK Battle state
    var pkState by remember {
        mutableStateOf(
            PkBattleState(
                challengerName = "Sarah Queen 👑",
                challengerAvatarRes = R.drawable.img_pk_queen,
                blueScore = 5230,
                redScore = 4890,
                timeLeftSeconds = 165,
                isBoostRound = true
            )
        )
    }

    // Connected guests for Guest Call mode
    val viewerConnectedGuests = remember {
        listOf(
            ConnectedGuest(id = "g1", name = "Hamza 🎙️", avatarRes = R.drawable.img_guest_streamer, isSpeaking = true),
            ConnectedGuest(id = "g2", name = "Ayesha 📹", avatarRes = R.drawable.img_pk_queen, isSpeaking = false)
        )
    }

    var dynamicViewerCount by remember(stream.viewerCount) { mutableIntStateOf(stream.viewerCount) }
    val particles = remember { mutableStateListOf<ReactionParticle>() }
    var tapComboCount by remember { mutableIntStateOf(0) }

    // Audio wave equalizer
    val infiniteTransition = rememberInfiniteTransition(label = "viewer_eq")
    val eq1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "veq1"
    )
    val eq2 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "veq2"
    )
    val eq3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "veq3"
    )

    // Fluctuating viewers & ambient background hearts + PK timer
    LaunchedEffect(stream.id) {
        while (true) {
            delay(1000)
            if (viewerStreamMode == LiveBroadcastMode.PK_BATTLE && pkState.timeLeftSeconds > 0) {
                val nextTime = pkState.timeLeftSeconds - 1
                // Dynamic gift simulation on scores
                val redAdd = if (Random.nextInt(5) == 0) Random.nextInt(30, 150) else 0
                val blueAdd = if (Random.nextInt(4) == 0) Random.nextInt(40, 180) else 0
                pkState = pkState.copy(
                    timeLeftSeconds = nextTime,
                    blueScore = pkState.blueScore + blueAdd,
                    redScore = pkState.redScore + redAdd
                )
            }
            if (Random.nextInt(4) == 0) {
                dynamicViewerCount = (dynamicViewerCount + Random.nextInt(-8, 15)).coerceAtLeast(120)
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

    val pkRemainingFormatted = remember(pkState.timeLeftSeconds) {
        val mins = pkState.timeLeftSeconds / 60
        val secs = pkState.timeLeftSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        tapComboCount++
                        particles.add(
                            ReactionParticle(
                                id = System.currentTimeMillis(),
                                emoji = listOf("💖", "❤️", "🔥", "✨").random(),
                                startXOffset = Random.nextFloat() * 80f - 40f,
                                swayAmount = 25f
                            )
                        )
                    },
                    onDoubleTap = {
                        tapComboCount += 5
                        repeat(3) {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis() + it * 40,
                                    emoji = "💖",
                                    startXOffset = Random.nextFloat() * 80f - 40f,
                                    swayAmount = 30f
                                )
                            )
                        }
                    }
                )
            }
    ) {
        // ================= VIDEO FEED CANVAS =================
        when (viewerStreamMode) {
            LiveBroadcastMode.SOLO -> {
                Image(
                    painter = painterResource(id = stream.bannerRes ?: R.drawable.stream_hero),
                    contentDescription = "Live Video Feed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            LiveBroadcastMode.PK_BATTLE -> {
                // TikTok 1v1 Split Screen (Left: Host Streamer, Right: Challenger)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Host (Team Blue)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
                    ) {
                        Image(
                            painter = painterResource(id = stream.bannerRes ?: R.drawable.creator_moin),
                            contentDescription = stream.streamerName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0x33000000), Color.Transparent, Color(0x99000000))))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC003B5C))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(stream.streamerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Right Side: PK Challenger (Team Red)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.5.dp, Color(0xFFFF2A85).copy(alpha = 0.5f))
                    ) {
                        Image(
                            painter = painterResource(id = pkState.challengerAvatarRes.takeIf { it != 0 } ?: R.drawable.img_pk_queen),
                            contentDescription = pkState.challengerName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0x33000000), Color.Transparent, Color(0x99000000))))
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xCC5C0028))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(pkState.challengerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.width(2.5.dp).height(eq1.dp).background(NeonPink, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.5.dp).height(eq2.dp).background(Color(0xFFFF80B0), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.5.dp).height(eq3.dp).background(NeonPink, RoundedCornerShape(1.dp)))
                            }
                        }
                    }
                }
            }

            LiveBroadcastMode.GUEST_CALL -> {
                // Multi-Guest Layout: Host Top, Connected Guest Boxes Bottom
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f)
                    ) {
                        Image(
                            painter = painterResource(id = stream.bannerRes ?: R.drawable.stream_hero),
                            contentDescription = stream.streamerName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Connected Guests
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .background(Color(0xFF0F172A))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewerConnectedGuests.forEach { guest ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (guest.isSpeaking) Color(0xFF00FF88) else Color(0x33FFFFFF))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = guest.avatarRes),
                                        contentDescription = guest.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color(0xCC000000))
                                            .padding(vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(guest.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // "Request to Join Call" Box for viewer
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { showGuestCallRequestDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x22334155)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhoneInTalk, contentDescription = "Request Call", tint = NeonCyan, modifier = Modifier.size(24.dp))
                                    Text("Join Call 🎙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gradient lighting overlay
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

        // Celebration Gift Announcement Banner
        CelebrationGiftBanner(
            giftTitle = celebrationGiftName,
            onDismiss = { celebrationGiftName = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // ================= TOP STREAMER & CONTROLS BAR =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                    // Follow button
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

                // Top Action Controls: Agora, Quality, Share & Close
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Share Live & APK
                    IconButton(
                        onClick = {
                            ShareHelper.shareLiveStream(
                                context = context,
                                hostName = stream.streamerName,
                                streamTitle = stream.title,
                                isPk = viewerStreamMode == LiveBroadcastMode.PK_BATTLE
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC111827))
                            .border(1.dp, NeonPink, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Stream & APK",
                            tint = NeonPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

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
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= TIKTOK VIEWER MODE TOGGLE PILLS =================
            // Allows the user to preview Solo, PK Battle, or Multi-Guest mode
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC111827))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (viewerStreamMode == LiveBroadcastMode.PK_BATTLE) Brush.horizontalGradient(listOf(NeonCyan, NeonPink)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                        .clickable { viewerStreamMode = LiveBroadcastMode.PK_BATTLE }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚔️ PK Battle", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (viewerStreamMode == LiveBroadcastMode.GUEST_CALL) Color(0xFF00FF88).copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { viewerStreamMode = LiveBroadcastMode.GUEST_CALL }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("👥 Multi-Guest", color = if (viewerStreamMode == LiveBroadcastMode.GUEST_CALL) Color(0xFF00FF88) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (viewerStreamMode == LiveBroadcastMode.SOLO) NeonCyan.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { viewerStreamMode = LiveBroadcastMode.SOLO }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("📱 Solo", color = if (viewerStreamMode == LiveBroadcastMode.SOLO) NeonCyan else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ================= PK BATTLE TUG-OF-WAR BAR IN VIEWER =================
            if (viewerStreamMode == LiveBroadcastMode.PK_BATTLE) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0B0F19)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔵 Blue: ${pkState.blueScore}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonPink)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PK $pkRemainingFormatted", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Text("🔴 Red: ${pkState.redScore}", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Score ratio bar
                        val total = (pkState.blueScore + pkState.redScore).coerceAtLeast(1)
                        val blueFraction = (pkState.blueScore.toFloat() / total).coerceIn(0.15f, 0.85f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(blueFraction)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF0055FF), NeonCyan)))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f - blueFraction)
                                    .background(Brush.horizontalGradient(listOf(NeonPink, Color(0xFFFF0055))))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Viewer PK Cheer buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3300F0FF))
                                    .clickable {
                                        pkState = pkState.copy(blueScore = pkState.blueScore + 100)
                                        repeat(4) {
                                            particles.add(ReactionParticle(System.currentTimeMillis() + it, "🔵", 0f, 20f))
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("🔵 Support Blue (+100)", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33FF2A85))
                                    .clickable {
                                        pkState = pkState.copy(redScore = pkState.redScore + 100)
                                        repeat(4) {
                                            particles.add(ReactionParticle(System.currentTimeMillis() + it, "🔴", 0f, 20f))
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("🔴 Support Red (+100)", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ================= BOTTOM CHAT & INTERACTION BAR =================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            // Live Chat Overlay
            LiveChatOverlay(
                messages = messages,
                onMessageClick = { clickedMessage ->
                    selectedMessageToReport = clickedMessage
                    showReportDialog = true
                },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat input
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Send comment...", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        if (chatInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSendMessage(chatInput)
                                    chatInput = ""
                                },
                                modifier = Modifier.testTag("send_comment_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = NeonPink)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Request Guest Call Button (if in guest call mode or general)
                IconButton(
                    onClick = { showGuestCallRequestDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = "Request Call", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Gift Button
                IconButton(
                    onClick = { showGiftDrawer = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFF007A), Color(0xFF7928CA))))
                        .testTag("open_gift_drawer_button")
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Send Gift", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Share Live & APK
                IconButton(
                    onClick = {
                        ShareHelper.shareLiveStream(
                            context = context,
                            hostName = stream.streamerName,
                            streamTitle = stream.title,
                            isPk = viewerStreamMode == LiveBroadcastMode.PK_BATTLE
                        )
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1E1B4B))
                        .border(1.5.dp, NeonPink, CircleShape)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonPink, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ================= REQUEST GUEST CALL DIALOG =================
        if (showGuestCallRequestDialog) {
            AlertDialog(
                onDismissRequest = { showGuestCallRequestDialog = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request to Join Guest Call", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Do you want to request to join ${stream.streamerName}'s live call as a guest?",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• You will speak directly on live stream.\n• Make sure your microphone is ready.", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGuestCallRequestDialog = false
                            celebrationGiftName = "📞 Call request sent to ${stream.streamerName}! Waiting for host approval."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
                    ) {
                        Text("Send Request 🎙️", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGuestCallRequestDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // ================= GIFT DRAWER SHEET =================
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
                        // Also boost PK score if in PK battle
                        if (viewerStreamMode == LiveBroadcastMode.PK_BATTLE) {
                            pkState = pkState.copy(blueScore = pkState.blueScore + gift.coinCost * 2)
                        }
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

        // ================= QUALITY SWITCHER DIALOG =================
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

        // ================= REPORT USER DIALOG =================
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
