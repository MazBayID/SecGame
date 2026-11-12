package com.mazbayid.cybertrace.execution.poc

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process

/**
 * Disposable Binder endpoint declared with android:isolatedProcess="true".
 * It only reports process identity; it never accepts or executes player source.
 */
class IsolatedPythonServiceProbe : Service() {
    private val messenger = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            if (message.what != MSG_PROBE) return

            val packagedAssetReadable = try {
                assets.open(ASSET_NAME).bufferedReader().use {
                    it.readText().trim() == EXPECTED_ASSET_CONTENT
                }
            } catch (_: Exception) {
                false
            }

            val reply = Message.obtain(null, MSG_PROBE_RESULT).apply {
                data = Bundle().apply {
                    putInt(KEY_UID, Process.myUid())
                    putInt(KEY_PID, Process.myPid())
                    putBoolean(KEY_ASSET_READABLE, packagedAssetReadable)
                }
            }

            try {
                message.replyTo?.send(reply)
            } catch (_: Exception) {
                // The debug activity may have closed before receiving the response.
            }
        }
    })

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    private companion object {
        const val MSG_PROBE = 1
        const val MSG_PROBE_RESULT = 2
        const val KEY_UID = "uid"
        const val KEY_PID = "pid"
        const val KEY_ASSET_READABLE = "asset_readable"
        const val ASSET_NAME = "isolated-process-probe.txt"
        const val EXPECTED_ASSET_CONTENT = "CYBER_TRACE_ISOLATED_ASSET_V1"
    }
}
