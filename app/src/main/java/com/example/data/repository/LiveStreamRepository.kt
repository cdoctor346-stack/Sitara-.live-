package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.StreamSettingsEntity
import com.example.data.local.VodEntity
import com.example.data.model.ChatMessage
import com.example.data.model.CrossPlatformStreamConfig
import com.example.data.model.GiftItem
import com.example.data.model.RecordedVod
import com.example.data.model.ScheduledStream
import com.example.data.model.ManagedUser
import com.example.data.model.PlatformStats
import com.example.data.model.ReportItem
import com.example.data.model.StreamItem
import com.example.data.model.StreamerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class LiveStreamRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    // User wallet state
    private val _coinsBalance = MutableStateFlow(1250)
    val coinsBalance: StateFlow<Int> = _coinsBalance.asStateFlow()

    private val _diamondsBalance = MutableStateFlow(480)
    val diamondsBalance: StateFlow<Int> = _diamondsBalance.asStateFlow()

    // Stream list
    private val _liveStreams = MutableStateFlow<List<StreamItem>>(emptyList())
    val liveStreams: StateFlow<List<StreamItem>> = _liveStreams.asStateFlow()

    // Active watching stream
    private val _activeStream = MutableStateFlow<StreamItem?>(null)
    val activeStream: StateFlow<StreamItem?> = _activeStream.asStateFlow()

    // Chat messages for active stream
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Cross-platform configuration
    private val _crossPlatformConfig = MutableStateFlow(CrossPlatformStreamConfig())
    val crossPlatformConfig: StateFlow<CrossPlatformStreamConfig> = _crossPlatformConfig.asStateFlow()

    // Schedule
    private val _scheduleList = MutableStateFlow<List<ScheduledStream>>(emptyList())
    val scheduleList: StateFlow<List<ScheduledStream>> = _scheduleList.asStateFlow()

    // VODs list
    private val _vodsList = MutableStateFlow<List<RecordedVod>>(emptyList())
    val vodsList: StateFlow<List<RecordedVod>> = _vodsList.asStateFlow()

    // Streamer Profile (User's profile)
    private val _userProfile = MutableStateFlow(
        StreamerProfile(
            id = "user_sitara_creator",
            username = "Sitara Streamer",
            handle = "@sitara_live_official",
            bio = "Official Sitara Live broadcast host 🎙️ 4K/60FPS daily gaming & live IRL tech adventures. Welcome to the vibe squad!",
            followers = 184500,
            following = 142,
            diamonds = 480,
            coins = 1250,
            isVerified = true,
            totalStreams = 112,
            avatarRes = R.drawable.creator_moin
        )
    )
    val userProfile: StateFlow<StreamerProfile> = _userProfile.asStateFlow()

    // Admin & Moderation States
    private val _allUsers = MutableStateFlow(
        listOf(
            ManagedUser("u_1", "Maya Plays", "@maya_plays", role = "Host", isVerified = true, coins = 4200, diamonds = 18900, followers = 48200),
            ManagedUser("u_2", "DJ Kairo", "@kairo_beats", role = "Host", isVerified = true, coins = 1500, diamonds = 9200, followers = 14300),
            ManagedUser("u_3", "TrollAccount99", "@troll99", role = "User", isBanned = true, coins = 50, diamonds = 0, followers = 3),
            ManagedUser("u_4", "CyberSamurai", "@cyber_sam", role = "VIP", isVerified = false, coins = 8900, diamonds = 450, followers = 890),
            ManagedUser("u_5", "Alex Wander", "@alex_irl", role = "Host", isVerified = true, coins = 2300, diamonds = 12400, followers = 31200)
        )
    )
    val allUsers: StateFlow<List<ManagedUser>> = _allUsers.asStateFlow()

    private val _reportQueue = MutableStateFlow(
        listOf(
            ReportItem("rep_1", "TrollAccount99", "PixelQueen", "Spamming inappropriate links in chat!", reason = "Spam / Links", timestamp = "4m ago", status = "PENDING"),
            ReportItem("rep_2", "AnonymousViewer", "NovaCaptain", "Abusive language towards streamer in round 4", reason = "Harassment", timestamp = "18m ago", status = "PENDING"),
            ReportItem("rep_3", "FakeBot12", "Dev Sarah", "Promoting coin scams", reason = "Scam / Fraud", timestamp = "1h ago", status = "RESOLVED")
        )
    )
    val reportQueue: StateFlow<List<ReportItem>> = _reportQueue.asStateFlow()

    private val _platformStats = MutableStateFlow(PlatformStats())
    val platformStats: StateFlow<PlatformStats> = _platformStats.asStateFlow()

    private val _maintenanceMode = MutableStateFlow(false)
    val maintenanceMode: StateFlow<Boolean> = _maintenanceMode.asStateFlow()

    private val _slowModeActive = MutableStateFlow(false)
    val slowModeActive: StateFlow<Boolean> = _slowModeActive.asStateFlow()

    init {
        initializeSampleData()
        loadPersistedVods()
    }

    private fun initializeSampleData() {
        val initialStreams = listOf(
            StreamItem(
                id = "stream_1",
                title = "🔥 GRAND FINALS: Neon Battlegrounds Pro League 2026",
                streamerName = "Maya Plays",
                streamerHandle = "@maya_plays",
                bannerRes = R.drawable.stream_hero,
                streamerAvatarRes = R.drawable.creator_moin,
                viewerCount = 14820,
                category = "Gaming",
                tags = listOf("Esports", "NeonFPS", "Ranked", "GrandFinals"),
                startedAgo = "42m ago",
                likesCount = 89200,
                streamResolution = "1080p60",
                bitrateKbps = 6400,
                isFollowing = true,
                streamDescription = "Team Nova vs Apex Predators in Map 5 Decider! Live casting with real-time stats and drops enabled."
            ),
            StreamItem(
                id = "stream_2",
                title = "Cyberpunk Synthwave DJ Set Live from Tokyo Shibuya",
                streamerName = "DJ Kairo",
                streamerHandle = "@kairo_beats",
                bannerRes = R.drawable.stream_hero,
                streamerAvatarRes = R.drawable.creator_moin,
                viewerCount = 8340,
                category = "Music & DJs",
                tags = listOf("LiveDJ", "Synthwave", "Tokyo", "NightDrive"),
                startedAgo = "1h 10m ago",
                likesCount = 45200,
                streamResolution = "1080p60",
                bitrateKbps = 6100,
                isFollowing = false,
                streamDescription = "Late night chill coding & driving beats with analog synthesizers. Drop song requests in chat!"
            ),
            StreamItem(
                id = "stream_3",
                title = "IRL Night Market Food Crawl in Seoul 🍜 Street Food",
                streamerName = "Alex Wander",
                streamerHandle = "@alex_irl",
                bannerRes = R.drawable.stream_hero,
                streamerAvatarRes = R.drawable.creator_moin,
                viewerCount = 6120,
                category = "IRL & Travel",
                tags = listOf("IRL", "Seoul", "StreetFood", "Travel"),
                startedAgo = "25m ago",
                likesCount = 31400,
                streamResolution = "720p60",
                bitrateKbps = 4800,
                isFollowing = false,
                streamDescription = "Trying spicy tteokbokki, grilled skewers, and night vibes in Hongdae with chat interactive polls."
            ),
            StreamItem(
                id = "stream_4",
                title = "Building an AI-Powered Mobile App in 2 Hours! Live Code",
                streamerName = "Dev Sarah",
                streamerHandle = "@sarah_codes",
                bannerRes = R.drawable.stream_hero,
                streamerAvatarRes = R.drawable.creator_moin,
                viewerCount = 3980,
                category = "Tech & Talks",
                tags = listOf("Coding", "Android", "Kotlin", "LiveDev"),
                startedAgo = "18m ago",
                likesCount = 18900,
                streamResolution = "1080p60",
                bitrateKbps = 5500,
                isFollowing = true,
                streamDescription = "Live coding session! Answering viewer questions on architecture, jetpack compose, and production deployment."
            ),
            StreamItem(
                id = "stream_5",
                title = "Chill Lofi Guitar Jam & Q&A with Viewers ☕",
                streamerName = "Liam Strings",
                streamerHandle = "@liam_strings",
                bannerRes = R.drawable.stream_hero,
                streamerAvatarRes = R.drawable.creator_moin,
                viewerCount = 2150,
                category = "Music & DJs",
                tags = listOf("Acoustic", "Lofi", "Chill", "Interactive"),
                startedAgo = "55m ago",
                likesCount = 12700,
                streamResolution = "1080p30",
                bitrateKbps = 4500,
                isFollowing = false,
                streamDescription = "Warm coffee, fingerstyle guitar melodies, relaxing chats after a busy week."
            )
        )
        _liveStreams.value = initialStreams
        _activeStream.value = initialStreams.first()

        _chatMessages.value = listOf(
            ChatMessage(UUID.randomUUID().toString(), "Kairo_Fan", "Let's goooo! What an insane round 🔥"),
            ChatMessage(UUID.randomUUID().toString(), "NovaCaptain", "Nova needs to hold the B site!", isSub = true),
            ChatMessage(UUID.randomUUID().toString(), "SitaraMod", "Welcome all 14,000+ viewers! Keep chat positive ❤️", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "PixelQueen", "Doctor sent a Space Rocket! 🚀", giftNotice = "Space Rocket 🚀"),
            ChatMessage(UUID.randomUUID().toString(), "CyberSamurai", "That crosshair placement was surgical"),
            ChatMessage(UUID.randomUUID().toString(), "Maya Plays", "Thank you everyone for the hype!! Clutch coming!", isStreamer = true)
        )

        _scheduleList.value = listOf(
            ScheduledStream("sch_1", "Apex Season 24 Invitational Qualifier", "Maya Plays", "Gaming", "Tomorrow, 8:00 PM", "8:00 PM EST", true),
            ScheduledStream("sch_2", "Tokyo Sunset to Neon Twilight Walk", "Alex Wander", "IRL & Travel", "Saturday, 5:00 AM", "6:00 PM JST", false),
            ScheduledStream("sch_3", "Weekly Creator Workshop & AMA", "Sitara Streamer", "Tech & Talks", "Sunday, 2:00 PM", "2:00 PM EST", true),
            ScheduledStream("sch_4", "Electronic Synth Sessions Live", "DJ Kairo", "Music & DJs", "Monday, 9:00 PM", "9:00 PM GMT", false)
        )

        _vodsList.value = listOf(
            RecordedVod("vod_1", "Pro League Semifinals Matchday 3", "Maya Plays", "2h 45m", "Yesterday", 45200, "Gaming"),
            RecordedVod("vod_2", "Midnight Cyberpunk Synth Session Vol. 4", "DJ Kairo", "1h 50m", "3 days ago", 28100, "Music & DJs"),
            RecordedVod("vod_3", "Exploring Ancient Temples in Kyoto", "Alex Wander", "3h 12m", "5 days ago", 39000, "IRL & Travel"),
            RecordedVod("vod_4", "Compose Multiplatform Full Stack Architecture", "Dev Sarah", "1h 35m", "1 week ago", 19500, "Tech & Talks")
        )
    }

    private fun loadPersistedVods() {
        scope.launch {
            database.streamDao().getAllVods().collect { entities ->
                if (entities.isNotEmpty()) {
                    val converted = entities.map { entity ->
                        RecordedVod(
                            id = entity.id,
                            title = entity.title,
                            streamerName = entity.streamerName,
                            durationString = entity.durationString,
                            dateString = entity.dateString,
                            viewsCount = entity.viewsCount,
                            category = entity.category
                        )
                    }
                    _vodsList.update { current ->
                        (converted + current).distinctBy { it.id }
                    }
                }
            }
        }
    }

    fun selectStream(stream: StreamItem) {
        _activeStream.value = stream
        // Load stream specific chats
        _chatMessages.value = listOf(
            ChatMessage(UUID.randomUUID().toString(), "StreamFan", "Hi ${stream.streamerName}! Tuning in from Android"),
            ChatMessage(UUID.randomUUID().toString(), "SitaraMod", "Welcome to ${stream.title}!", isModerator = true),
            ChatMessage(UUID.randomUUID().toString(), "NeonHype", "Audio and 1080p stream looking crisp ✨", isSub = true)
        )
    }

    fun sendChatMessage(text: String, senderName: String = "You") {
        if (text.isBlank()) return
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            text = text,
            timestamp = "Just now"
        )
        _chatMessages.update { it + newMsg }
    }

    fun sendGift(gift: GiftItem): Boolean {
        if (_coinsBalance.value < gift.coinCost) {
            return false
        }
        _coinsBalance.update { it - gift.coinCost }
        _diamondsBalance.update { it + (gift.coinCost / 2) }

        val giftMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "You",
            text = "sent ${gift.name} ${gift.iconEmoji}!",
            giftNotice = "${gift.name} ${gift.iconEmoji}"
        )
        _chatMessages.update { it + giftMsg }
        return true
    }

    fun addCoins(amount: Int) {
        _coinsBalance.update { it + amount }
    }

    fun toggleFollow(streamId: String) {
        _liveStreams.update { list ->
            list.map { item ->
                if (item.id == streamId) item.copy(isFollowing = !item.isFollowing) else item
            }
        }
        if (_activeStream.value?.id == streamId) {
            _activeStream.update { it?.copy(isFollowing = !(it.isFollowing)) }
        }
    }

    fun toggleScheduleReminder(scheduleId: String) {
        _scheduleList.update { list ->
            list.map { item ->
                if (item.id == scheduleId) item.copy(isReminderSet = !item.isReminderSet) else item
            }
        }
    }

    fun updateCrossPlatformConfig(newConfig: CrossPlatformStreamConfig) {
        _crossPlatformConfig.value = newConfig
    }

    fun recordNewBroadcastVod(title: String, durationStr: String, category: String) {
        val newVod = RecordedVod(
            id = "broadcast_" + System.currentTimeMillis(),
            title = title,
            streamerName = _userProfile.value.username,
            durationString = durationStr,
            dateString = "Just now",
            viewsCount = 142,
            category = category
        )
        _vodsList.update { listOf(newVod) + it }
        scope.launch {
            database.streamDao().insertVod(
                VodEntity(
                    id = newVod.id,
                    title = newVod.title,
                    streamerName = newVod.streamerName,
                    durationString = newVod.durationString,
                    dateString = newVod.dateString,
                    viewsCount = newVod.viewsCount,
                    category = newVod.category
                )
            )
        }
    }

    val availableGifts = listOf(
        GiftItem("gift_rose", "Neon Rose", "🌹", 5),
        GiftItem("gift_heart", "Cyber Heart", "💖", 20),
        GiftItem("gift_mic", "Golden Mic", "🎤", 50),
        GiftItem("gift_game", "Pro Controller", "🎮", 100),
        GiftItem("gift_crown", "Royal Crown", "👑", 500),
        GiftItem("gift_rocket", "Space Rocket", "🚀", 1000)
    )

    fun updateUserProfile(newName: String, newBio: String, newAvatar: android.graphics.Bitmap?) {
        _userProfile.update {
            it.copy(
                username = newName,
                bio = newBio
            )
        }
    }

    fun toggleUserBan(userId: String) {
        _allUsers.update { list ->
            list.map { user ->
                if (user.id == userId) user.copy(isBanned = !user.isBanned) else user
            }
        }
    }

    fun toggleUserVerification(userId: String) {
        _allUsers.update { list ->
            list.map { user ->
                if (user.id == userId) user.copy(isVerified = !user.isVerified) else user
            }
        }
    }

    fun forceTerminateStream(streamId: String) {
        _liveStreams.update { list ->
            list.filter { it.id != streamId }
        }
        _platformStats.update { it.copy(activeLiveStreams = (it.activeLiveStreams - 1).coerceAtLeast(0)) }
    }

    fun resolveReport(reportId: String, newStatus: String = "RESOLVED") {
        _reportQueue.update { list ->
            list.map { report ->
                if (report.id == reportId) report.copy(status = newStatus) else report
            }
        }
        _platformStats.update { it.copy(pendingReports = _reportQueue.value.count { r -> r.status == "PENDING" }) }
    }

    fun submitReport(reportedUsername: String, messageText: String, reason: String) {
        val newRep = ReportItem(
            id = "rep_" + System.currentTimeMillis(),
            reportedUsername = reportedUsername,
            reporterName = "You",
            messageText = messageText,
            reason = reason,
            timestamp = "Just now",
            status = "PENDING"
        )
        _reportQueue.update { listOf(newRep) + it }
        _platformStats.update { it.copy(pendingReports = it.pendingReports + 1) }
    }

    fun updateUserRole(userId: String, newRole: String) {
        _allUsers.update { list ->
            list.map { user ->
                if (user.id == userId) user.copy(role = newRole) else user
            }
        }
    }

    fun adjustUserCoins(userId: String, deltaCoins: Int) {
        _allUsers.update { list ->
            list.map { user ->
                if (user.id == userId) user.copy(coins = (user.coins + deltaCoins).coerceAtLeast(0)) else user
            }
        }
    }

    fun toggleMaintenanceMode() {
        _maintenanceMode.update { !it }
    }

    fun toggleSlowMode() {
        _slowModeActive.update { !it }
    }

    fun sendGlobalAnnouncement(messageText: String) {
        val announcement = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "🛡️ SUPERADMIN",
            text = "ANNOUNCEMENT: $messageText",
            isModerator = true
        )
        _chatMessages.update { it + announcement }
    }
}
