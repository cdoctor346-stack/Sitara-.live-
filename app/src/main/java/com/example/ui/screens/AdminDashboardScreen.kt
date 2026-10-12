package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ManagedUser
import com.example.data.model.PlatformStats
import com.example.data.model.ReportItem
import com.example.data.model.StreamItem
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
fun AdminDashboardScreen(
    stats: PlatformStats,
    activeStreams: List<StreamItem>,
    users: List<ManagedUser>,
    reports: List<ReportItem>,
    onToggleBanUser: (String) -> Unit,
    onToggleVerifyUser: (String) -> Unit,
    onForceTerminateStream: (String) -> Unit,
    onResolveReport: (String, String) -> Unit,
    onUpdateUserRole: (String, String) -> Unit = { _, _ -> },
    onAdjustCoins: (String, Int) -> Unit = { _, _ -> },
    onSendGlobalAnnouncement: (String) -> Unit = {},
    maintenanceMode: Boolean = false,
    onToggleMaintenance: () -> Unit = {},
    slowModeActive: Boolean = false,
    onToggleSlowMode: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var userSearchQuery by remember { mutableStateOf("") }
    var userRoleFilter by remember { mutableStateOf("ALL") }
    var reportFilter by remember { mutableStateOf("ALL") }
    
    // Dialog States
    var streamToTerminate by remember { mutableStateOf<StreamItem?>(null) }
    var streamWarningTarget by remember { mutableStateOf<StreamItem?>(null) }
    var announcementText by remember { mutableStateOf("") }
    var announcementSentAlert by remember { mutableStateOf(false) }
    var userForRoleEdit by remember { mutableStateOf<ManagedUser?>(null) }
    var userForCoinsEdit by remember { mutableStateOf<ManagedUser?>(null) }
    var cacheSyncedAlert by remember { mutableStateOf(false) }

    // User filtering
    val filteredUsers = remember(users, userSearchQuery, userRoleFilter) {
        users.filter { user ->
            val matchRole = when (userRoleFilter) {
                "HOSTS" -> user.role == "Host"
                "VIPS" -> user.role == "VIP"
                "BANNED" -> user.isBanned
                "VERIFIED" -> user.isVerified
                else -> true
            }
            val matchSearch = userSearchQuery.isBlank() ||
                    user.username.contains(userSearchQuery, ignoreCase = true) ||
                    user.handle.contains(userSearchQuery, ignoreCase = true) ||
                    user.role.contains(userSearchQuery, ignoreCase = true)
            matchRole && matchSearch
        }
    }

    // Reports filtering
    val filteredReports = remember(reports, reportFilter) {
        when (reportFilter) {
            "PENDING" -> reports.filter { it.status == "PENDING" }
            "RESOLVED" -> reports.filter { it.status == "RESOLVED" || it.status == "BANNED" }
            "DISMISSED" -> reports.filter { it.status == "DISMISSED" }
            else -> reports
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
    ) {
        // Top Admin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonPink, NeonCyan)))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BrandDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Admin Security",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Admin Control Hub",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SUPERADMIN",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Platform Moderation, Security & Operations Console",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandDarkCard)
                        .testTag("admin_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Admin Panel",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Platform KPI Summary Cards (2x2 Grid)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Total Users",
                        value = "${stats.totalUsers / 1000}K",
                        subtitle = "Active worldwide",
                        icon = Icons.Default.People,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Active Live Streams",
                        value = "${activeStreams.size} LIVE",
                        subtitle = "1080p60 Cluster",
                        icon = Icons.Default.LiveTv,
                        accentColor = LiveRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Gifts Revenue",
                        value = "$${stats.totalRevenueDollars.toInt()}",
                        subtitle = "1.24M 🪙 traded",
                        icon = Icons.Default.Gavel,
                        accentColor = NeonAmber,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Reports Queue",
                        value = "${reports.count { it.status == "PENDING" }} Pending",
                        subtitle = "${reports.size} Total Cases",
                        icon = Icons.Default.Warning,
                        accentColor = NeonPink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Navigation Tabs (Streams, Users, Reports, System Controls)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Live (${activeStreams.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) NeonCyan else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Users (${users.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) NeonCyan else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Reports (${reports.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) NeonPink else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "Platform",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 3) NeonAmber else TextSecondary
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // TAB 0: Live Streams Monitor
                if (activeStreams.isEmpty()) {
                    item {
                        EmptyStateItem(message = "No streams currently live on platform.")
                    }
                } else {
                    items(activeStreams, key = { it.id }) { stream ->
                        ActiveStreamModerationCard(
                            stream = stream,
                            onTerminate = { streamToTerminate = stream },
                            onWarn = { streamWarningTarget = stream }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            1 -> {
                // TAB 1: User Management
                item {
                    Column {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search users by username, handle...", color = Color.Gray, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_user_search"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF2E2C4D),
                                focusedContainerColor = BrandDarkCard,
                                unfocusedContainerColor = BrandDarkCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role Filter Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            val roleOptions = listOf("ALL" to "All Users", "HOSTS" to "Streamers", "VIPS" to "VIPs", "VERIFIED" to "Verified", "BANNED" to "Banned")
                            items(roleOptions) { (key, label) ->
                                val isSelected = userRoleFilter == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else BrandDarkCard)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonCyan else Color(0xFF2B284B),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { userRoleFilter = key }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        EmptyStateItem(message = "No users match this filter query.")
                    }
                } else {
                    items(filteredUsers, key = { it.id }) { user ->
                        ManagedUserCard(
                            user = user,
                            onToggleBan = { onToggleBanUser(user.id) },
                            onToggleVerify = { onToggleVerifyUser(user.id) },
                            onEditRole = { userForRoleEdit = user },
                            onAdjustCoins = { userForCoinsEdit = user }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            2 -> {
                // TAB 2: User Reports Moderation
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        val reportTabs = listOf("ALL" to "All (${reports.size})", "PENDING" to "Pending (${reports.count { it.status == "PENDING" }})", "RESOLVED" to "Resolved", "DISMISSED" to "Dismissed")
                        items(reportTabs) { (key, label) ->
                            val isSelected = reportFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) NeonPink.copy(alpha = 0.2f) else BrandDarkCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonPink else Color(0xFF2B284B),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { reportFilter = key }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonPink else TextSecondary
                                )
                            }
                        }
                    }
                }

                if (filteredReports.isEmpty()) {
                    item {
                        EmptyStateItem(message = "No reports found under this filter.")
                    }
                } else {
                    items(filteredReports, key = { it.id }) { report ->
                        ReportItemCard(
                            report = report,
                            onResolve = { action -> onResolveReport(report.id, action) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            3 -> {
                // TAB 3: System Announcements, Maintenance & Platform Controls
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Global Announcement Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandDarkCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send Global Banner to All Live Rooms",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "This priority message will instantly broadcast across all active viewer chat rooms and studio monitors.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = announcementText,
                                    onValueChange = { announcementText = it },
                                    placeholder = { Text("E.g. Scheduled server maintenance at 04:00 UTC...", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp),
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = NeonAmber,
                                        unfocusedBorderColor = Color(0xFF374151),
                                        focusedContainerColor = Color(0xFF131127),
                                        unfocusedContainerColor = Color(0xFF131127)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (announcementText.isNotBlank()) {
                                            onSendGlobalAnnouncement(announcementText)
                                            announcementSentAlert = true
                                            announcementText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Broadcast Announcement 📢", color = Color(0xFF0F0E26), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Operational Switches Card (Maintenance & Slow Mode)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandDarkCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Platform Operational Controls",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))

                                // Maintenance Mode Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Emergency Maintenance Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Pauses new broadcasts and displays service banner", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = maintenanceMode,
                                        onCheckedChange = { onToggleMaintenance() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = LiveRed
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Chat Slow Mode Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Chat Slow Mode (5-sec throttle)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Mitigates chat flooding during massive hype moments", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = slowModeActive,
                                        onCheckedChange = { onToggleSlowMode() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = NeonCyan
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Cache / Cluster Resync Button
                                Button(
                                    onClick = { cacheSyncedAlert = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24224A)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Purge Room Cache & Rebalance CDN", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Financial & Split Model Breakdown Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandDarkCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Financial Distribution & Revenue Split",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Streamer Diamond Payout:", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "75% ($18,637)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Platform Commission Fee:", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "25% ($6,213)", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Edge Cluster Health:", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = "99.99% (Tokyo / US-East / EU)", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Force Terminate Stream Confirmation Dialog
    if (streamToTerminate != null) {
        val target = streamToTerminate!!
        var terminationReason by remember { mutableStateOf("Inappropriate Content / Terms Violation") }

        AlertDialog(
            onDismissRequest = { streamToTerminate = null },
            title = {
                Text(text = "Force Terminate Stream? ⛔", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to stop this broadcast immediately? The host and viewers will be disconnected.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Room: ${target.title}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Host: ${target.streamerName} (@${target.streamerHandle})", color = TextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Select Infraction Category:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val reasons = listOf("Inappropriate Adult Content", "Copyright Music Violation", "Severe Harassment", "Spam / Fraud Promotion")
                    reasons.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { terminationReason = r }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (terminationReason == r) "🔘 $r" else "⚪ $r",
                                color = if (terminationReason == r) LiveRed else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onForceTerminateStream(target.id)
                        streamToTerminate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                ) {
                    Text("Terminate Now ⛔")
                }
            },
            dismissButton = {
                TextButton(onClick = { streamToTerminate = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }

    // Stream Warning Sent Dialog
    if (streamWarningTarget != null) {
        val target = streamWarningTarget!!
        AlertDialog(
            onDismissRequest = { streamWarningTarget = null },
            title = { Text("Streamer Warning Dispatched ⚠️", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("A priority content advisory notice has been sent to ${target.streamerName}. If violations persist, force-terminate the stream.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { streamWarningTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
                ) {
                    Text("Done", color = Color(0xFF0F0E26), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }

    // Edit Role Dialog
    if (userForRoleEdit != null) {
        val targetUser = userForRoleEdit!!
        val roles = listOf("User", "Host", "VIP", "Moderator", "Superadmin")

        AlertDialog(
            onDismissRequest = { userForRoleEdit = null },
            title = { Text("Update Role: ${targetUser.username}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a permission role for this account:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    roles.forEach { r ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (targetUser.role == r) NeonCyan.copy(alpha = 0.2f) else Color(0xFF1C1A36))
                                .clickable {
                                    onUpdateUserRole(targetUser.id, r)
                                    userForRoleEdit = null
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (targetUser.role == r) "✓ $r (Current)" else r,
                                color = if (targetUser.role == r) NeonCyan else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { userForRoleEdit = null }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }

    // Adjust Coins Dialog
    if (userForCoinsEdit != null) {
        val targetUser = userForCoinsEdit!!
        AlertDialog(
            onDismissRequest = { userForCoinsEdit = null },
            title = { Text("Adjust Wallet: ${targetUser.username}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Current Balance: ${targetUser.coins} 🪙 Coins", color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Quick balance adjustment operations:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onAdjustCoins(targetUser.id, 500)
                                userForCoinsEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+500 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                onAdjustCoins(targetUser.id, 2000)
                                userForCoinsEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+2000 🪙", color = Color(0xFF0F0E26), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                onAdjustCoins(targetUser.id, -500)
                                userForCoinsEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-500 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { userForCoinsEdit = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }

    // Announcement Alert
    if (announcementSentAlert) {
        AlertDialog(
            onDismissRequest = { announcementSentAlert = false },
            title = { Text("Announcement Pushed! 📢", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Your platform alert was broadcasted to all active viewers and broadcasters successfully.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { announcementSentAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("OK", color = Color(0xFF0F0E26), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }

    // CDN Resync Alert
    if (cacheSyncedAlert) {
        AlertDialog(
            onDismissRequest = { cacheSyncedAlert = false },
            title = { Text("CDN Cache Rebalanced ✓", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Global edge servers in Tokyo, Frankfurt, and Virginia reloaded room manifests and purged stale sockets.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { cacheSyncedAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("OK", color = Color(0xFF0F0E26), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF16152B)
        )
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextSecondary, fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            if (subtitle != null) {
                Text(text = subtitle, color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActiveStreamModerationCard(
    stream: StreamItem,
    onTerminate: () -> Unit,
    onWarn: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF262447), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(LiveRed)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE NOW",
                            color = LiveRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${stream.viewerCount} Viewers",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${stream.streamResolution}",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stream.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Host: ${stream.streamerName}  •  ${stream.category}  •  ${stream.bitrateKbps} kbps",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onWarn,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Warn", fontSize = 11.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onTerminate,
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Terminate", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagedUserCard(
    user: ManagedUser,
    onToggleBan: () -> Unit,
    onToggleVerify: () -> Unit,
    onEditRole: () -> Unit,
    onAdjustCoins: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (user.isBanned) LiveRed.copy(alpha = 0.5f) else Color(0xFF262447),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBanned) Color(0xFF2A1522) else BrandDarkCard
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.username,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = NeonCyan, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E1B38))
                                .clickable { onEditRole() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "${user.role} ▾", color = NeonAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (user.isBanned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LiveRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(text = "BANNED", color = LiveRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = user.handle, color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.followers} Followers  •  ${user.coins} 🪙  •  ${user.diamonds} 💎",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Coins button
                    IconButton(
                        onClick = onAdjustCoins,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("🪙", fontSize = 14.sp)
                    }

                    // Verify Badge Toggle
                    IconButton(
                        onClick = onToggleVerify,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.CheckCircle,
                            contentDescription = "Toggle Verification",
                            tint = if (user.isVerified) NeonCyan else Color(0xFF4B5563),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Ban / Unban Button
                    Button(
                        onClick = onToggleBan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isBanned) Color(0xFF10B981) else LiveRed
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isBanned) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user.isBanned) "Unban" else "Ban",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportItemCard(
    report: ReportItem,
    onResolve: (String) -> Unit
) {
    val isPending = report.status == "PENDING"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isPending) NeonPink.copy(alpha = 0.4f) else Color(0xFF262447),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = BrandDarkCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = report.reason, color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "• ${report.timestamp}", color = Color.Gray, fontSize = 10.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (report.status) {
                                "PENDING" -> Color(0xFFEF4444)
                                "BANNED" -> LiveRed
                                "DISMISSED" -> Color(0xFF4B5563)
                                else -> Color(0xFF10B981)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = report.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reported User: ${report.reportedUsername}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Reported by: ${report.reporterName}",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF131127))
                    .padding(8.dp)
            ) {
                Text(
                    text = "\"${report.messageText}\"",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onResolve("BANNED") },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Ban User 🚫", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onResolve("RESOLVED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Resolve ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onResolve("DISMISSED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text("Dismiss ✕", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateItem(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandDarkCard),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextSecondary, fontSize = 13.sp)
    }
}
