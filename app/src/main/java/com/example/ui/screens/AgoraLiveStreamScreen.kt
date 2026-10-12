package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.agora.AgoraConnectionStatus
import com.example.agora.AgoraLiveTelemetryBadge
import com.example.agora.AgoraLocalVideoView
import com.example.agora.AgoraManager
import com.example.agora.AgoraRemoteVideoView
import com.example.agora.AgoraUserRole
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandDarkCard
import com.example.ui.theme.BrandDarkSurface
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AgoraLiveStreamScreen(
    initialChannel: String = "sitara_live_room_1",
    initialRole: AgoraUserRole = AgoraUserRole.BROADCASTER,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val agoraManager = remember { AgoraManager.getInstance() }

    var channelName by remember { mutableStateOf(initialChannel) }
    var userRole by remember { mutableStateOf(initialRole) }
    var isJoined by remember { mutableStateOf(false) }

    val stats by agoraManager.streamStats.collectAsState()
    val remoteUsers by agoraManager.remoteUsers.collectAsState()

    var showAppIdDialog by remember { mutableStateOf(false) }
    var currentAppId by remember { mutableStateOf(agoraManager.getEffectiveAppId(context)) }
    var inputAppId by remember { mutableStateOf(currentAppId) }
    var setupErrorMessage by remember { mutableStateOf<String?>(null) }

    var isPkBattleActive by remember { mutableStateOf(false) }
    var viewerCount by remember { mutableStateOf(248) }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                "Sitara: Welcome to live stream! 🔥",
                "Ali: Amazing broadcast quality!",
                "Sara: Sent a Diamond 💎",
                "Zainab: Hello from Lahore! 👋"
            )
        )
    }
    var latestGift by remember { mutableStateOf<String?>(null) }
    var showChatDialog by remember { mutableStateOf(false) }
    var newChatMessage by remember { mutableStateOf("") }

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = (perms[Manifest.permission.CAMERA] ?: false) &&
                (perms[Manifest.permission.RECORD_AUDIO] ?: false)
    }

    // Leave channel when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            if (isJoined) {
                agoraManager.leaveChannel()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandDark)
    ) {
        // Video View Canvas
        if (isJoined) {
            if (isPkBattleActive) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (userRole == AgoraUserRole.BROADCASTER) {
                            AgoraLocalVideoView(modifier = Modifier.fillMaxSize(), agoraManager = agoraManager)
                        } else {
                            val activeRemoteUid = stats.remoteUid ?: remoteUsers.firstOrNull()
                            if (activeRemoteUid != null) {
                                AgoraRemoteVideoView(remoteUid = activeRemoteUid, modifier = Modifier.fillMaxSize(), agoraManager = agoraManager)
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1B4B)), contentAlignment = Alignment.Center) {
                                    Text("Host Stream", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(NeonPink))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF26102A)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚔️ PK BATTLE", color = NeonPink, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Opponent: @ahmed_live", color = TextPrimary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            val remoteOpponent = remoteUsers.lastOrNull()
                            if (remoteOpponent != null) {
                                AgoraRemoteVideoView(remoteUid = remoteOpponent, modifier = Modifier.fillMaxSize(), agoraManager = agoraManager)
                            } else {
                                Text("Waiting for opponent...", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            } else {
                when (userRole) {
                    AgoraUserRole.BROADCASTER -> {
                        AgoraLocalVideoView(
                            modifier = Modifier.fillMaxSize(),
                            agoraManager = agoraManager
                        )
                    }

                    AgoraUserRole.AUDIENCE -> {
                        val activeRemoteUid = stats.remoteUid ?: remoteUsers.firstOrNull()
                        if (activeRemoteUid != null) {
                            AgoraRemoteVideoView(
                                remoteUid = activeRemoteUid,
                                modifier = Modifier.fillMaxSize(),
                                agoraManager = agoraManager
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F0E26)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Waiting for Broadcaster to join channel...",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Channel: $channelName",
                                        color = NeonAmber,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Chat & Viewer Overlay when joined
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 90.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(160.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    chatMessages.takeLast(5).forEach { msg ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x77000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = msg, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                if (latestGift != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xCCFF2D55))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = latestGift ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            // Placeholder when not joined
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Agora Live",
                        tint = NeonCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Agora.io Real-Time Video Engine",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Broadcast video in ultra-low latency (<200ms) or view live streams from global SD-RTN.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Top Navigation & Stats Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isJoined) agoraManager.leaveChannel()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            inputAppId = agoraManager.getEffectiveAppId(context)
                            showAppIdDialog = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                            .testTag("agora_config_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Configure Agora App ID",
                            tint = if (currentAppId.isNotBlank()) NeonCyan else NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AgoraLiveTelemetryBadge(
                        channelName = channelName,
                        role = userRole,
                        agoraManager = agoraManager
                    )
                }
            }

            // Real-time telemetry pills when connected
            if (isJoined && stats.connectionStatus == AgoraConnectionStatus.CONNECTED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (userRole == AgoraUserRole.BROADCASTER) "Tx: ${stats.sendBitrateKbps} kbps" else "Rx: ${stats.recvBitrateKbps} kbps",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Loss: ${stats.packetLossRate}%",
                        color = if (stats.packetLossRate > 5) LiveRed else Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (userRole == AgoraUserRole.BROADCASTER) {
                        Text(
                            text = "${stats.videoFps} FPS",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Controls Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF216152B)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2C4D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!isJoined) {
                    // App ID Status Banner & Direct Configuration Prompt
                    if (currentAppId.isBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    inputAppId = ""
                                    showAppIdDialog = true
                                }
                                .testTag("agora_missing_appid_card"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C14)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Agora App ID Required ⚡",
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Agar aapne popup cross (X) kar diya tha, yahan click kar ke App ID paste karein.",
                                        color = TextPrimary.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add ⚙️",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F261B))
                                .clickable {
                                    inputAppId = currentAppId
                                    showAppIdDialog = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "App ID Active (${if (currentAppId.length > 8) "${currentAppId.take(4)}...${currentAppId.takeLast(4)}" else currentAppId})",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Settings ⚙️",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { userRole = AgoraUserRole.BROADCASTER },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userRole == AgoraUserRole.BROADCASTER) NeonPink else Color(0xFF262447)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Broadcaster", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { userRole = AgoraUserRole.AUDIENCE },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userRole == AgoraUserRole.AUDIENCE) NeonCyan else Color(0xFF262447)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Audience",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userRole == AgoraUserRole.AUDIENCE) Color(0xFF0F0E26) else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Channel Name input
                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text("Channel Name", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agora_channel_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Join Channel Button with validation
                    Button(
                        onClick = {
                            val effectiveId = agoraManager.getEffectiveAppId(context)
                            if (effectiveId.isBlank()) {
                                setupErrorMessage = "Please enter your Agora App ID before connecting."
                                inputAppId = ""
                                showAppIdDialog = true
                                return@Button
                            }

                            if (!hasPermissions) {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                )
                            } else {
                                val initialized = agoraManager.initRtcEngine(context)
                                if (initialized) {
                                    val joined = agoraManager.joinChannel(
                                        channelName = channelName.ifBlank { "sitara_live" },
                                        role = userRole
                                    )
                                    if (joined) {
                                        isJoined = true
                                    } else {
                                        setupErrorMessage = "Unable to connect to Agora channel. Please check connection."
                                    }
                                } else {
                                    setupErrorMessage = "Agora initialization failed. Please re-check your App ID."
                                    showAppIdDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (userRole == AgoraUserRole.BROADCASTER) NeonPink else NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agora_join_channel_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userRole == AgoraUserRole.BROADCASTER) "Start Broadcasting Now" else "Join Audience Stream",
                            color = if (userRole == AgoraUserRole.BROADCASTER) Color.White else Color(0xFF0F0E26),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Joined in session controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userRole == AgoraUserRole.BROADCASTER) {
                            // Camera Switch
                            IconButton(
                                onClick = { agoraManager.switchCamera() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262447))
                            ) {
                                Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }

                            // Mic Mute Toggle
                            IconButton(
                                onClick = { agoraManager.toggleLocalAudio(!stats.isLocalAudioMuted) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (stats.isLocalAudioMuted) LiveRed else Color(0xFF262447))
                            ) {
                                Icon(
                                    imageVector = if (stats.isLocalAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // PK Battle Toggle
                            IconButton(
                                onClick = { isPkBattleActive = !isPkBattleActive },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isPkBattleActive) NeonPink else Color(0xFF262447))
                            ) {
                                Text("⚔️", fontSize = 16.sp)
                            }
                        } else {
                            // Audience Gift Button
                            Button(
                                onClick = {
                                    latestGift = "🎁 Sent Super Car Gift to Broadcaster!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("🎁 Send Gift", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Leave Channel Button
                        Button(
                            onClick = {
                                agoraManager.leaveChannel()
                                isJoined = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("agora_leave_channel_button").height(38.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leave", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Agora App ID Configuration Dialog
        if (showAppIdDialog) {
            AlertDialog(
                onDismissRequest = {
                    setupErrorMessage = null
                    showAppIdDialog = false
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agora App ID Setup",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Agar aapne AI Studio ka popup cross (X) kar diya tha, toh aap yahan direct apna Agora App ID enter kar sakte hain ya Secrets panel use kar sakte hain:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Method summary card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1D36)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33315C))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "💡 2 Aasan Tareeqay (Ways to Add):",
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1️⃣ AI Studio Secrets: AI Studio ke left sidebar mein 'Secrets' tab kholein aur key 'AGORA_APP_ID' add karein.\n\n2️⃣ Direct yahan: Apna App ID niche paste karein aur 'Save App ID' dabayein (free ID: console.agora.io).",
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputAppId,
                            onValueChange = { inputAppId = it },
                            label = { Text("Agora App ID (e.g. 32-char hex)", fontSize = 11.sp) },
                            placeholder = { Text("Paste your Agora App ID here", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agora_appid_input_dialog"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (setupErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = setupErrorMessage ?: "",
                                color = LiveRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanId = inputAppId.trim()
                            if (cleanId.isNotBlank()) {
                                agoraManager.saveAppId(context, cleanId)
                                currentAppId = cleanId
                                setupErrorMessage = null
                                showAppIdDialog = false
                            } else {
                                setupErrorMessage = "Please enter or paste a valid App ID."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("agora_save_appid_button")
                    ) {
                        Text("Save App ID", color = Color(0xFF0F0E26), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (currentAppId.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    agoraManager.saveAppId(context, "")
                                    currentAppId = ""
                                    inputAppId = ""
                                    setupErrorMessage = null
                                }
                            ) {
                                Text("Clear", color = LiveRed, fontSize = 12.sp)
                            }
                        }
                        TextButton(
                            onClick = {
                                setupErrorMessage = null
                                showAppIdDialog = false
                            }
                        ) {
                            Text("Close", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                },
                containerColor = Color(0xFF16152B),
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}
