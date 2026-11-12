package com.mazbayid.cybertrace.execution.poc

import android.os.SystemClock
import android.util.Base64
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONTokener
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class RuntimePocInstrumentedTest {

    @Test
    fun realPythonRunsAndWorkerCanBeStoppedAndRecreated() {
        val scenario = ActivityScenario.launch(RuntimePocActivity::class.java)

        try {
            val startup = awaitText(scenario, "status", 60_000) {
                it.contains("Python 3.14.") && it.contains("Pyodide 314.0.7")
            }
            assertTrue(
                "Expected pinned Pyodide and Python versions, got: $startup",
                startup.contains("Pyodide 314.0.7")
            )

            val basic = runPythonAndAwait(
                scenario,
                """print("ANDROID_WEBVIEW_RUNTIME_OK")""",
                "ANDROID_WEBVIEW_RUNTIME_OK"
            )
            assertTrue(basic.contains("COMPLETED"))

            val syntax = runPythonAndAwait(
                scenario,
                """if True print("broken")""",
                "SYNTAX_ERROR"
            )
            assertTrue(syntax.contains("SyntaxError"))

            val runtime = runPythonAndAwait(
                scenario,
                """print(1 / 0)""",
                "RUNTIME_ERROR"
            )
            assertTrue(runtime.contains("ZeroDivisionError"))

            val outputLimit = runPythonAndAwait(
                scenario,
                """print("X" * 20000)""",
                "OUTPUT_LIMIT"
            )
            assertTrue(outputLimit.contains("Output truncated at 16 KiB"))

            val missingPackage = runPythonAndAwait(
                scenario,
                """import numpy""",
                "RUNTIME_ERROR"
            )
            assertTrue(missingPackage.contains("ModuleNotFoundError"))

            val timeout = runPythonAndAwait(
                scenario,
                """while True: pass""",
                "TIMEOUT"
            )
            assertTrue(timeout.contains("terminated the worker"))

            startPython(scenario, """while True: pass""")
            awaitText(scenario, "output", 5_000) {
                it.contains("Starting a fresh Python worker")
            }
            SystemClock.sleep(500)
            evaluate(scenario, """document.querySelector("#cancel-python").click(); "cancel-clicked" """)
            val cancelled = awaitText(scenario, "output", 10_000) {
                it.contains("CANCELLED")
            }
            assertTrue(cancelled.contains("CANCELLED"))

            val recovered = runPythonAndAwait(
                scenario,
                """print("FRESH_WORKER_OK")""",
                "FRESH_WORKER_OK"
            )
            assertTrue(recovered.contains("COMPLETED"))

            evaluate(scenario, """document.querySelector("#ping").click(); "ping-clicked" """)
            val pong = awaitText(scenario, "output", 10_000) {
                it.contains("PASS") && it.contains("responded to ping")
            }
            assertTrue(pong.contains("Generation"))

            evaluate(scenario, """document.querySelector("#hang").click(); "hang-clicked" """)
            awaitText(scenario, "output", 5_000) {
                it.contains("non-stop task")
            }
            evaluate(scenario, """document.querySelector("#stop").click(); "stop-clicked" """)
            val stopped = awaitText(scenario, "output", 5_000) {
                it.contains("page remains interactive")
            }
            assertTrue(stopped.contains("Termination requested"))

            evaluate(scenario, """document.querySelector("#restart").click(); "restart-clicked" """)
            val restarted = awaitText(scenario, "status", 10_000) {
                it.contains("Worker ready")
            }
            assertTrue(restarted.contains("Worker ready"))
        } finally {
            scenario.close()
        }
    }

    private fun runPythonAndAwait(
        scenario: ActivityScenario<RuntimePocActivity>,
        source: String,
        expected: String
    ): String {
        startPython(scenario, source)
        awaitText(scenario, "output", 5_000) {
            it.contains("Starting a fresh Python worker")
        }
        return awaitText(scenario, "output", 45_000) {
            it.contains(expected)
        }
    }

    private fun startPython(
        scenario: ActivityScenario<RuntimePocActivity>,
        source: String
    ) {
        val encoded = Base64.encodeToString(
            source.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )
        evaluate(
            scenario,
            """
            (() => {
              const editor = document.querySelector("#python-source");
              editor.value = atob("$encoded");
              document.querySelector("#run-python").click();
              return "run-started";
            })()
            """.trimIndent()
        )
    }

    private fun awaitText(
        scenario: ActivityScenario<RuntimePocActivity>,
        elementId: String,
        timeoutMillis: Long,
        predicate: (String) -> Boolean
    ): String {
        val deadline = SystemClock.elapsedRealtime() + timeoutMillis
        var latest = ""

        while (SystemClock.elapsedRealtime() < deadline) {
            latest = evaluate(
                scenario,
                """document.querySelector("#$elementId")?.textContent || """""
            )
            if (predicate(latest)) return latest
            SystemClock.sleep(200)
        }

        throw AssertionError(
            "Timed out waiting for #$elementId. Last text: $latest"
        )
    }

    private fun evaluate(
        scenario: ActivityScenario<RuntimePocActivity>,
        script: String
    ): String {
        val result = AtomicReference<String?>()
        val latch = CountDownLatch(1)

        scenario.onActivity { activity ->
            val webView = activity.runtimeWebViewForTests
            if (webView == null) {
                result.set(null)
                latch.countDown()
            } else {
                webView.evaluateJavascript(script) { value ->
                    result.set(value)
                    latch.countDown()
                }
            }
        }

        assertTrue(
            "WebView JavaScript evaluation did not return",
            latch.await(10, TimeUnit.SECONDS)
        )

        val raw = result.get() ?: return ""
        return try {
            JSONTokener(raw).nextValue() as? String ?: raw
        } catch (_: Exception) {
            raw
        }
    }
}
