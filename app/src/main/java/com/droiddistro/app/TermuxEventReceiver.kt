package com.droiddistro.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TermuxEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_EVENT) return
        val prefs = context.getSharedPreferences("droiddistro", Context.MODE_PRIVATE)
        val suppliedToken = intent.getStringExtra("token") ?: return
        val savedToken = prefs.getString("eventToken", null)
        if (savedToken.isNullOrBlank() || suppliedToken != savedToken) return

        val state = intent.getStringExtra("state")?.take(40) ?: "UNKNOWN"
        val message = intent.getStringExtra("message")?.take(500) ?: "Termux event received"
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val existing = prefs.getString("logText", "") ?: ""
        val updated = (existing + "[$time] [$state] $message\n").takeLast(12000)
        val editor = prefs.edit()
            .putString("installState", state)
            .putString("lastMessage", message)
            .putString("logText", updated)
        val distro = intent.getStringExtra("distro")
        val desktop = intent.getStringExtra("desktop")
        when (state) {
            "READY" -> {
                editor.putBoolean("installed", true)
                if (!distro.isNullOrBlank()) editor.putString("installedDistro", distro)
                if (!desktop.isNullOrBlank()) editor.putString("installedDesktop", desktop)
            }
            "NOT_INSTALLED" -> editor.putBoolean("installed", false)
            "ERROR" -> editor.putBoolean("installed", false)
            "TASK_ERROR", "TASK_DONE", "TASK_RUNNING" -> Unit
        }
        editor.apply()
    }

    companion object {
        const val ACTION_EVENT = "com.droiddistro.app.TERMUX_EVENT"
    }
}
