package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

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

        copyToClipboard(context, "Sitara Live Stream", shareMessage)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join $hostName on Sitara Live!")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Live Stream via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Link copied to clipboard! (Paste in WhatsApp/SMS)", Toast.LENGTH_LONG).show()
        }
    }

    fun shareAppApk(context: Context) {
        val shareMessage = "✨ Sitara Live - Live PK Battles, Multi-Guest Calls & Streaming! 🌟\n\n" +
                "Install the latest Sitara Live APK to go live, play PK matches, and talk with friends in guest rooms!\n" +
                "📲 Download APK: https://sitaralive.app/download\n" +
                "🎮 Watch live streams & join voice lounges together!"

        copyToClipboard(context, "Sitara Live APK Link", shareMessage)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Download Sitara Live App APK")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Sitara Live APK").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "APK link copied to clipboard! (Paste to share)", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(context: Context, label: String, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard! Opening share...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }
}
