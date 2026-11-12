package com.mazbayid.cybertrace.execution.poc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

private class SecureRuntimeAssetsHandler(context: Context) :
    WebViewAssetLoader.PathHandler {

    private val assetsHandler = WebViewAssetLoader.AssetsPathHandler(context)

    override fun handle(path: String): WebResourceResponse? {
        val response = assetsHandler.handle(path) ?: return null
        val isWorkerScript = path.endsWith("/worker.js") ||
            path.endsWith("/python-worker.js")
        val policy = if (isWorkerScript) WORKER_CONTENT_SECURITY_POLICY
            else DOCUMENT_CONTENT_SECURITY_POLICY
        val statusCode = response.statusCode.takeIf {
            it in 100..299 || it in 400..599
        } ?: if (response.data == null) 404 else 200
        val reasonPhrase = response.reasonPhrase?.takeIf(String::isNotBlank)
            ?: if (statusCode == 404) "Not Found" else "OK"
        val mimeType = if (path.endsWith(".wasm")) {
            "application/wasm"
        } else {
            response.mimeType ?: "application/octet-stream"
        }
        val headers = (response.responseHeaders ?: emptyMap()).toMutableMap().apply {
            put("Content-Security-Policy", policy)
            put("X-Content-Type-Options", "nosniff")
            put("Cache-Control", "no-store")
            put("Referrer-Policy", "no-referrer")
            put("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
        }

        return WebResourceResponse(
            mimeType,
            response.encoding,
            statusCode,
            reasonPhrase,
            headers,
            response.data
        )
    }

    private companion object {
        const val DOCUMENT_CONTENT_SECURITY_POLICY =
            "default-src 'self'; " +
                "script-src 'self' 'wasm-unsafe-eval'; " +
                "worker-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "connect-src 'self'; " +
                "base-uri 'none'; object-src 'none'; frame-src 'none'; " +
                "form-action 'none'; img-src 'self' data:; " +
                "frame-ancestors 'none'"

        const val WORKER_CONTENT_SECURITY_POLICY =
            "default-src 'self'; " +
                "script-src 'self' 'wasm-unsafe-eval'; " +
                "worker-src 'none'; " +
                "connect-src 'self'; " +
                "object-src 'none'; base-uri 'none'; " +
                "form-action 'none';"
    }
}

class RuntimePocActivity : Activity() {
    private var webView: WebView? = null

    internal var runtimeWebViewForTests: WebView? = null
        private set

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROCESS) ||
            !WebViewCompat.isMultiProcessEnabled()
        ) {
            setContentView(TextView(this).apply {
                text = "RUNTIME BLOCKED\n\nThis device is not running WebView in a separate sandboxed renderer process. Python execution is disabled."
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(7, 11, 18))
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
            })
            return
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                SecureRuntimeAssetsHandler(this)
            )
            .build()

        val runtimeWebView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.allowFileAccessFromFileURLs = false
            settings.allowUniversalAccessFromFileURLs = false
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.domStorageEnabled = false
            settings.databaseEnabled = false
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ) = assetLoader.shouldInterceptRequest(request.url)

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    return request.url.scheme != "https" ||
                        request.url.host != "appassets.androidplatform.net"
                }

                override fun onRenderProcessGone(
                    view: WebView,
                    detail: android.webkit.RenderProcessGoneDetail
                ): Boolean {
                    finish()
                    return true
                }
            }
        }

        webView = runtimeWebView
        runtimeWebViewForTests = runtimeWebView
        setContentView(runtimeWebView)
        runtimeWebView.loadUrl(
            "https://appassets.androidplatform.net/assets/runtime-poc/index.html"
        )
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            destroy()
        }
        webView = null
        runtimeWebViewForTests = null
        super.onDestroy()
    }
}
