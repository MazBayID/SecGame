package com.mazbayid.cybertrace.execution.poc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader

private class SecureRuntimeAssetsHandler(context: Context) :
    WebViewAssetLoader.PathHandler {

    private val assetsHandler = WebViewAssetLoader.AssetsPathHandler(context)

    override fun handle(path: String): WebResourceResponse? {
        val response = assetsHandler.handle(path) ?: return null
        val mimeType = if (path.endsWith(".wasm")) {
            "application/wasm"
        } else {
            response.mimeType
        }
        val headers = (response.responseHeaders ?: emptyMap()).toMutableMap().apply {
            put("Content-Security-Policy", CONTENT_SECURITY_POLICY)
            put("X-Content-Type-Options", "nosniff")
            put("Cache-Control", "no-store")
        }

        return WebResourceResponse(
            mimeType,
            response.encoding,
            response.statusCode,
            response.reasonPhrase,
            headers,
            response.data
        )
    }

    private companion object {
        const val CONTENT_SECURITY_POLICY =
            "default-src 'self'; " +
                "script-src 'self' 'wasm-unsafe-eval'; " +
                "worker-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "connect-src 'self'; " +
                "base-uri 'none'; object-src 'none'; frame-src 'none'; " +
                "form-action 'none'; img-src 'self' data:"
    }
}

class RuntimePocActivity : Activity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                SecureRuntimeAssetsHandler(this)
            )
            .build()

        webView = WebView(this).apply {
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

        setContentView(webView)
        webView.loadUrl(
            "https://appassets.androidplatform.net/assets/runtime-poc/index.html"
        )
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
