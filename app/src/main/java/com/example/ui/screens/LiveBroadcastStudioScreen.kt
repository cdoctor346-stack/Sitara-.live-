package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.camera.CameraBroadcastPreview
import com.example.camera.CameraFilter
import com.example.data.model.ChatMessage
import com.example.data.model.ConnectedGuest
import com.example.data.model.GuestCallRequest
import com.example.data.model.LiveBroadcastMode
import com.example.data.model.PkBattleState
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.LiveChatOverlay
import com.example.ui.components.ReactionParticle
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.ShareHelper
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

@Composable
fun LiveBroadcastStudioScreen(
    onBroadcastEnded: (title: String, duration: String, category: String) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenAgoraStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Camera and Audio Permissions state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: false
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Studio Settings & State
    var streamTitle by remember { mutableStateOf("🔴 Live PK Battle & Multi-Guest Call ✨") }
    var isLive by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownNumber by remember { mutableIntStateOf(3) }

    // TikTok Live Broadcast Mode (SOLO, PK_BATTLE, GUEST_CALL)
    var currentMode by remember { mutableStateOf(LiveBroadcastMode.PK_BATTLE) }

    // Broadcaster Controls
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isMicMuted by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(CameraFilter.NONE) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showEndStreamConfirmation by remember { mutableStateOf(false) }

    // Live Metrics
    var durationSeconds by remember { mutableIntStateOf(0) }
    var viewerCount by remember { mutableIntStateOf(342) }
    var totalDiamonds by remember { mutableIntStateOf(1840) }

    // ================= PK BATTLE STATE =================
    var pkState by remember {
        mutableStateOf(
            PkBattleState(
                challengerName = "Sarah Queen 👑",
                challengerAvatarRes = R.drawable.img_pk_queen,
                blueScore = 5480,
                redScore = 4920,
                timeLeftSeconds = 180,
                isBoostRound = true,
                isFinished = false
            )
        )
    }
    var showPkResultDialog by remember { mutableStateOf(false) }
    var pkOpponentIndex by remember { mutableIntStateOf(0) }
    val pkOpponents = listOf(
        Pair("Sarah Queen 👑", R.drawable.img_pk_queen),
        Pair("Ryan Pro 🎮", R.drawable.img_pk_rival),
        Pair("Moin King ⭐", R.drawable.creator_moin)
    )

    // ================= GUEST CALL STATE =================
    val connectedGuests = remember {
        mutableStateListOf(
            ConnectedGuest(id = "g1", name = "Hamza 🎙️", avatarRes = R.drawable.img_guest_streamer, isSpeaking = true, isMuted = false),
            ConnectedGuest(id = "g2", name = "Ayesha 📹", avatarRes = R.drawable.img_pk_queen, isSpeaking = false, isMuted = false)
        )
    }
    val guestCallRequests = remember {
        mutableStateListOf(
            GuestCallRequest(id = "r1", name = "Usman Ali", avatarRes = R.drawable.creator_moin, requestType = "Audio", timeAgo = "1m ago"),
            GuestCallRequest(id = "r2", name = "Zaid Star", avatarRes = R.drawable.img_pk_rival, requestType = "Video", timeAgo = "Just now")
        )
    }
    var showGuestRequestsSheet by remember { mutableStateOf(false) }
    var selectedGuestForManagement by remember { mutableStateOf<ConnectedGuest?>(null) }

    // Live Chat & Reactions
    val liveChatMessages = remember {
        mutableStateListOf(
            ChatMessage(UUID.randomUUID().toString(), "SitaraBot", "Welcome! You are LIVE on TikTok PK & Guest Studio ✨", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "Ali_Gamer", "Lets win this PK battle!! 🚀"),
            ChatMessage(UUID.randomUUID().toString(), "Zara_VIP", "Sent 100 Roses to Blue Team! 🌹🌹")
        )
    }
    var chatInput by remember { mutableStateOf("") }
    val particles = remember { mutableStateListOf<ReactionParticle>() }

    // Equalizer audio wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_eq")
    val eqBar1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(320), RepeatMode.Reverse), label = "b1"
    )
    val eqBar2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(410), RepeatMode.Reverse), label = "b2"
    )
    val eqBar3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(290), RepeatMode.Reverse), label = "b3"
    )

    // Live Timer & Simulation
    LaunchedEffect(isLive) {
        if (isLive) {
            durationSeconds = 0
            while (isLive) {
                delay(1000)
                durationSeconds++

                // PK Battle Timer countdown
                if (currentMode == LiveBroadcastMode.PK_BATTLE && !pkState.isFinished) {
                    val nextTime = pkState.timeLeftSeconds - 1
                    if (nextTime <= 0) {
                        val winner = if (pkState.blueScore >= pkState.redScore) "BLUE" else "RED"
                        pkState = pkState.copy(timeLeftSeconds = 0, isFinished = true, winnerSide = winner)
                        showPkResultDialog = true
                    } else {
                        // Dynamic gifts simulation on Red & Blue sides
                        val redAdd = if (Random.nextInt(5) == 0) Random.nextInt(40, 220) else 0
                        val blueAdd = if (Random.nextInt(4) == 0) Random.nextInt(50, 260) else 0
                        pkState = pkState.copy(
                            timeLeftSeconds = nextTime,
                            blueScore = pkState.blueScore + blueAdd,
                            redScore = pkState.redScore + redAdd
                        )
                    }
                }

                // Random viewer fluctuation
                if (durationSeconds % 4 == 0) {
                    viewerCount = (viewerCount + Random.nextInt(-4, 9)).coerceAtLeast(150)
                }

                // Incoming viewer chat
                if (durationSeconds % 6 == 0) {
                    val sampleChatters = listOf("Bilal", "Sara_X", "CyberNinja", "QueenK", "Zain")
                    val sampleTexts = listOf("Let's go BLUE team! ⚔️", "Sent Galaxy gift! 🪐", "Host add me to guest call!", "PK hype is real! 🔥", "Shared stream to WhatsApp! 📲")
                    liveChatMessages.add(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            senderName = sampleChatters.random(),
                            text = sampleTexts.random()
                        )
                    )
                }

                // Periodic reaction heart float
                if (durationSeconds % 3 == 0) {
                    particles.add(
                        ReactionParticle(
                            id = System.currentTimeMillis() + Random.nextLong(1000),
                            emoji = listOf("❤️", "🔥", "💖", "🚀", "👑").random(),
                            startXOffset = Random.nextFloat() * 100f - 50f,
                            swayAmount = Random.nextFloat() * 30f + 10f
                        )
                    )
                }
            }
        }
    }

    val formattedDuration = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    val pkRemainingFormatted = remember(pkState.timeLeftSeconds) {
        val mins = pkState.timeLeftSeconds / 60
        val secs = pkState.timeLeftSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090814))
    ) {
        // ================= BACKGROUND / VIDEO BROADCAST STAGE =================
        when (currentMode) {
            LiveBroadcastMode.SOLO -> {
                // Full Screen CameraX
                CameraBroadcastPreview(
                    lensFacing = lensFacing,
                    isTorchEnabled = isTorchEnabled,
                    selectedFilter = selectedFilter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LiveBroadcastMode.PK_BATTLE -> {
                // TikTok 1v1 Split Screen (Left: Host, Right: Challenger)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Host Live Camera (Team Blue)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
                    ) {
                        CameraBroadcastPreview(
                            lensFacing = lensFacing,
                            isTorchEnabled = isTorchEnabled,
                            selectedFilter = selectedFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Blue Team Overlay Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC003B5C))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Host (You)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Right Side: Challenger Streamer (Team Red)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.5.dp, Color(0xFFFF2A85).copy(alpha = 0.5f))
                    ) {
                        Image(
                            painter = painterResource(id = pkState.challengerAvatarRes.takeIf { it != 0 } ?: R.drawable.img_pk_queen),
                            contentDescription = "PK Challenger",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay on Challenger
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x44000000), Color.Transparent, Color(0x99000000))
                                    )
                                )
                        )
                        // Challenger Name & Equalizer
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonPink)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(pkState.challengerName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Equalizer Audio Wave
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.width(2.5.dp).height(eqBar1.dp).background(NeonPink, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.5.dp).height(eqBar2.dp).background(Color(0xFFFF80B0), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.5.dp).height(eqBar3.dp).background(NeonPink, RoundedCornerShape(1.dp)))
                            }
                        }
                    }
                }
            }

            LiveBroadcastMode.GUEST_CALL -> {
                // Multi-Guest Layout: Host Top (60%), Connected Guest Boxes Bottom (40%)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f)
                    ) {
                        CameraBroadcastPreview(
                            lensFacing = lensFacing,
                            isTorchEnabled = isTorchEnabled,
                            selectedFilter = selectedFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x99000000))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("👑 Host Camera", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Connected Guests Stage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .background(Color(0xFF0F172A))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        connectedGuests.forEach { guest ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedGuestForManagement = guest },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (guest.isSpeaking) Color(0xFF00FF88) else Color(0x33FFFFFF)
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = guest.avatarRes),
                                        contentDescription = guest.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Guest name pill
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color(0xCC000000))
                                            .padding(vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = guest.name,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (guest.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = if (guest.isMuted) LiveRed else Color(0xFF00FF88),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    // Audio wave speaking indicator
                                    if (guest.isSpeaking) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xCC00FF88))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("TALKING", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        // Add/Invite Empty Guest Box
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { showGuestRequestsSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x22334155)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Invite Guest",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Invite Guest", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Scrim
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

        // Floating Reaction Hearts overlay
        FloatingReactionsContainer(
            particles = particles,
            onParticleFinished = { id -> particles.removeAll { it.id == id } }
        )

        // ================= TOP HEADER HUD =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Host Live Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC111827))
                        .border(1.dp, if (isLive) LiveRed.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLive) LiveRed else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLive) "LIVE" else "PREVIEW",
                        color = if (isLive) LiveRed else NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                    if (isLive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedDuration,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Viewers",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$viewerCount",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top Controls: Flip, Flash, Filters, Agora, Share & End
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Share Live & APK Button
                    IconButton(
                        onClick = {
                            ShareHelper.shareLiveStream(
                                context = context,
                                hostName = "Sitara Host",
                                streamTitle = streamTitle,
                                isPk = currentMode == LiveBroadcastMode.PK_BATTLE
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC1E1B4B))
                            .border(1.dp, NeonPink, CircleShape)
                            .testTag("share_live_apk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Live & APK",
                            tint = NeonPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Beauty Filter
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (selectedFilter != CameraFilter.NONE) NeonPink else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flip Camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close / Exit
                    IconButton(
                        onClick = {
                            if (isLive) showEndStreamConfirmation = true else onNavigateBack()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ================= TIKTOK MODE SWITCHER TABS =================
            // [ 📱 Solo ] | [ ⚔️ PK Battle ] | [ 👥 Guest Call ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC111827))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Solo Mode Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (currentMode == LiveBroadcastMode.SOLO) NeonCyan.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { currentMode = LiveBroadcastMode.SOLO }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "📱 Solo",
                        color = if (currentMode == LiveBroadcastMode.SOLO) NeonCyan else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // PK Battle Mode Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (currentMode == LiveBroadcastMode.PK_BATTLE) Brush.horizontalGradient(listOf(NeonCyan, NeonPink)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                        .clickable { currentMode = LiveBroadcastMode.PK_BATTLE }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "⚔️ PK Battle",
                        color = if (currentMode == LiveBroadcastMode.PK_BATTLE) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Guest Call Mode Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (currentMode == LiveBroadcastMode.GUEST_CALL) Color(0xFF00FF88).copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { currentMode = LiveBroadcastMode.GUEST_CALL }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👥 Guest Call",
                            color = if (currentMode == LiveBroadcastMode.GUEST_CALL) Color(0xFF00FF88) else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (guestCallRequests.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(LiveRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${guestCallRequests.size}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // ================= TIKTOK PK BATTLE PROGRESS TUG-OF-WAR BAR =================
            if (currentMode == LiveBroadcastMode.PK_BATTLE) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0B0F19)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // PK Header row: Blue score | PK Timer | Red score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Blue Team Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔵 Blue: ", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${pkState.blueScore}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }

                            // Center Flame PK VS & Timer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFFEF4444), NeonPink)))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("🔥 PK $pkRemainingFormatted", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                if (pkState.isBoostRound) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonAmber)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("x2 Boost", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // Red Team Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔴 Red: ", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${pkState.redScore}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tug of war ratio bar
                        val totalScore = (pkState.blueScore + pkState.redScore).coerceAtLeast(1)
                        val blueFraction = (pkState.blueScore.toFloat() / totalScore).coerceIn(0.15f, 0.85f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
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

                        // Broadcaster PK Quick Actions (Cheer Host, Switch Opponent, Rematch)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cheer Host Boost Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3300F0FF))
                                    .border(0.5.dp, NeonCyan, RoundedCornerShape(8.dp))
                                    .clickable {
                                        pkState = pkState.copy(blueScore = pkState.blueScore + 500)
                                        liveChatMessages.add(
                                            ChatMessage(UUID.randomUUID().toString(), "Host (You)", "Boosted Team Blue with +500 PK Points! ⚡", isStreamer = true)
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("⚡ Cheer Host +500", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Switch Opponent Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33FF2A85))
                                    .border(0.5.dp, NeonPink, RoundedCornerShape(8.dp))
                                    .clickable {
                                        pkOpponentIndex = (pkOpponentIndex + 1) % pkOpponents.size
                                        val next = pkOpponents[pkOpponentIndex]
                                        pkState = pkState.copy(
                                            challengerName = next.first,
                                            challengerAvatarRes = next.second,
                                            blueScore = 3200,
                                            redScore = 3100,
                                            timeLeftSeconds = 180,
                                            isFinished = false
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("🔄 Switch Opponent", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // End PK Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33EF4444))
                                    .border(0.5.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val winner = if (pkState.blueScore >= pkState.redScore) "BLUE" else "RED"
                                        pkState = pkState.copy(timeLeftSeconds = 0, isFinished = true, winnerSide = winner)
                                        showPkResultDialog = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("🛑 End PK", color = Color(0xFFFF6B6B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ================= GUEST CALL BANNER & REQUESTS SHORTCUT =================
            if (currentMode == LiveBroadcastMode.GUEST_CALL) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC0F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${connectedGuests.size} Guests Connected on Call",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Requests button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0284C7))
                            .clickable { showGuestRequestsSheet = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📞 Requests (${guestCallRequests.size})",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // ================= LIVE CHAT OVERLAY (LOWER THIRD) =================
        if (isLive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 76.dp)
            ) {
                LiveChatOverlay(
                    messages = liveChatMessages,
                    onMessageClick = { /* no-op */ },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // Broadcaster quick message bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Say something to chat...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("broadcaster_chat_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = Color(0xFF4B5563),
                            focusedContainerColor = Color(0xCC111827),
                            unfocusedContainerColor = Color(0x99111827)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                liveChatMessages.add(
                                    ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        senderName = "Host (You)",
                                        text = chatInput,
                                        isStreamer = true
                                    )
                                )
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                            .testTag("broadcaster_send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // ================= BOTTOM BAR CONTROLS =================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            if (!isLive) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stream title setup card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xD91E1C38)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ TikTok Live Setup: PK Battle & Guest Call",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = streamTitle,
                                onValueChange = { streamTitle = it },
                                label = { Text("Stream Title", color = TextSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stream_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = NeonPink,
                                    unfocusedBorderColor = Color(0xFF374151)
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // GO LIVE BUTTON
                    Button(
                        onClick = {
                            isCountingDown = true
                            countdownNumber = 3
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("go_live_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = "Broadcast",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START TIKTOK LIVE BROADCAST",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                // Active Broadcast Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // End Stream
                    Button(
                        onClick = { showEndStreamConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("end_stream_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("END LIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Center: Mic Toggle
                    IconButton(
                        onClick = { isMicMuted = !isMicMuted },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isMicMuted) LiveRed else Color(0xCC111827))
                            .border(1.dp, Color(0x44FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share Stream / APK Quick Action
                    Button(
                        onClick = {
                            ShareHelper.shareLiveStream(
                                context = context,
                                hostName = "Sitara Live",
                                streamTitle = streamTitle,
                                isPk = currentMode == LiveBroadcastMode.PK_BATTLE
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SHARE APK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    // Tap to Heart button
                    IconButton(
                        onClick = {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis(),
                                    emoji = "💖",
                                    startXOffset = 0f,
                                    swayAmount = 25f
                                )
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xE6FF2A85))
                    ) {
                        Text(text = "💖", fontSize = 20.sp)
                    }
                }
            }
        }

        // ================= COUNTDOWN OVERLAY =================
        if (isCountingDown) {
            LaunchedEffect(countdownNumber) {
                if (countdownNumber > 1) {
                    delay(1000)
                    countdownNumber--
                } else {
                    delay(1000)
                    isCountingDown = false
                    isLive = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC090814)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$countdownNumber",
                        fontSize = 88.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonPink
                    )
                    Text(
                        text = "GET READY FOR TIKTOK LIVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // ================= PK BATTLE WINNER CELEBRATION MODAL =================
        if (showPkResultDialog) {
            AlertDialog(
                onDismissRequest = { showPkResultDialog = false },
                containerColor = Color(0xFF13172E),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pkState.winnerSide == "BLUE") "🏆 BLUE TEAM WON!" else "🔴 RED TEAM WON!",
                            color = if (pkState.winnerSide == "BLUE") NeonCyan else NeonPink,
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = if (pkState.winnerSide == "BLUE")
                                "Congratulations Host! Your followers pushed the PK score to ${pkState.blueScore} points and defeated ${pkState.challengerName}!"
                            else
                                "Great match! ${pkState.challengerName} won this round with ${pkState.redScore} points. Challenge for a rematch now!",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3300FF88))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💎 +2,500 Diamonds Earned in Battle!", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Rematch
                            pkState = pkState.copy(
                                blueScore = 3500,
                                redScore = 3200,
                                timeLeftSeconds = 180,
                                isFinished = false,
                                winnerSide = null
                            )
                            showPkResultDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("🔄 Rematch PK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPkResultDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        // ================= GUEST REQUESTS DRAWER MODAL =================
        if (showGuestRequestsSheet) {
            AlertDialog(
                onDismissRequest = { showGuestRequestsSheet = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text("👥 Guest Call Requests (${guestCallRequests.size})", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (guestCallRequests.isEmpty()) {
                            Text("No pending guest call requests right now.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            guestCallRequests.forEach { req ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = req.avatarRes),
                                                contentDescription = req.name,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(req.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Wants to join ${req.requestType} call", color = NeonCyan, fontSize = 10.sp)
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Accept
                                            Button(
                                                onClick = {
                                                    connectedGuests.add(
                                                        ConnectedGuest(
                                                            id = UUID.randomUUID().toString(),
                                                            name = req.name,
                                                            avatarRes = req.avatarRes,
                                                            isSpeaking = true
                                                        )
                                                    )
                                                    guestCallRequests.remove(req)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Accept", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Decline
                                            TextButton(
                                                onClick = { guestCallRequests.remove(req) }
                                            ) {
                                                Text("✕", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                // Simulate inviting new friend
                                connectedGuests.add(
                                    ConnectedGuest(
                                        id = UUID.randomUUID().toString(),
                                        name = "Guest ${connectedGuests.size + 1} 🎙️",
                                        avatarRes = R.drawable.creator_moin,
                                        isSpeaking = true
                                    )
                                )
                                showGuestRequestsSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Invite New Guest to Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGuestRequestsSheet = false }) {
                        Text("Close", color = NeonCyan)
                    }
                }
            )
        }

        // ================= GUEST MANAGEMENT MODAL =================
        selectedGuestForManagement?.let { guest ->
            AlertDialog(
                onDismissRequest = { selectedGuestForManagement = null },
                containerColor = Color(0xFF1E293B),
                title = { Text("Manage: ${guest.name}", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val idx = connectedGuests.indexOfFirst { it.id == guest.id }
                                if (idx != -1) {
                                    connectedGuests[idx] = connectedGuests[idx].copy(isMuted = !guest.isMuted)
                                }
                                selectedGuestForManagement = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (guest.isMuted) Color(0xFF00FF88) else Color(0xFFEF4444))
                        ) {
                            Text(if (guest.isMuted) "Unmute Guest Mic 🎙️" else "Mute Guest Mic 🔇", color = Color.White)
                        }

                        Button(
                            onClick = {
                                connectedGuests.removeIf { it.id == guest.id }
                                selectedGuestForManagement = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                        ) {
                            Text("Disconnect / End Call 📴", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedGuestForManagement = null }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        // ================= BEAUTY FILTERS SHEET =================
        if (showFilterSheet) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp, top = 80.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6131224)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Beauty & Lighting", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CameraFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPink.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable {
                                    selectedFilter = filter
                                    showFilterSheet = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filter.displayName,
                                color = if (isSelected) NeonPink else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ================= END STREAM CONFIRMATION =================
        if (showEndStreamConfirmation) {
            AlertDialog(
                onDismissRequest = { showEndStreamConfirmation = false },
                containerColor = Color(0xFF13172E),
                title = { Text("End Live Broadcast?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to end this live broadcast? All PK and Guest Call data will be saved.", color = Color(0xFFCBD5E1))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEndStreamConfirmation = false
                            isLive = false
                            onBroadcastEnded(streamTitle, formattedDuration, "Live PK & Guest")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                    ) {
                        Text("End Stream", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndStreamConfirmation = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}
