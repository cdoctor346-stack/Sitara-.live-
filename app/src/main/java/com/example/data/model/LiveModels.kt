package com.example.data.model

data class StreamItem(
    val id: String,
    val title: String,
    val streamerName: String,
    val streamerHandle: String,
    val streamerAvatar: String = "",
    val streamerAvatarRes: Int? = null,
    val bannerRes: Int? = null,
    val viewerCount: Int,
    val category: String,
    val tags: List<String> = emptyList(),
    val isLive: Boolean = true,
    val startedAgo: String = "24m ago",
    val likesCount: Int = 1240,
    val streamResolution: String = "1080p60",
    val bitrateKbps: Int = 5800,
    val isFollowing: Boolean = false,
    val streamDescription: String = "Welcome to the broadcast! Type in chat to interact, send gifts, or join the discussion."
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: String = "Just now",
    val isStreamer: Boolean = false,
    val isModerator: Boolean = false,
    val isSub: Boolean = false,
    val giftNotice: String? = null
)

data class GiftItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val coinCost: Int,
    val effectTag: String = "sparkle"
)

data class StreamerProfile(
    val id: String,
    val username: String,
    val handle: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val diamonds: Int,
    val coins: Int,
    val isVerified: Boolean = true,
    val totalStreams: Int = 84,
    val avatarRes: Int? = null
)

data class ScheduledStream(
    val id: String,
    val title: String,
    val streamerName: String,
    val category: String,
    val dateString: String,
    val timeString: String,
    val isReminderSet: Boolean = false
)

data class RecordedVod(
    val id: String,
    val title: String,
    val streamerName: String,
    val durationString: String,
    val dateString: String,
    val viewsCount: Int,
    val category: String
)

data class CrossPlatformStreamConfig(
    val rtmpServerUrl: String = "rtmp://live.sitaralive.tv/app",
    val streamKey: String = "live_sk_sitara_8923a4b912ecf",
    val srtServerUrl: String = "srt://ingest.sitaralive.tv:9710?streamid=sitara_live",
    val targetPlatform: String = "Sitara Live + Cross-Stream Bridge (Twitch/YouTube)",
    val isRestreamActive: Boolean = true,
    val selectedResolution: String = "1080p60",
    val selectedBitrate: Int = 6000
)

data class ManagedUser(
    val id: String,
    val username: String,
    val handle: String,
    val role: String = "User", // "User", "Host", "Admin", "VIP"
    val isBanned: Boolean = false,
    val isVerified: Boolean = false,
    val coins: Int = 500,
    val diamonds: Int = 0,
    val followers: Int = 120
)

data class ReportItem(
    val id: String,
    val reportedUsername: String,
    val reporterName: String,
    val messageText: String,
    val reason: String = "Inappropriate Behavior", // "Harassment", "Spam", "Underage", "Nudity"
    val timestamp: String = "10m ago",
    val status: String = "PENDING" // "PENDING", "RESOLVED", "BANNED", "DISMISSED"
)

data class PlatformStats(
    val totalUsers: Int = 184520,
    val activeLiveStreams: Int = 42,
    val totalRevenueDollars: Double = 24850.00,
    val totalCoinsTraded: Int = 1240000,
    val pendingReports: Int = 4
)

