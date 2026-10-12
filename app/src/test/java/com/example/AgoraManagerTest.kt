package com.example

import com.example.agora.AgoraConnectionStatus
import com.example.agora.AgoraManager
import com.example.agora.AgoraStreamStats
import com.example.agora.AgoraUserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgoraManagerTest {

    @Test
    fun `test AgoraManager instance exists`() {
        val manager = AgoraManager.getInstance()
        assertNotNull(manager)
    }

    @Test
    fun `test initial Agora stream stats defaults`() {
        val stats = AgoraStreamStats()
        assertEquals("", stats.channelName)
        assertEquals(0, stats.localUid)
        assertEquals(null, stats.remoteUid)
        assertEquals(AgoraConnectionStatus.DISCONNECTED, stats.connectionStatus)
    }

    @Test
    fun `test Agora roles enum`() {
        assertEquals(2, AgoraUserRole.values().size)
        assertEquals(AgoraUserRole.BROADCASTER, AgoraUserRole.valueOf("BROADCASTER"))
        assertEquals(AgoraUserRole.AUDIENCE, AgoraUserRole.valueOf("AUDIENCE"))
    }
}
