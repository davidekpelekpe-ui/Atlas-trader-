package com.example.api

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.TradeJournal

@JsonClass(generateAdapter = true)
data class BybitTickerResponse(
    val retCode: Int,
    val retMsg: String,
    val result: BybitTickerResult?
)

@JsonClass(generateAdapter = true)
data class BybitTickerResult(
    val category: String?,
    val list: List<BybitTicker>?
)

@JsonClass(generateAdapter = true)
data class BybitTicker(
    val symbol: String,
    val lastPrice: String,
    val highPrice24h: String?,
    val lowPrice24h: String?,
    val prevPrice24h: String?,
    val price24hPcnt: String?
)

@JsonClass(generateAdapter = true)
data class BybitPositionResponse(
    val retCode: Int,
    val retMsg: String,
    val result: BybitPositionResult?
)

@JsonClass(generateAdapter = true)
data class BybitPositionResult(
    val category: String?,
    val list: List<BybitPosition>?
)

@JsonClass(generateAdapter = true)
data class BybitPosition(
    val symbol: String,
    val size: String,
    val side: String, // "Buy" or "Sell"
    val entryPrice: String,
    val markPrice: String,
    val unrealisedPnl: String,
    val liqPrice: String?,
    val leverage: String?
)

@JsonClass(generateAdapter = true)
data class BybitWalletResponse(
    val retCode: Int,
    val retMsg: String,
    val result: BybitWalletResult?
)

@JsonClass(generateAdapter = true)
data class BybitWalletResult(
    val list: List<BybitWalletAccount>?
)

@JsonClass(generateAdapter = true)
data class BybitWalletAccount(
    val accountType: String?,
    val totalEquity: String?,
    val totalWalletBalance: String?,
    val totalMarginBalance: String?,
    val totalAvailableBalance: String?,
    val totalPerpUPL: String?
)

object BybitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val tickerAdapter = moshi.adapter(BybitTickerResponse::class.java)
    private val positionAdapter = moshi.adapter(BybitPositionResponse::class.java)
    private val walletAdapter = moshi.adapter(BybitWalletResponse::class.java)
    private val closedPnlAdapter = moshi.adapter(BybitClosedPnlResponse::class.java)

    private fun getBaseUrl(useTestnet: Boolean): String {
        return if (useTestnet) "https://api-testnet.bybit.com" else "https://api.bybit.com"
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun generateHmacSha256(data: String, secret: String): String {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        sha256HMAC.init(secretKey)
        return bytesToHex(sha256HMAC.doFinal(data.toByteArray(Charsets.UTF_8)))
    }

    /**
     * Fetches public market ticker for a given symbol.
     * Async on Dispatchers.IO.
     */
    suspend fun fetchTicker(symbol: String, useTestnet: Boolean = true): BybitTicker? = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(useTestnet)}/v5/market/tickers?category=linear&symbol=$symbol"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: return@withContext null
                val res = tickerAdapter.fromJson(bodyString)
                if (res?.retCode == 0) {
                    res.result?.list?.firstOrNull()
                } else {
                    Log.w("BybitClient", "Bybit response error: ${res?.retMsg}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("BybitClient", "Failed to fetch ticker for $symbol", e)
            null
        }
    }

    /**
     * Fetches public market tickers for all linear perpetual contracts.
     * Async on Dispatchers.IO.
     */
    suspend fun fetchAllTickers(useTestnet: Boolean = true): List<BybitTicker> = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(useTestnet)}/v5/market/tickers?category=linear"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyString = response.body?.string() ?: return@withContext emptyList()
                val res = tickerAdapter.fromJson(bodyString)
                if (res?.retCode == 0) {
                    res.result?.list ?: emptyList()
                } else {
                    Log.w("BybitClient", "Bybit response error: ${res?.retMsg}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BybitClient", "Failed to fetch all linear tickers", e)
            emptyList()
        }
    }

    /**
     * Fetches private open positions for the linear perpetual category.
     * Async on Dispatchers.IO.
     */
    suspend fun fetchOpenPositions(
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean = true
    ): List<BybitPosition> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            return@withContext emptyList()
        }
        try {
            val timestamp = System.currentTimeMillis().toString()
            val recvWindow = "5000"
            val queryParams = "category=linear"
            
            val message = timestamp + apiKey + recvWindow + queryParams
            val signature = generateHmacSha256(message, apiSecret)

            val url = "${getBaseUrl(useTestnet)}/v5/position/list?$queryParams"
            val request = Request.Builder()
                .url(url)
                .addHeader("X-BAPI-API-KEY", apiKey)
                .addHeader("X-BAPI-TIMESTAMP", timestamp)
                .addHeader("X-BAPI-SIGN", signature)
                .addHeader("X-BAPI-RECV-WINDOW", recvWindow)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: return@withContext emptyList()
                if (!response.isSuccessful) {
                    Log.e("BybitClient", "Bybit positions request failed: ${response.code} -> $bodyString")
                    return@withContext emptyList()
                }
                val res = positionAdapter.fromJson(bodyString)
                if (res?.retCode == 0) {
                    res.result?.list?.filter { 
                        try {
                            it.size.toDouble() > 0.0
                        } catch (e: Exception) {
                            false
                        }
                    } ?: emptyList()
                } else {
                    Log.e("BybitClient", "Bybit positions response error: ${res?.retMsg}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BybitClient", "Failed to fetch open positions from Bybit", e)
            emptyList()
        }
    }

    /**
     * Fetches private account wallet balance.
     * Async on Dispatchers.IO.
     * Robust: automatically falls back to CONTRACT accounts if UNIFIED fails.
     */
    suspend fun fetchWalletBalance(
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean = true
    ): BybitWalletAccount? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            return@withContext null
        }
        
        // Try UNIFIED first, and if that fails, fall back to CONTRACT (Classical Accounts)
        val accountTypes = listOf("UNIFIED", "CONTRACT")
        for (accountType in accountTypes) {
            try {
                val timestamp = System.currentTimeMillis().toString()
                val recvWindow = "5000"
                val queryParams = "accountType=$accountType"
                
                val message = timestamp + apiKey + recvWindow + queryParams
                val signature = generateHmacSha256(message, apiSecret)

                val url = "${getBaseUrl(useTestnet)}/v5/account/wallet-balance?$queryParams"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("X-BAPI-API-KEY", apiKey)
                    .addHeader("X-BAPI-TIMESTAMP", timestamp)
                    .addHeader("X-BAPI-SIGN", signature)
                    .addHeader("X-BAPI-RECV-WINDOW", recvWindow)
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string() ?: return@withContext null
                    if (!response.isSuccessful) {
                        Log.e("BybitClient", "Bybit wallet request ($accountType) failed: ${response.code} -> $bodyString")
                    } else {
                        val res = walletAdapter.fromJson(bodyString)
                        if (res?.retCode == 0) {
                            val account = res.result?.list?.firstOrNull()
                            if (account != null) {
                                Log.d("BybitClient", "Successfully fetched wallet balance using $accountType")
                                // If the account object returned doesn't explicitly have accountType set, populate it
                                return@withContext if (account.accountType.isNullOrBlank()) {
                                    account.copy(accountType = accountType)
                                } else {
                                    account
                                }
                            }
                        } else {
                            Log.e("BybitClient", "Bybit wallet response ($accountType) error code ${res?.retCode}: ${res?.retMsg}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BybitClient", "Failed to fetch wallet balance for $accountType", e)
            }
        }
        null
    }

    /**
     * Fetches closed P&L from Bybit (recent trades).
     * Async on Dispatchers.IO.
     */
    suspend fun fetchClosedPnL(
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean = true
    ): List<TradeJournal> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            return@withContext emptyList()
        }
        try {
            val timestamp = System.currentTimeMillis().toString()
            val recvWindow = "5000"
            val queryParams = "category=linear&limit=15"
            
            val message = timestamp + apiKey + recvWindow + queryParams
            val signature = generateHmacSha256(message, apiSecret)

            val url = "${getBaseUrl(useTestnet)}/v5/position/closed-pnl?$queryParams"
            val request = Request.Builder()
                .url(url)
                .addHeader("X-BAPI-API-KEY", apiKey)
                .addHeader("X-BAPI-TIMESTAMP", timestamp)
                .addHeader("X-BAPI-SIGN", signature)
                .addHeader("X-BAPI-RECV-WINDOW", recvWindow)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: return@withContext emptyList()
                if (!response.isSuccessful) {
                    Log.e("BybitClient", "Bybit closed P&L request failed: ${response.code} -> $bodyString")
                    return@withContext emptyList()
                }
                val res = closedPnlAdapter.fromJson(bodyString)
                if (res?.retCode == 0) {
                    res.result?.list?.map { item ->
                        val entry = item.avgEntryPrice.toDoubleOrNull() ?: 0.0
                        val exit = item.avgExitPrice.toDoubleOrNull() ?: 0.0
                        val size = item.closedSize.toDoubleOrNull() ?: 0.0
                        val pnlVal = item.closedPnl.toDoubleOrNull() ?: 0.0
                        TradeJournal(
                            coin = item.symbol,
                            direction = if (pnlVal >= 0.0) "LONG" else "SHORT", // trade setup direction representation
                            setupType = "Bybit Closed P&L",
                            entryPrice = entry,
                            exitPrice = exit,
                            profitLoss = pnlVal,
                            notes = "Bybit historical closed P&L trade. Closed size: $size. Leverage: ${item.leverage}x.",
                            timestamp = item.updatedTime.toLongOrNull() ?: System.currentTimeMillis()
                        )
                    } ?: emptyList()
                } else {
                    Log.e("BybitClient", "Bybit closed P&L response error: ${res?.retMsg}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BybitClient", "Failed to fetch closed P&L", e)
            emptyList()
        }
    }

    /**
     * Validates API credentials before saving them.
     */
    suspend fun validateCredentials(
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean = true
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiSecret.isBlank()) {
            return@withContext Pair(false, "API Key or Secret cannot be blank")
        }
        try {
            val balance = fetchWalletBalance(apiKey, apiSecret, useTestnet)
            if (balance != null) {
                val balStr = balance.totalAvailableBalance ?: balance.totalWalletBalance ?: "0"
                val type = balance.accountType ?: "Connected"
                Pair(true, "Successfully connected! $type Wallet Balance: $balStr USDT.")
            } else {
                Pair(false, "Connection failed. Please check your credentials and testnet setting.")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}

@JsonClass(generateAdapter = true)
data class BybitClosedPnlResponse(
    val retCode: Int,
    val retMsg: String,
    val result: BybitClosedPnlResult?
)

@JsonClass(generateAdapter = true)
data class BybitClosedPnlResult(
    val category: String?,
    val list: List<BybitClosedPnl>?
)

@JsonClass(generateAdapter = true)
data class BybitClosedPnl(
    val symbol: String,
    val orderId: String,
    val closedSize: String,
    val leverage: String,
    val avgEntryPrice: String,
    val avgExitPrice: String,
    val closedPnl: String,
    val updatedTime: String
)
