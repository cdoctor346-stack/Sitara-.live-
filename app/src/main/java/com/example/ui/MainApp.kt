package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreamItem
import com.example.data.repository.LiveStreamRepository
import androidx.compose.material.icons.filled.Groups
import com.example.ui.screens.AgoraLiveStreamScreen
import com.example.ui.screens.GuestRoomSeatingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.LiveBroadcastStudioScreen
import com.example.ui.screens.LiveViewerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandDarkSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppDestination {
    DISCOVER,
    GUEST_ROOM,
    SCHEDULE,
    PROFILE,
    EDIT_PROFILE,
    ADMIN_PANEL,
    BROADCAST_STUDIO,
    VIEWER_PLAYER,
    AGORA_LIVE_RTC
}

@Composable
fun MainApp(
    repository: LiveStreamRepository? = null
) {
    val context = LocalContext.current
    val repo = repository ?: remember { LiveStreamRepository(context) }
    var currentDestination by remember { mutableStateOf(AppDestination.DISCOVER) }
    var selectedViewingStream by remember { mutableStateOf<StreamItem?>(null) }
    var showTopUpDialog by remember { mutableStateOf(false) }

    // Repository states
    val streams by repo.liveStreams.collectAsState()
    val coinsBalance by repo.coinsBalance.collectAsState()
    val chatMessages by repo.chatMessages.collectAsState()
    val scheduleItems by repo.scheduleList.collectAsState()
    val userProfile by repo.userProfile.collectAsState()
    val vods by repo.vodsList.collectAsState()
    val crossConfig by repo.crossPlatformConfig.collectAsState()
    val allUsers by repo.allUsers.collectAsState()
    val reportQueue by repo.reportQueue.collectAsState()
    val platformStats by repo.platformStats.collectAsState()
    val maintenanceMode by repo.maintenanceMode.collectAsState()
    val slowModeActive by repo.slowModeActive.collectAsState()

    // Handle back button when inside Viewer or Broadcast Studio
    BackHandler(enabled = currentDestination != AppDestination.DISCOVER) {
        when (currentDestination) {
            AppDestination.VIEWER_PLAYER -> {
                selectedViewingStream = null
                currentDestination = AppDestination.DISCOVER
            }
            AppDestination.BROADCAST_STUDIO -> {
                currentDestination = AppDestination.DISCOVER
            }
            else -> {
                currentDestination = AppDestination.DISCOVER
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDark),
        containerColor = BrandDark,
        bottomBar = {
            // Only show bottom navigation when not in full-screen broadcast studio or viewer player
            if (currentDestination != AppDestination.BROADCAST_STUDIO &&
                currentDestination != AppDestination.VIEWER_PLAYER &&
                currentDestination != AppDestination.AGORA_LIVE_RTC) {
                NavigationBar(
                    containerColor = Color(0xF2100E26),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(68.dp)
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.DISCOVER,
                        onClick = { currentDestination = AppDestination.DISCOVER },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Discover"
                            )
                        },
                        label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonPink,
                            selectedTextColor = NeonPink,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0x33FF2A85)
                        ),
                        modifier = Modifier.testTag("nav_discover")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.GUEST_ROOM,
                        onClick = { currentDestination = AppDestination.GUEST_ROOM },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Guest Room"
                            )
                        },
                        label = { Text("Guest Room", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0x3300F0FF)
                        ),
                        modifier = Modifier.testTag("nav_guest_room")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.SCHEDULE,
                        onClick = { currentDestination = AppDestination.SCHEDULE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Schedule"
                            )
                        },
                        label = { Text("Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonPink,
                            selectedTextColor = NeonPink,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0x33FF2A85)
                        ),
                        modifier = Modifier.testTag("nav_schedule")
                    )

                    // Prominent GO LIVE Center Action
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentDestination = AppDestination.BROADCAST_STUDIO },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(NeonPink, NeonCyan)))
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F0E26)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Go Live Studio",
                                        tint = NeonPink,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { Text("Go Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_go_live")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.PROFILE,
                        onClick = { currentDestination = AppDestination.PROFILE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonPink,
                            selectedTextColor = NeonPink,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0x33FF2A85)
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { target ->
                when (target) {
                    AppDestination.DISCOVER -> {
                        HomeScreen(
                            streams = streams,
                            coinsBalance = coinsBalance,
                            onStreamClick = { stream ->
                                repo.selectStream(stream)
                                selectedViewingStream = stream
                                currentDestination = AppDestination.VIEWER_PLAYER
                            },
                            onGoLiveClick = {
                                currentDestination = AppDestination.BROADCAST_STUDIO
                            },
                            onTopUpClick = { showTopUpDialog = true },
                            onOpenAdminPanel = { currentDestination = AppDestination.ADMIN_PANEL },
                            onOpenAgoraLive = { currentDestination = AppDestination.AGORA_LIVE_RTC },
                            onOpenGuestRoom = { currentDestination = AppDestination.GUEST_ROOM }
                        )
                    }

                    AppDestination.GUEST_ROOM -> {
                        GuestRoomSeatingScreen(
                            coinsBalance = coinsBalance,
                            onCoinsDeducted = { cost -> repo.addCoins(-cost) },
                            onTopUpCoins = { showTopUpDialog = true },
                            onNavigateBack = { currentDestination = AppDestination.DISCOVER }
                        )
                    }

                    AppDestination.SCHEDULE -> {
                        ScheduleScreen(
                            scheduleItems = scheduleItems,
                            onToggleReminder = { id -> repo.toggleScheduleReminder(id) }
                        )
                    }

                    AppDestination.PROFILE -> {
                        ProfileScreen(
                            profile = userProfile.copy(coins = coinsBalance),
                            vods = vods,
                            crossConfig = crossConfig,
                            onTopUpCoins = { showTopUpDialog = true },
                            onEditProfileClick = { currentDestination = AppDestination.EDIT_PROFILE },
                            onOpenAdminPanel = { currentDestination = AppDestination.ADMIN_PANEL },
                            onOpenAgoraRtc = { currentDestination = AppDestination.AGORA_LIVE_RTC }
                        )
                    }

                    AppDestination.EDIT_PROFILE -> {
                        EditProfileScreen(
                            currentProfile = userProfile,
                            onSaveProfile = { name, bio, avatar ->
                                repo.updateUserProfile(name, bio, avatar)
                                currentDestination = AppDestination.PROFILE
                            },
                            onNavigateBack = { currentDestination = AppDestination.PROFILE }
                        )
                    }

                    AppDestination.ADMIN_PANEL -> {
                        AdminDashboardScreen(
                            stats = platformStats,
                            activeStreams = streams,
                            users = allUsers,
                            reports = reportQueue,
                            onToggleBanUser = { userId -> repo.toggleUserBan(userId) },
                            onToggleVerifyUser = { userId -> repo.toggleUserVerification(userId) },
                            onForceTerminateStream = { streamId -> repo.forceTerminateStream(streamId) },
                            onResolveReport = { reportId, action -> repo.resolveReport(reportId, action) },
                            onUpdateUserRole = { userId, role -> repo.updateUserRole(userId, role) },
                            onAdjustCoins = { userId, delta -> repo.adjustUserCoins(userId, delta) },
                            onSendGlobalAnnouncement = { msg -> repo.sendGlobalAnnouncement(msg) },
                            maintenanceMode = maintenanceMode,
                            onToggleMaintenance = { repo.toggleMaintenanceMode() },
                            slowModeActive = slowModeActive,
                            onToggleSlowMode = { repo.toggleSlowMode() },
                            onNavigateBack = { currentDestination = AppDestination.PROFILE }
                        )
                    }

                    AppDestination.BROADCAST_STUDIO -> {
                        LiveBroadcastStudioScreen(
                            onBroadcastEnded = { title, duration, category ->
                                repo.recordNewBroadcastVod(title, duration, category)
                                currentDestination = AppDestination.PROFILE
                            },
                            onNavigateBack = { currentDestination = AppDestination.DISCOVER },
                            onOpenAgoraStudio = { currentDestination = AppDestination.AGORA_LIVE_RTC }
                        )
                    }

                    AppDestination.VIEWER_PLAYER -> {
                        selectedViewingStream?.let { activeStream ->
                            LiveViewerScreen(
                                stream = activeStream,
                                messages = chatMessages,
                                gifts = repo.availableGifts,
                                coinsBalance = coinsBalance,
                                onSendMessage = { text -> repo.sendChatMessage(text) },
                                onSendGift = { gift -> repo.sendGift(gift) },
                                onToggleFollow = { id -> repo.toggleFollow(id) },
                                onTopUpCoins = { showTopUpDialog = true },
                                onOpenAgoraAudience = { currentDestination = AppDestination.AGORA_LIVE_RTC },
                                onClose = {
                                    selectedViewingStream = null
                                    currentDestination = AppDestination.DISCOVER
                                }
                            )
                        } ?: run {
                            currentDestination = AppDestination.DISCOVER
                        }
                    }

                    AppDestination.AGORA_LIVE_RTC -> {
                        AgoraLiveStreamScreen(
                            onNavigateBack = { currentDestination = AppDestination.DISCOVER }
                        )
                    }
                }
            }
        }
    }

    // Top Up Coins Dialog
    if (showTopUpDialog) {
        val coinPackages = listOf(
            Pair(100, "$0.99"),
            Pair(500, "$4.99"),
            Pair(1200, "$9.99 (Best Value 🔥)"),
            Pair(3000, "$24.99")
        )

        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = {
                Text(
                    text = "🪙 Top Up Sitara Coins",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Use coins to send gifts and support streamers in real-time:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    coinPackages.forEach { (amount, price) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F1D38))
                                .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    repo.addCoins(amount)
                                    showTopUpDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$amount Coins",
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = price,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }
}
