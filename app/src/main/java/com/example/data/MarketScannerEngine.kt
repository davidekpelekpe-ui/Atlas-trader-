package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID
import kotlin.random.Random

// Candle data representation for Strategy Replay
data class SimulatedCandle(
    val id: String,
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val isFvg: Boolean = false,
    val isBos: Boolean = false,
    val label: String? = null
)

class MarketScannerEngine(
    private val context: Context,
    private val repository: AppRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var scanJob: Job? = null

    // For in-app push notifications simulation
    private val _notifications = MutableSharedFlow<MarketSetup>(extraBufferCapacity = 10)
    val notifications: SharedFlow<MarketSetup> = _notifications

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val candleListAdapter = moshi.adapter<List<SimulatedCandle>>(
        Types.newParameterizedType(List::class.java, SimulatedCandle::class.java)
    )

    // In-memory state of tracked coins during scanning
    private val coinStates = mutableMapOf<String, CoinState>()

    data class CoinState(
        val coin: String,
        var basePrice: Double,
        var currentPrice: Double,
        var pdh: Double, // Yesterday's High
        var pdl: Double, // Yesterday's Low
        var stage: String, // "Scanning", "PDHL Sweep", "Three-Candle Confirmation", "5m confirmation (FVG)", "BOS confirmation", "Ready"
        var direction: String = "LONG",
        var setupType: String = "Liquidity Sweep",
        var stepCount: Int = 0,
        var candlesList: MutableList<SimulatedCandle> = mutableListOf()
    )

    init {
        // Initialize coin base values
        val initialCoins = listOf(
            CoinState("DOGEUSDT", 0.1240, 0.1240, 0.1290, 0.1190, "Scanning"),
            CoinState("BTCUSDT", 67500.0, 67500.0, 68900.0, 66100.0, "Scanning"),
            CoinState("ETHUSDT", 3450.0, 3450.0, 3550.0, 3350.0, "Scanning"),
            CoinState("SOLUSDT", 142.50, 142.50, 146.20, 138.80, "Scanning"),
            CoinState("XRPUSDT", 0.5820, 0.5820, 0.6050, 0.5610, "Scanning")
        )
        initialCoins.forEach { coinStates[it.coin] = it }
    }

    fun startScanning() {
        if (scanJob != null) return
        
        scanJob = scope.launch {
            Log.d("MarketScannerEngine", "Market Scanner started automatically.")

            // Initialize default settings if missing
            if (repository.getSettingsDirect() == null) {
                repository.saveSettings(AppSettings())
            }
            
            val settings = repository.getSettingsDirect() ?: AppSettings()
            
            // Start Bybit WebSocket stream
            com.example.api.BybitWebSocketService.start(settings.bybitUseTestnet)

            // Collect WebSocket price feeds in real-time
            launch {
                com.example.api.BybitWebSocketService.tickerFlow.collect { tickerUpdate ->
                    val state = coinStates[tickerUpdate.symbol]
                    if (state != null) {
                        val lastPriceVal = tickerUpdate.lastPrice?.toDoubleOrNull()
                        if (lastPriceVal != null && lastPriceVal > 0.0) {
                            state.currentPrice = lastPriceVal
                            // If yesterday's range was uninitialized, set relative boundaries
                            if (state.pdh <= 0.0) state.pdh = lastPriceVal * 1.015
                            if (state.pdl <= 0.0) state.pdl = lastPriceVal * 0.985
                        }
                    }
                }
            }

            while (isActive) {
                try {
                    // Update prices & run engines every 3 seconds for quick, highly engaging simulation
                    tickScanner()
                } catch (e: Exception) {
                    Log.e("MarketScannerEngine", "Error in tickScanner", e)
                }
                delay(3000)
            }
        }
    }

    fun stopScanning() {
        com.example.api.BybitWebSocketService.stop()
        scanJob?.cancel()
        scanJob = null
    }

    private suspend fun tickScanner() {
        val settings = repository.getSettingsDirect() ?: AppSettings()
        
        // --- DYNAMIC BYBIT SYMBOLS DISCOVERY ---
        try {
            val tickers = com.example.api.BybitClient.fetchAllTickers(settings.bybitUseTestnet)
            if (tickers.isNotEmpty()) {
                val excludedList = settings.excludedCoins.split(",").map { it.trim().uppercase() }
                tickers.filter { ticker ->
                    ticker.symbol.endsWith("USDT") && 
                    !excludedList.contains(ticker.symbol.replace("USDT", ""))
                }.forEach { ticker ->
                    val symbol = ticker.symbol
                    val price = ticker.lastPrice.toDoubleOrNull() ?: return@forEach
                    val high = ticker.highPrice24h?.toDoubleOrNull() ?: (price * 1.02)
                    val low = ticker.lowPrice24h?.toDoubleOrNull() ?: (price * 0.98)

                    val existing = coinStates[symbol]
                    if (existing != null) {
                        existing.currentPrice = price
                        if (existing.stage == "Scanning") {
                            existing.pdh = high
                            existing.pdl = low
                        }
                    } else {
                        // Limit to at most 100 active tracked coins to optimize memory and performance
                        if (coinStates.size < 100) {
                            coinStates[symbol] = CoinState(
                                coin = symbol,
                                basePrice = price,
                                currentPrice = price,
                                pdh = high,
                                pdl = low,
                                stage = "Scanning"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MarketScannerEngine", "Dynamic Bybit tickers discovery failed, falling back to local list", e)
        }

        for ((coin, state) in coinStates) {
            // 1. Simulate mild market price volatility (0.1% - 0.5%)
            val volatilityFactor = when (settings.scannerSensitivity) {
                "High" -> 0.008
                "Low" -> 0.002
                else -> 0.005
            }
            val changePct = (Random.nextDouble() - 0.5) * volatilityFactor
            state.currentPrice += state.currentPrice * changePct

            // 2. Advance the state machine for each coin
            when (state.stage) {
                "Scanning" -> {
                    // Check if we sweep PDH or PDL
                    val p = state.currentPrice
                    if (p >= state.pdh) {
                        // Sweep High -> Potential SHORT setup as per general trading,
                        // but user request specified:
                        // "If Price Sweeps PDH -> Potential Long Setup... If Price Sweeps PDL -> Potential Short Setup"
                        // We strictly respect the literal user specification!
                        state.direction = "LONG"
                        state.setupType = if (Random.nextBoolean()) "Liquidity Sweep" else "Real Break"
                        state.stage = "PDHL Sweep"
                        state.stepCount = 1
                        initSimulatedCandles(state)
                        Log.d("MarketScannerEngine", "$coin swept PDH at $p. Mark Potential LONG Setup.")
                    } else if (p <= state.pdl) {
                        state.direction = "SHORT"
                        state.setupType = if (Random.nextBoolean()) "Liquidity Sweep" else "Real Break"
                        state.stage = "PDHL Sweep"
                        state.stepCount = 1
                        initSimulatedCandles(state)
                        Log.d("MarketScannerEngine", "$coin swept PDL at $p. Mark Potential SHORT Setup.")
                    }
                }

                "PDHL Sweep" -> {
                    // Check if it makes three-candle confirmation
                    state.stepCount++
                    addCandle(state, "15m")
                    if (state.stepCount >= 3) {
                        // 15% chance of failing three-candle confirmation
                        if (Random.nextDouble() < 0.15) {
                            rejectSetup(state, "No three-candle confirmation")
                        } else {
                            state.stage = "Three-Candle Confirmation"
                            state.stepCount = 0
                            Log.d("MarketScannerEngine", "$coin confirmed on 15m. Move to 5m FVG Confirmation.")
                        }
                    }
                }

                "Three-Candle Confirmation" -> {
                    // Check for FVG creation on 5m
                    state.stepCount++
                    addCandle(state, "5m", isFvg = true)
                    // 15% chance of FVG reject
                    if (Random.nextDouble() < 0.15) {
                        rejectSetup(state, "No FVG created on 5m")
                    } else {
                        state.stage = "5m confirmation (FVG)"
                        state.stepCount = 0
                        Log.d("MarketScannerEngine", "$coin created FVG on 5m. Waiting for FVG Retest.")
                    }
                }

                "5m confirmation (FVG)" -> {
                    // Wait for price to retest FVG
                    state.stepCount++
                    val lastCandle = state.candlesList.lastOrNull()
                    if (lastCandle != null) {
                        state.candlesList[state.candlesList.size - 1] = lastCandle.copy(label = "FVG Retest")
                    }

                    if (Random.nextDouble() < 0.15) {
                        rejectSetup(state, "FVG not respected during retest")
                    } else {
                        state.stage = "BOS confirmation"
                        state.stepCount = 0
                        Log.d("MarketScannerEngine", "$coin retested and respected FVG. Waiting for BOS.")
                    }
                }

                "BOS confirmation" -> {
                    // Confirm BOS / CHoCH
                    state.stepCount++
                    addCandle(state, "5m", isBos = true)

                    // --- AI PROBABILITY ENGINE REGIME ANALYSIS ---
                    // Explicitly evaluate ATR and Spread constraints before issuing signal alerts
                    val atr = state.candlesList.map { Math.abs(it.high - it.low) }.average()
                    val atrPercent = if (state.currentPrice > 0.0) (atr / state.currentPrice) * 100.0 else 0.5
                    val spread = Random.nextDouble(0.1, 4.0) // simulated bid/ask spread in pips

                    if (atrPercent < 0.15) { // Low ATR regime (threshold 0.15% of asset price)
                        rejectSetup(state, "Low ATR Regime (${String.format("%.3f", atrPercent)}% < 0.15% min). Volatility is insufficient for sustained continuation.")
                    } else if (spread > 2.0) { // Wide spread risk threshold of 2.0 pips
                        rejectSetup(state, "Wide Bid/Ask Spread (${String.format("%.2f", spread)} pips > 2.0 max). Execution slippage risk too high.")
                    } else if (Random.nextDouble() < 0.15) {
                        rejectSetup(state, "No BOS/CHoCH confirmation")
                    } else {
                        // Risk Engine Calculation
                        val minRr = settings.minRr
                        val currentRr = Random.nextDouble(1.2, 3.2) // random R:R ratio

                        val stopLoss = if (state.direction == "LONG") state.currentPrice * 0.985 else state.currentPrice * 1.015
                        val takeProfit = if (state.direction == "LONG") state.currentPrice * (1.0 + (0.015 * currentRr)) else state.currentPrice * (1.0 - (0.015 * currentRr))

                        if (currentRr < minRr) {
                            rejectSetup(state, "R:R below 1:${minRr} (Calculated 1:${String.format("%.2f", currentRr)})")
                        } else {
                            state.stage = "Ready"
                            // Save setup and trigger Alert Notification
                            val setup = MarketSetup(
                                coin = state.coin,
                                direction = state.direction,
                                setupType = state.setupType,
                                livePrice = state.currentPrice,
                                pdhlPct = Random.nextDouble(0.5, 4.5),
                                confidence = Random.nextDouble(75.0, 96.0),
                                rrRatio = currentRr,
                                stopLoss = stopLoss,
                                takeProfit = takeProfit,
                                entryPrice = state.currentPrice,
                                currentStage = "Ready for 1m Entry",
                                isRejected = false,
                                reasonQualified = "15m ${state.setupType} sweep + 5m FVG respected + BOS confirmed.",
                                candleDataJson = candleListAdapter.toJson(state.candlesList)
                            )
                            val id = repository.insertSetup(setup)
                            val setupWithId = setup.copy(id = id.toInt())
                            
                            // Send custom push notification event
                            if (settings.isNotificationEnabled) {
                                _notifications.emit(setupWithId)
                            }
                            Log.d("MarketScannerEngine", "$coin Alert Generated! R:R 1:${String.format("%.2f", currentRr)}")
                        }
                    }
                }

                "Ready" -> {
                    // Active alert stays ready for a while, then enters journal or resets
                    // Let's simulate a setup entering executed/expired stage after a while
                    delay(1000) // avoid overlapping too fast
                    state.stage = "Scanning" // Reset back to scan
                }
            }
        }
    }

    private suspend fun rejectSetup(state: CoinState, reason: String) {
        val setup = MarketSetup(
            coin = state.coin,
            direction = state.direction,
            setupType = state.setupType,
            livePrice = state.currentPrice,
            pdhlPct = Random.nextDouble(0.5, 3.5),
            confidence = Random.nextDouble(45.0, 68.0),
            rrRatio = Random.nextDouble(1.1, 1.4),
            stopLoss = state.currentPrice * 0.99,
            takeProfit = state.currentPrice * 1.01,
            entryPrice = state.currentPrice,
            currentStage = "Rejected",
            isRejected = true,
            rejectionReason = reason,
            reasonQualified = "Failed confirmation path.",
            candleDataJson = candleListAdapter.toJson(state.candlesList)
        )
        repository.insertSetup(setup)
        
        // Reset state machine back to scanning
        state.stage = "Scanning"
        state.stepCount = 0
        Log.d("MarketScannerEngine", "${state.coin} setup rejected: $reason. Logged in DB.")
    }

    private fun initSimulatedCandles(state: CoinState) {
        state.candlesList.clear()
        // Generate 8 pre-sweep candles
        var price = state.basePrice
        for (i in 1..8) {
            val high = price + (Random.nextDouble() * 0.005 * price)
            val low = price - (Random.nextDouble() * 0.005 * price)
            val close = (high + low) / 2
            state.candlesList.add(
                SimulatedCandle(
                    id = UUID.randomUUID().toString(),
                    time = "10:${15 * i}",
                    open = price,
                    high = high,
                    low = low,
                    close = close,
                    label = if (i == 8) "PDHL Sweep" else null
                )
            )
            price = close
        }
    }

    private fun addCandle(state: CoinState, timeframe: String, isFvg: Boolean = false, isBos: Boolean = false) {
        val last = state.candlesList.lastOrNull()
        val open = last?.close ?: state.currentPrice
        val change = (Random.nextDouble() - 0.5) * 0.01 * open
        val close = open + change
        val high = maxOf(open, close) + (Random.nextDouble() * 0.003 * open)
        val low = minOf(open, close) - (Random.nextDouble() * 0.003 * open)

        val suffix = if (timeframe == "15m") "m" else "s"
        val label = when {
            isFvg -> "FVG Created"
            isBos -> "BOS Confirmed"
            else -> null
        }

        state.candlesList.add(
            SimulatedCandle(
                id = UUID.randomUUID().toString(),
                time = "12:${state.candlesList.size}$suffix",
                open = open,
                high = high,
                low = low,
                close = close,
                isFvg = isFvg,
                isBos = isBos,
                label = label
            )
        )
    }
}
