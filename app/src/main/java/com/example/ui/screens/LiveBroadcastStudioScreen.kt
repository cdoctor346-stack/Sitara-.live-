package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraBroadcastPreview
import com.example.camera.CameraFilter
import com.example.data.model.ChatMessage
import com.example.ui.components.FloatingReactionsContainer
import com.example.ui.components.LiveChatOverlay
import com.example.ui.components.ReactionParticle
import com.example.ui.theme.BrandDarkCard
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
    val clipboardManager = LocalClipboardManager.current

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

    // Studio Settings
    var streamTitle by remember { mutableStateOf("🔴 Live with Sitara: Chill & Hangout") }
    var selectedCategory by remember { mutableStateOf("Gaming") }
    var streamResolution by remember { mutableStateOf("1080p60") }
    var isLive by remember { mutableStateOf(false) }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownNumber by remember { mutableIntStateOf(3) }

    // Broadcaster Controls
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isMicMuted by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(CameraFilter.NONE) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showOBSConfigDialog by remember { mutableStateOf(false) }
    var showEndStreamConfirmation by remember { mutableStateOf(false) }
    var showStreamSummary by remember { mutableStateOf(false) }

    // Live Metrics
    var durationSeconds by remember { mutableIntStateOf(0) }
    var viewerCount by remember { mutableIntStateOf(24) }
    var totalDiamonds by remember { mutableIntStateOf(85) }
    var streamBitrate by remember { mutableIntStateOf(6180) }

    // Live Chat & Reactions
    val liveChatMessages = remember {
        mutableStateListOf(
            ChatMessage(UUID.randomUUID().toString(), "SitaraBot", "Welcome! You are live to followers ✨", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "NeonRider", "Quality looks clean 1080p!"),
            ChatMessage(UUID.randomUUID().toString(), "Sara_X", "Let's goooo! 🔥")
        )
    }
    var chatInput by remember { mutableStateOf("") }
    val particles = remember { mutableStateListOf<ReactionParticle>() }

    // Live Timer & Dynamic viewers
    LaunchedEffect(isLive) {
        if (isLive) {
            durationSeconds = 0
            while (isLive) {
                delay(1000)
                durationSeconds++
                // Random viewer fluctuation
                if (durationSeconds % 4 == 0) {
                    val delta = Random.nextInt(-3, 8)
                    viewerCount = (viewerCount + delta).coerceAtLeast(12)
                    streamBitrate = Random.nextInt(5900, 6450)
                }
                // Random simulated incoming viewer chat
                if (durationSeconds % 7 == 0) {
                    val sampleChatters = listOf("Elena", "Max_Pro", "HypeBeast", "Kira", "Viper")
                    val sampleTexts = listOf("Awesome broadcast!", "Hype!! 🔥", "Sent 10 stars ⭐", "What software is this?", "Love the vibe")
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
                            emoji = listOf("❤️", "🔥", "💎", "🚀", "🎉").random(),
                            startXOffset = Random.nextFloat() * 100f - 50f,
                            swayAmount = Random.nextFloat() * 30f + 10f
                        )
                    )
                }
            }
        }
    }

    // Format duration mm:ss
    val formattedDuration = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Broadcast Preview
        CameraBroadcastPreview(
            lensFacing = lensFacing,
            isTorchEnabled = isTorchEnabled,
            selectedFilter = selectedFilter,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Reaction Hearts overlay
        FloatingReactionsContainer(
            particles = particles,
            onParticleFinished = { id -> particles.removeAll { it.id == id } }
        )

        // Overlay Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color.Transparent,
                            Color(0xCC000000)
                        )
                    )
                )
        )

        // Top Studio HUD Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live status or Setup pill
                if (isLive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCC111827))
                            .border(1.dp, LiveRed.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LiveRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            color = LiveRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
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
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xB31E1B4B))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "STUDIO PREVIEW",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Broadcaster Action Controls (Flip, Flash, Filters, OBS)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Filter button
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .testTag("camera_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Beauty & Filters",
                            tint = if (selectedFilter != CameraFilter.NONE) NeonPink else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Flip camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                CameraSelector.LENS_FACING_BACK
                            else
                                CameraSelector.LENS_FACING_FRONT
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .testTag("camera_flip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Torch
                    IconButton(
                        onClick = { isTorchEnabled = !isTorchEnabled },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .testTag("torch_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Torch",
                            tint = if (isTorchEnabled) NeonAmber else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mic Mute
                    IconButton(
                        onClick = { isMicMuted = !isMicMuted },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .testTag("mic_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = if (isMicMuted) LiveRed else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // OBS / RTMP settings
                    IconButton(
                        onClick = { showOBSConfigDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .testTag("obs_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cross Platform Stream Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            // Real-time broadcast telemetry row (when live)
            if (isLive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● 60 FPS",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$streamBitrate kbps",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "💎 $totalDiamonds",
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Chat Overlay (Middle-Bottom)
        if (isLive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            ) {
                LiveChatOverlay(
                    messages = liveChatMessages,
                    onMessageClick = { /* No-op in studio */ },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // Quick broadcaster chat response
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Say something to chat...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
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
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                            .testTag("broadcaster_send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Bottom Broadcast Action Panel
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (!isLive) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stream title preview
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xD91E1C38)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Stream Setup",
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

                    // Agora.io Global Broadcast Studio Button
                    Button(
                        onClick = onOpenAgoraStudio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .height(46.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Launch Agora.io Global RTC Studio",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // GO LIVE Button
                    Button(
                        onClick = {
                            isCountingDown = true
                            countdownNumber = 3
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("go_live_button"),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = "Broadcast",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START LIVE BROADCAST",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                // End Stream Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showEndStreamConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("end_stream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "END STREAM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Send heart reaction as host
                    IconButton(
                        onClick = {
                            particles.add(
                                ReactionParticle(
                                    id = System.currentTimeMillis(),
                                    emoji = "❤️",
                                    startXOffset = 0f,
                                    swayAmount = 25f
                                )
                            )
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xE6FF2A85))
                    ) {
                        Text(text = "💖", fontSize = 24.sp)
                    }
                }
            }
        }

        // Countdown Overlay
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
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonPink
                    )
                    Text(
                        text = "GET READY TO GO LIVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Beauty Filter Selector Bar (Modal)
        if (showFilterSheet) {
            Card(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, top = 80.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6131224)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Beauty & Lighting",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // OBS Studio & Cross-Platform Stream Setup Dialog
        if (showOBSConfigDialog) {
            val rtmpUrl = "rtmp://live.sitaralive.tv/app"
            val streamKey = "live_sk_sitara_8923a4b912ecf"

            AlertDialog(
                onDismissRequest = { showOBSConfigDialog = false },
                title = {
                    Text(text = "Cross-Platform Streaming Setup", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(
                            text = "Stream from OBS Studio, Streamlabs, or iOS/Android devices using RTMP:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "RTMP Ingest Server:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandDarkCard)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = rtmpUrl, color = TextPrimary, fontSize = 12.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(rtmpUrl)) }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Stream Key (Keep secret):", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandDarkCard)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "••••••••••••••••••••", color = TextPrimary, fontSize = 12.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(streamKey)) }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonPink, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showOBSConfigDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Done")
                    }
                },
                containerColor = Color(0xFF16152B)
            )
        }

        // End Stream Confirmation Dialog
        if (showEndStreamConfirmation) {
            AlertDialog(
                onDismissRequest = { showEndStreamConfirmation = false },
                title = { Text("End Live Broadcast?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to end this live broadcast now?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showEndStreamConfirmation = false
                            isLive = false
                            showStreamSummary = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                    ) {
                        Text("End Stream")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndStreamConfirmation = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF16152B)
            )
        }

        // Stream End Summary Modal (Saves VOD to Room!)
        if (showStreamSummary) {
            AlertDialog(
                onDismissRequest = {
                    showStreamSummary = false
                    onBroadcastEnded(streamTitle, formattedDuration, selectedCategory)
                },
                title = {
                    Text(text = "🎉 Broadcast Completed!", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(text = streamTitle, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Duration", color = TextSecondary, fontSize = 11.sp)
                                Text(text = formattedDuration, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text(text = "Peak Viewers", color = TextSecondary, fontSize = 11.sp)
                                Text(text = "$viewerCount", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text(text = "Diamonds", color = TextSecondary, fontSize = 11.sp)
                                Text(text = "$totalDiamonds 💎", color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "This recording has been saved to your VOD Archives in Room database.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStreamSummary = false
                            onBroadcastEnded(streamTitle, formattedDuration, selectedCategory)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Save & Exit Studio")
                    }
                },
                containerColor = Color(0xFF16152B)
            )
        }
    }
}
