package com.example.agora

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Renders local broadcaster camera feed using Agora VideoCanvas.
 */
@Composable
fun AgoraLocalVideoView(
    modifier: Modifier = Modifier,
    agoraManager: AgoraManager = AgoraManager.getInstance()
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                setZOrderMediaOverlay(true)
                agoraManager.setupLocalVideo(this)
            }
        },
        modifier = modifier
    )
}

/**
 * Renders remote broadcaster feed for audience viewing using Agora VideoCanvas.
 */
@Composable
fun AgoraRemoteVideoView(
    remoteUid: Int,
    modifier: Modifier = Modifier,
    agoraManager: AgoraManager = AgoraManager.getInstance()
) {
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                agoraManager.setupRemoteVideo(this, remoteUid)
            }
        },
        update = { surfaceView ->
            agoraManager.setupRemoteVideo(surfaceView, remoteUid)
        },
        modifier = modifier
    )
}

/**
 * Overlay indicator displaying Agora connection telemetry or setup assistance.
 */
@Composable
fun AgoraLiveTelemetryBadge(
    channelName: String,
    role: AgoraUserRole,
    modifier: Modifier = Modifier,
    agoraManager: AgoraManager = AgoraManager.getInstance()
) {
    val stats by agoraManager.streamStats.collectAsState()
    val isAppIdConfigured = remember { agoraManager.getEffectiveAppId().isNotBlank() }

    if (!isAppIdConfigured) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xD91F1838)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Agora.io Real-Time Engine Ready",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add AGORA_APP_ID in AI Studio Secrets panel to connect to live SD-RTN.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xCC0E0C22))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (stats.connectionStatus) {
                            AgoraConnectionStatus.CONNECTED -> Color(0xFF10B981)
                            AgoraConnectionStatus.CONNECTING -> NeonAmber
                            AgoraConnectionStatus.RECONNECTING -> NeonAmber
                            else -> LiveRed
                        }
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Agora ${role.name}: $channelName",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (stats.videoFps > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${stats.videoFps} FPS",
                    color = NeonCyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}
