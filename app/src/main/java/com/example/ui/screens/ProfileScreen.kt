package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CrossPlatformStreamConfig
import com.example.data.model.RecordedVod
import com.example.data.model.StreamerProfile
import com.example.ui.theme.BrandDarkCard
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    profile: StreamerProfile,
    vods: List<RecordedVod>,
    crossConfig: CrossPlatformStreamConfig,
    onTopUpCoins: () -> Unit,
    onEditProfileClick: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenAgoraRtc: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedVodForReplay by remember { mutableStateOf<RecordedVod?>(null) }
    var isRestreamingEnabled by remember { mutableStateOf(crossConfig.isRestreamActive) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090814)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(
                                2.5.dp,
                                Brush.sweepGradient(listOf(NeonPink, NeonCyan, NeonPink)),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = profile.avatarRes ?: R.drawable.creator_moin),
                            contentDescription = profile.username,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.username,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Creator",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = profile.handle,
                        color = NeonPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = profile.bio,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF131224))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn(title = "Followers", value = "184.5K")
                        StatColumn(title = "Following", value = "${profile.following}")
                        StatColumn(title = "Broadcasts", value = "${profile.totalStreams}")
                        StatColumn(title = "Diamonds", value = "${profile.diamonds} 💎")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEditProfileClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242247)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Edit Profile", color = TextPrimary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenAgoraRtc,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("📡 Agora Live", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenAdminPanel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🛡️ Admin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Wallet & Tipping Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = NeonAmber,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wallet & Gifting Hub",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Coins: ${profile.coins} 🪙  •  Diamonds: ${profile.diamonds} 💎",
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = onTopUpCoins,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("top_up_coins_button")
                    ) {
                        Text(
                            text = "+ Top Up",
                            color = Color(0xFF1E1B4B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Tabs: Broadcast Archives (VODs) & Cross-Platform Config
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonPink
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VOD Archives (${vods.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("OBS & Multi-Stream", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tab Content
        if (selectedTab == 0) {
            // VOD Archives
            items(vods, key = { it.id }) { vod ->
                VodItemCard(
                    vod = vod,
                    onPlay = { selectedVodForReplay = vod }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            // Cross-Platform OBS / RTMP Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandDarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cross-Platform Streaming Setup",
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect Sitara Live with OBS Studio, Streamlabs, or Restream on iOS, Android, and Desktop.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // RTMP Ingest
                        Text(text = "RTMP Server Ingest URL", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131224))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = crossConfig.rtmpServerUrl, color = NeonCyan, fontSize = 12.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(crossConfig.rtmpServerUrl)) }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stream Key
                        Text(text = "Stream Key", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF131224))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = crossConfig.streamKey, color = NeonPink, fontSize = 12.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(crossConfig.streamKey)) }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonPink, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Multi-platform Restream Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Simulcast to Twitch & YouTube", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Broadcast simultaneously across platforms", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isRestreamingEnabled,
                                onCheckedChange = { isRestreamingEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonPink, checkedTrackColor = NeonPink.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }

    // VOD Replay Modal Dialog
    if (selectedVodForReplay != null) {
        val currentVod = selectedVodForReplay!!
        AlertDialog(
            onDismissRequest = { selectedVodForReplay = null },
            title = {
                Text(text = "📼 VOD Replay", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(text = currentVod.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Streamer: ${currentVod.streamerName}", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "Duration: ${currentVod.durationString}  •  ${currentVod.viewsCount} views", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1932)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Playing", tint = NeonPink, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Playback in 1080p60", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedVodForReplay = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("Close Replay")
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }
}

@Composable
private fun StatColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text(text = title, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun VodItemCard(
    vod: RecordedVod,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .testTag("vod_item_${vod.id}"),
        colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonPink.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play VOD",
                    tint = NeonPink,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vod.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${vod.durationString} • ${vod.viewsCount} views", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "• ${vod.dateString}", color = Color(0xFF64748B), fontSize = 11.sp)
                }
            }
        }
    }
}
