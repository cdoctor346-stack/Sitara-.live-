package com.example.agora

enum class AgoraUserRole {
    BROADCASTER,
    AUDIENCE
}

enum class AgoraConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

data class AgoraStreamStats(
    val channelName: String = "",
    val localUid: Int = 0,
    val remoteUid: Int? = null,
    val sendBitrateKbps: Int = 0,
    val recvBitrateKbps: Int = 0,
    val videoFps: Int = 0,
    val packetLossRate: Int = 0,
    val isLocalAudioMuted: Boolean = false,
    val isLocalVideoMuted: Boolean = false,
    val connectionStatus: AgoraConnectionStatus = AgoraConnectionStatus.DISCONNECTED
)
