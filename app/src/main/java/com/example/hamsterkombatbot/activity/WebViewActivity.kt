package com.example.hamsterkombatbot.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.helper.PreferencesHelper
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val TAG = "WebViewActivity"
        const val EXTRA_URL = "url"
        const val AUTH_PAYLOAD = "auth_payload"
    }

    private data class HttpRequest(
        val method: String,
        val path: String,
        val headers: Map<String, String>,
        val body: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // get url from intent
        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val fragmentData = url.substringAfter("#")
        Log.d(TAG, "fragmentData: $fragmentData")

        val webView = findViewById<WebView>(R.id.web_view)
        startTcpServer(
            onServerReady = {
                webView.apply {
                    this.layoutParams =
                        android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                        )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.setSupportMultipleWindows(true)
                    settings.textSize = WebSettings.TextSize.NORMAL

                    isVerticalScrollBarEnabled = true

                    this.webChromeClient =
                        object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                if (newProgress == 100) {
                                    view?.evaluateJavascript("getAuthPayLoad('$it');", null)
                                }
                            }
                        }

                    this.webViewClient =
                        object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.evaluateJavascript("getAuthPayLoad('$it');", null)
                            }
                        }
                }

                webView.loadUrl("file:///android_asset/sample.html#$fragmentData")
            },
            onDataReceived = {
                if (it.path == "/error") {
                    val json = JSONObject(it.body ?: "{}")
                    if (json.has("message")) {
                        Toast.makeText(
                                this,
                                getString(R.string.webview_failed, json.getString("message")),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                        return@startTcpServer
                    }
                } else if (it.path == "/success") {
                    // save headers
                    val currentAccount = PreferencesHelper.getCurrentAccount()
                    val preferencesHelper = PreferencesHelper(this, currentAccount)
                    preferencesHelper.saveHeaders(it.headers)

                    val json = JSONObject(it.body ?: "{}")
                    if (json.has("payload")) {
                        onDataLoaded(json.getString("payload"))
                        return@startTcpServer
                    }
                }

                Toast.makeText(
                        this,
                        getString(
                            R.string.webview_failed,
                            getString(R.string.webview_failed_unknown_response)
                        ),
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        )
    }

    private fun onDataLoaded(fingerprintData: String?) {
        val resultIntent = Intent()
        resultIntent.putExtra(AUTH_PAYLOAD, fingerprintData)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // create a tcp server in IO dispatcher using coroutines
    private fun startTcpServer(
        onServerReady: (url: String) -> Unit,
        onDataReceived: (httpRequest: HttpRequest) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket = ServerSocket(0)
            val port = serverSocket.localPort
            lifecycleScope.launch { onServerReady("http://127.0.0.1:$port") }

            while (true) {
                val socket = serverSocket.accept()
                Log.d(TAG, "new connection: ${socket.port}")

                // make new thread to handle the request
                handleClient(
                    socket,
                    onDataReceived = {
                        onDataReceived(it)

                        // send response
                        CoroutineScope(Dispatchers.IO).launch {
                            socket.outputStream.write("HTTP/1.1 200 OK\r\n\r\n".toByteArray())
                            socket.close()
                            Log.d(TAG, "connection closed: ${socket.port}")
                        }
                    }
                )
            }
        }
    }

    private suspend fun readHttpRequest(inputStream: InputStream): String {
        val reader = inputStream.bufferedReader()
        val requestBuilder = StringBuilder()
        var line: String?

        // Read request line and headers
        while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
            requestBuilder.appendLine(line)
        }

        // Extract headers from the request
        val headers = requestBuilder.lines().drop(1).takeWhile { it.isNotEmpty() }
        val contentLengthHeader =
            headers.find { it.startsWith("Content-Length", ignoreCase = true) }
        val contentLength = contentLengthHeader?.split(":")?.get(1)?.trim()?.toIntOrNull()

        // Ensure a blank line between headers and body
        requestBuilder.appendLine()

        // Read the body if Content-Length header is present
        if (contentLength != null && contentLength > 0) {
            val body = CharArray(contentLength)
            reader.read(body, 0, contentLength)
            requestBuilder.append(body)
        }

        return requestBuilder.toString()
    }

    private fun parseHttpRequest(request: String): HttpRequest {
        val lines = request.lines()
        if (lines.isEmpty()) {
            throw IllegalArgumentException("Empty request")
        }

        val requestLine = lines[0].split(" ")
        val method = requestLine[0]
        val path = requestLine[1]

        val headers = mutableMapOf<String, String>()
        var bodyIndex = lines.indexOfFirst { it.isEmpty() }
        if (bodyIndex == -1) {
            bodyIndex = lines.size
        }

        for (i in 1 until bodyIndex) {
            val headerLine = lines[i]
            val colonIndex = headerLine.indexOf(":")
            if (colonIndex != -1) {
                val headerName = headerLine.substring(0, colonIndex).trim()
                val headerValue = headerLine.substring(colonIndex + 1).trim()
                headers[headerName] = headerValue
            }
        }

        val body =
            if (bodyIndex + 1 < lines.size) {
                lines.subList(bodyIndex + 1, lines.size).joinToString("\n").trim()
            } else {
                null
            }

        return HttpRequest(method, path, headers, body)
    }

    private fun handleClient(socket: Socket, onDataReceived: (HttpRequest) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = readHttpRequest(socket.inputStream)
            val httpRequest = parseHttpRequest(request)

            Log.d(TAG, "request: $httpRequest")

            if (httpRequest.method.equals("OPTIONS", ignoreCase = true)) {
                respondOptions(socket.outputStream)
            } else if (httpRequest.method.equals("POST", ignoreCase = true)) {
                withContext(Dispatchers.Main) { onDataReceived(httpRequest) }
            }
        }
    }

    private fun respondOptions(outputStream: OutputStream) {
        val responseHeaders =
            listOf(
                    "HTTP/1.1 204 No Content",
                    "Allow: OPTIONS, GET, POST",
                    "Access-Control-Allow-Origin: *",
                    "Access-Control-Allow-Methods: OPTIONS, GET, POST",
                    "Access-Control-Allow-Headers: Content-Type",
                    "Content-Length: 0"
                )
                .joinToString(separator = "\r\n")

        outputStream.apply {
            write(responseHeaders.toByteArray())
            write("\r\n\r\n\r\n\r\n".toByteArray())
            flush()
        }
    }
}
