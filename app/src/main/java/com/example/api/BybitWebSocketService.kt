package com.example.api

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class BybitWebsocketTickerMsg(
    val topic: String?,
    val type: String?,
    val ts: Long?,
    val data: BybitWebsocketTickerData?
)

@JsonClass(generateAdapter = true)
data class BybitWebsocketTickerData(
    val symbol: String,
    val lastPrice: String?,
    val highPrice24h: String?,
    val lowPrice24h: String?,
    val prevPrice24h: String?,
    val volume24h: String?,
    val turnover24h: String?
)

object BybitWebSocketService {
    private const val TAG = "BybitWebSocket"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
        
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val msgAdapter = moshi.adapter(BybitWebsocketTickerMsg::class.java)

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _tickerFlow = MutableSharedFlow<BybitWebsocketTickerData>(replay = 1, extraBufferCapacity = 64)
    val tickerFlow: SharedFlow<BybitWebsocketTickerData> = _tickerFlow.asSharedFlow()

    private var isConnecting = false
    private var isStopped = false
    private var reconnectAttempt = 0
    private var useTestnet = true

    // Backoff constants
    private const val BASE_DELAY_MS = 1000L
    private const val MAX_DELAY_MS = 60000L

    fun start(testnet: Boolean) {
        useTestnet = testnet
        isStopped = false
        reconnectAttempt = 0
        connect()
    }

    fun stop() {
        isStopped = true
        webSocket?.close(1000, "Service stopped")
        webSocket = null
        Log.d(TAG, "WebSocket service stopped.")
    }

    @Synchronized
    private fun connect() {
        if (isConnecting || isStopped) return
        isConnecting = true

        val baseUrl = if (useTestnet) {
            "wss://stream-testnet.bybit.com/v5/public/linear"
        } else {
            "wss://stream.bybit.com/v5/public/linear"
        }

        Log.d(TAG, "Connecting to Bybit WebSocket: $baseUrl (Attempt: ${reconnectAttempt + 1})")
        
        val request = Request.Builder().url(baseUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "Bybit WebSocket Connected successfully.")
                isConnecting = false
                reconnectAttempt = 0
                
                // Subscribe to all linear perpetual tickers
                subscribeToTickers(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val msg = msgAdapter.fromJson(text)
                    if (msg?.topic != null && msg.topic.startsWith("tickers.") && msg.data != null) {
                        scope.launch {
                            _tickerFlow.emit(msg.data)
                        }
                    }
                } catch (e: Exception) {
                    // Fail silently for non-ticker frames or parsing anomalies
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "Bybit WebSocket Closed: $code -> $reason")
                isConnecting = false
                triggerReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Bybit WebSocket Failure: ${t.localizedMessage}", t)
                isConnecting = false
                triggerReconnect()
            }
        })
    }

    private fun subscribeToTickers(ws: WebSocket) {
        // Bybit public topics subscribe payload.
        // For general linear perpetual tickers, subscribe to "tickers.BTCUSDT", "tickers.ETHUSDT", "tickers.SOLUSDT", "tickers.XRPUSDT" etc.
        val payload = """
            {
                "op": "subscribe",
                "args": [
                    "tickers.BTCUSDT",
                    "tickers.ETHUSDT",
                    "tickers.SOLUSDT",
                    "tickers.XRPUSDT",
                    "tickers.DOGEUSDT",
                    "tickers.ADAUSDT"
                ]
            }
        """.trimIndent()
        ws.send(payload)
        Log.d(TAG, "Subscribed to linear tickers: BTC, ETH, SOL, XRP, DOGE, ADA.")
    }

    private fun triggerReconnect() {
        if (isStopped) return
        
        reconnectAttempt++
        
        // Exponential backoff: base * 2^attempt
        val exponentialDelay = BASE_DELAY_MS * (1L shl min(reconnectAttempt, 10))
        val rawDelay = min(exponentialDelay, MAX_DELAY_MS)
        
        // Random Jitter (adds +/- 15% random variance to avoid thundering herd)
        val jitterPercent = Random.nextDouble(0.85, 1.15)
        val finalDelay = (rawDelay * jitterPercent).toLong()

        Log.w(TAG, "Reconnecting in ${finalDelay}ms (Exponential Backoff, attempt: $reconnectAttempt)...")
        
        scope.launch {
            delay(finalDelay)
            if (!isStopped) {
                connect()
            }
        }
    }
}
