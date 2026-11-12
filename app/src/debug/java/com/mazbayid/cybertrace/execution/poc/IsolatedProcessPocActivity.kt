package com.mazbayid.cybertrace.execution.poc

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Debug-only check that Android can bind to a service running under an isolated UID.
 * This does not execute Python and does not prove Chaquopy compatibility.
 */
class IsolatedProcessPocActivity : Activity() {
    private lateinit var status: TextView
    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val replyMessenger = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            if (message.what != MSG_PROBE_RESULT) return

            val serviceUid = message.data.getInt(KEY_UID, -1)
            val servicePid = message.data.getInt(KEY_PID, -1)
            val appUid = applicationInfo.uid
            val separated = serviceUid >= 0 && serviceUid != appUid

            status.text = buildString {
                append(if (separated) "PASS" else "FAIL")
                append(" · isolated service UID check\n\n")
                append("App UID: ")
                append(appUid)
                append("\nService UID: ")
                append(serviceUid)
                append("\nService PID: ")
                append(servicePid)
                append("\n\n")
                append(
                    if (separated) {
                        "The service runs under a different Android UID. " +
                            "This verifies the Android isolated-process boundary only; " +
                            "Python runtime startup and resource limits remain untested."
                    } else {
                        "The service did not report a distinct UID. Do not use this " +
                            "process as a Python execution boundary."
                    }
                )
            }
        }
    })

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder == null) {
                status.text = "FAIL · Isolated service returned no Binder."
                return
            }

            serviceMessenger = Messenger(binder)
            val request = Message.obtain(null, MSG_PROBE).apply {
                replyTo = replyMessenger
            }

            try {
                serviceMessenger?.send(request)
                status.text = "Bound · requesting isolated process identity…"
            } catch (error: RemoteException) {
                status.text = "FAIL · Binder request failed: " +
                    (error.message ?: "unknown error")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            status.text = "Service disconnected."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply {
            text = "This disposable test checks whether Android starts a bound service " +
                "under a distinct isolated UID. It does not execute Python."
            textSize = 16f
            setTextColor(0xFFE2EDF5.toInt())
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "TEST ISOLATED PROCESS"
            setOnClickListener { bindIsolatedService() }
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
                setBackgroundColor(0xFF070B12.toInt())
                addView(
                    status,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                )
                addView(
                    button,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        )
    }

    private fun bindIsolatedService() {
        if (isBound) return

        status.text = "Starting isolated service…"
        val intent = Intent(this, IsolatedPythonServiceProbe::class.java)
        isBound = bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!isBound) {
            status.text = "FAIL · Android refused to bind the isolated service."
        }
    }

    override fun onDestroy() {
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        serviceMessenger = null
        super.onDestroy()
    }

    private companion object {
        const val MSG_PROBE = 1
        const val MSG_PROBE_RESULT = 2
        const val KEY_UID = "uid"
        const val KEY_PID = "pid"
    }
}
