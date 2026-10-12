package com.example.agora

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import com.example.BuildConfig
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * AgoraManager provides the complete boilerplate infrastructure for Agora.io Real-Time
 * Communication (RTC) SDK v4.x on Android.
 *
 * Supports:
 * - Live video broadcasting (Streamer / Host role)
 * - Live video audience viewing (Viewer role)
 * - Channel join/leave with token or test mode
 * - Dynamic camera switching (front/back)
 * - Audio & Video muting controls
 * - Real-time network statistics (bitrate, FPS, packet loss)
 * - Clean lifecycle disposal
 */
class AgoraManager private constructor() {

    companion object {
        private const val TAG = "AgoraManager"

        @Volatile
        private var instance: AgoraManager? = null

        fun getInstance(): AgoraManager {
            return instance ?: synchronized(this) {
                instance ?: AgoraManager().also { instance = it }
            }
        }
    }

    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false

    private val _streamStats = MutableStateFlow(AgoraStreamStats())
    val streamStats: StateFlow<AgoraStreamStats> = _streamStats.asStateFlow()

    private val _remoteUsers = MutableStateFlow<List<Int>>(emptyList())
    val remoteUsers: StateFlow<List<Int>> = _remoteUsers.asStateFlow()

    private val _currentRole = MutableStateFlow(AgoraUserRole.AUDIENCE)
    val currentRole: StateFlow<AgoraUserRole> = _currentRole.asStateFlow()

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "onJoinChannelSuccess: channel=$channel, uid=$uid")
            _streamStats.update {
                it.copy(
                    channelName = channel ?: "",
                    localUid = uid,
                    connectionStatus = AgoraConnectionStatus.CONNECTED
                )
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "onUserJoined: remote uid=$uid")
            _remoteUsers.update { current ->
                if (!current.contains(uid)) current + uid else current
            }
            _streamStats.update {
                it.copy(remoteUid = uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "onUserOffline: uid=$uid, reason=$reason")
            _remoteUsers.update { current ->
                current.filter { it != uid }
            }
            if (_streamStats.value.remoteUid == uid) {
                _streamStats.update {
                    it.copy(remoteUid = _remoteUsers.value.firstOrNull())
                }
            }
        }

        override fun onRtcStats(stats: RtcStats?) {
            stats?.let { s ->
                _streamStats.update {
                    it.copy(
                        sendBitrateKbps = s.txKBitRate,
                        recvBitrateKbps = s.rxKBitRate,
                        packetLossRate = s.txPacketLossRate
                    )
                }
            }
        }

        override fun onLocalVideoStats(source: Constants.VideoSourceType?, stats: LocalVideoStats?) {
            stats?.let { s ->
                _streamStats.update {
                    it.copy(videoFps = s.encoderOutputFrameRate)
                }
            }
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora error: code=$err")
            if (err == Constants.ERR_TOKEN_EXPIRED || err == Constants.ERR_INVALID_TOKEN) {
                _streamStats.update { it.copy(connectionStatus = AgoraConnectionStatus.FAILED) }
            }
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.d(TAG, "onConnectionStateChanged: state=$state, reason=$reason")
            val status = when (state) {
                Constants.CONNECTION_STATE_CONNECTING -> AgoraConnectionStatus.CONNECTING
                Constants.CONNECTION_STATE_CONNECTED -> AgoraConnectionStatus.CONNECTED
                Constants.CONNECTION_STATE_RECONNECTING -> AgoraConnectionStatus.RECONNECTING
                Constants.CONNECTION_STATE_FAILED -> AgoraConnectionStatus.FAILED
                else -> AgoraConnectionStatus.DISCONNECTED
            }
            _streamStats.update { it.copy(connectionStatus = status) }
        }
    }

    /**
     * Resolves the Agora App ID from SharedPreferences, BuildConfig, or custom parameter.
     */
    fun getSavedAppId(context: Context): String {
        val sp = context.getSharedPreferences("agora_prefs", Context.MODE_PRIVATE)
        val saved = sp.getString("custom_app_id", "")?.trim() ?: ""
        if (saved.isBlank()) {
            val defaultAppId = "e118fbb5617f4580aa9564c1a5d6608b"
            sp.edit().putString("custom_app_id", defaultAppId).apply()
            return defaultAppId
        }
        return saved
    }

    fun getSavedToken(context: Context): String {
        val sp = context.getSharedPreferences("agora_prefs", Context.MODE_PRIVATE)
        val savedToken = sp.getString("custom_token", "")?.trim() ?: ""
        if (savedToken.isBlank()) {
            val defaultToken = "007eJxTYHirfjXYymiu1N1bX8PVnJOOWVoJq1cka1Sc33H5aaIg13QFhlRDQ4u0pCRTM0PzNBNTC4PEREtTM5Nkw0TTFDMzA4ukEtHTWQ2BjAyi65wZGKEQxOdk8M3PzFPIySxLZWAAAHGnHxc="
            sp.edit().putString("custom_token", defaultToken).apply()
            return defaultToken
        }
        return savedToken
    }

    fun saveAppId(context: Context, appId: String) {
        val sp = context.getSharedPreferences("agora_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("custom_app_id", appId.trim()).apply()
        if (rtcEngine != null) {
            destroy()
        }
    }

    fun hasValidAppId(context: Context): Boolean {
        return getEffectiveAppId(context).isNotBlank()
    }

    fun getEffectiveAppId(context: Context? = null, customAppId: String? = null): String {
        val custom = customAppId?.trim()?.takeIf { it.isNotBlank() }
        if (custom != null) return custom

        if (context != null) {
            val saved = getSavedAppId(context)
            if (saved.isNotBlank()) return saved
        }

        val configured = runCatching { BuildConfig.AGORA_APP_ID }.getOrNull()
        return if (configured.isNullOrBlank() || configured.contains("YOUR_AGORA_APP_ID")) {
            ""
        } else {
            configured.trim()
        }
    }

    /**
     * Initializes the Agora RtcEngine with live broadcasting profile.
     */
    fun initRtcEngine(context: Context, customAppId: String? = null): Boolean {
        if (isInitialized && rtcEngine != null) return true

        val appId = getEffectiveAppId(context, customAppId)
        if (appId.isBlank()) {
            Log.w(TAG, "Agora App ID is not configured in Secrets, SharedPreferences, or BuildConfig.")
            return false
        }

        return try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = appId
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            }
            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableVideo()
            rtcEngine?.enableAudio()
            isInitialized = true
            Log.i(TAG, "Agora RtcEngine initialized successfully with live profile.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora RtcEngine", e)
            false
        }
    }

    /**
     * Joins an Agora live broadcast channel as a Broadcaster (Streamer) or Audience (Viewer).
     */
    fun joinChannel(
        channelName: String,
        role: AgoraUserRole,
        token: String? = null,
        uid: Int = 0
    ): Boolean {
        val engine = rtcEngine ?: return false

        _currentRole.value = role
        _streamStats.update { it.copy(connectionStatus = AgoraConnectionStatus.CONNECTING) }

        try {
            val clientRole = when (role) {
                AgoraUserRole.BROADCASTER -> Constants.CLIENT_ROLE_BROADCASTER
                AgoraUserRole.AUDIENCE -> Constants.CLIENT_ROLE_AUDIENCE
            }
            engine.setClientRole(clientRole)

            val options = ChannelMediaOptions().apply {
                clientRoleType = clientRole
                channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                publishCameraTrack = (role == AgoraUserRole.BROADCASTER)
                publishMicrophoneTrack = (role == AgoraUserRole.BROADCASTER)
                autoSubscribeAudio = true
                autoSubscribeVideo = true
            }

            if (role == AgoraUserRole.BROADCASTER) {
                engine.startPreview()
            }

            val channelToken = token?.takeIf { it.isNotBlank() }
                ?: runCatching { 
                    val sp = rtcEngine?.let { 
                        // fallback or default token
                        "007eJxTYHirfjXYymiu1N1bX8PVnJOOWVoJq1cka1Sc33H5aaIg13QFhlRDQ4u0pCRTM0PzNBNTC4PEREtTM5Nkw0TTFDMzA4ukEtHTWQ2BjAyi65wZGKEQxOdk8M3PzFPIySxLZWAAAHGnHxc="
                    }
                    sp
                }.getOrNull()
            val result = engine.joinChannel(channelToken, channelName, uid, options)
            return result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error joining Agora channel $channelName", e)
            _streamStats.update { it.copy(connectionStatus = AgoraConnectionStatus.FAILED) }
            return false
        }
    }

    /**
     * Binds a local camera video feed into the given SurfaceView.
     */
    fun setupLocalVideo(surfaceView: SurfaceView) {
        val engine = rtcEngine ?: return
        try {
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            engine.setupLocalVideo(canvas)
            engine.startPreview()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup local video canvas", e)
        }
    }

    /**
     * Binds a remote broadcaster's video feed into the given SurfaceView.
     */
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        val engine = rtcEngine ?: return
        try {
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            engine.setupRemoteVideo(canvas)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup remote video canvas for uid=$uid", e)
        }
    }

    /**
     * Toggles between front and rear device cameras.
     */
    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    /**
     * Mutes or unmutes the local microphone.
     */
    fun toggleLocalAudio(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
        _streamStats.update { it.copy(isLocalAudioMuted = muted) }
    }

    /**
     * Enables or disables local camera capture.
     */
    fun toggleLocalVideo(muted: Boolean) {
        rtcEngine?.muteLocalVideoStream(muted)
        _streamStats.update { it.copy(isLocalVideoMuted = muted) }
    }

    /**
     * Leaves the current channel.
     */
    fun leaveChannel() {
        rtcEngine?.stopPreview()
        rtcEngine?.leaveChannel()
        _remoteUsers.value = emptyList()
        _streamStats.update {
            it.copy(
                channelName = "",
                localUid = 0,
                remoteUid = null,
                connectionStatus = AgoraConnectionStatus.DISCONNECTED
            )
        }
    }

    /**
     * Releases Agora RtcEngine resources.
     */
    fun destroy() {
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
        isInitialized = false
    }
}
