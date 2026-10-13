package com.example.ui.util

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareLiveStream(
        context: Context,
        hostName: String = "Sitara Live",
        streamTitle: String = "Live PK Battle & Guest Call",
        isPk: Boolean = false
    ) {
        val shareMessage = if (isPk) {
            "🔥 Join my LIVE PK BATTLE on Sitara Live! ⚔️\n" +
            "Host: $hostName is battling in PK right now! Tap to cheer & send gifts to win!\n" +
            "🎙️ Join Guest Call & watch: https://sitaralive.app/pk-live\n" +
            "📲 Download & Install Sitara Live APK: https://sitaralive.app/download"
        } else {
            "🔴 I'm LIVE on Sitara Live! 🎙️\n" +
            "Title: $streamTitle\n" +
            "Join my guest call or chat with me live! 🚀\n" +
            "📲 Download & Install Sitara Live APK: https://sitaralive.app/download"
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join $hostName on Sitara Live!")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Live Stream & APK via")
        context.startActivity(chooser)
    }

    fun shareAppApk(context: Context) {
        val shareMessage = "✨ Sitara Live - Live PK Battles, Multi-Guest Calls & Streaming! 🌟\n\n" +
                "Install the latest Sitara Live APK v1.1 to go live, play PK matches, and talk with friends in guest rooms!\n" +
                "📲 Download APK: https://sitaralive.app/download"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Download Sitara Live App APK")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Sitara Live APK with Friends")
        context.startActivity(chooser)
    }
}
