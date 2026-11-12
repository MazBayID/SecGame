package com.mazbayid.cybertrace.execution.poc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.IBinder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class IsolatedProcessPocInstrumentedTest {

    @Test
    fun isolatedServiceHasSeparateUidAndCanReadPackagedAsset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val response = AtomicReference<Bundle?>()
        val latch = CountDownLatch(1)
        val replyMessenger = Messenger(
            Handler(Looper.getMainLooper()) { message ->
                if (message.what == MSG_PROBE_RESULT) {
                    response.set(message.data)
                    latch.countDown()
                }
                true
            }
        )
        var serviceMessenger: Messenger? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder == null) {
                    latch.countDown()
                    return
                }

                serviceMessenger = Messenger(binder)
                try {
                    serviceMessenger?.send(
                        Message.obtain(null, MSG_PROBE).apply {
                            replyTo = replyMessenger
                        }
                    )
                } catch (_: Exception) {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceMessenger = null
            }
        }

        val bound = context.bindService(
            Intent(context, IsolatedPythonServiceProbe::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        assertTrue("Android did not bind the isolated service", bound)

        try {
            assertTrue(
                "Isolated service did not answer the identity probe",
                latch.await(10, TimeUnit.SECONDS)
            )
            val result = response.get()
            assertTrue("Isolated service returned no result", result != null)

            val serviceUid = result!!.getInt(KEY_UID, -1)
            assertNotEquals(
                "Service must run under an isolated UID",
                context.applicationInfo.uid,
                serviceUid
            )
            assertTrue(
                "Service could not read the APK-packaged probe asset",
                result.getBoolean(KEY_ASSET_READABLE, false)
            )
        } finally {
            context.unbindService(connection)
        }
    }

    private companion object {
        const val MSG_PROBE = 1
        const val MSG_PROBE_RESULT = 2
        const val KEY_UID = "uid"
        const val KEY_ASSET_READABLE = "asset_readable"
    }
}
