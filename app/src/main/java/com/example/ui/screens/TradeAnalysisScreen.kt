package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.*
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun TradeAnalysisScreen(
    tradeId: String = "",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var animationProgress by remember { mutableStateOf(1f) }

    // Mock data - replace with real data from your data source
    val mockCandles = generateMockCandles()
    val mockLevels = generateMockLevels()
    val mockReplaySteps = generateMockReplaySteps()
    val mockSignalDetails = generateMockSignalDetails()
    val mockPositionData = generateMockPositionData()
    val mockProgression = generateMockProgression()
    val mockAnalysis = generateMockAnalysis()
    val mockPriceDistance = generateMockPriceDistance()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ETH/USD",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "Short Setup | 15M Timeframe",
                            fontSize = 12.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Professional Trade Analysis Workspace",
                    fontSize = 11.sp,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chart section
            Text(
                text = "Candlestick Analysis",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            CandlestickChart(
                candles = mockCandles,
                levels = mockLevels,
                currentPrice = mockPositionData.currentPrice,
                animationProgress = animationProgress,
                highlightedCandleIndices = mockReplaySteps.getOrNull(currentStepIndex)?.chartHighlight ?: emptyList()
            )

            // Replay controls
            TradeReplayPanel(
                steps = mockReplaySteps,
                currentStepIndex = currentStepIndex,
                onStepChange = { currentStepIndex = it }
            )

            // Signal details
            SignalDetailsCard(details = mockSignalDetails)

            // Live position monitor
            LivePositionMonitor(positionData = mockPositionData)

            // Price position indicator
            PricePositionIndicator(
                priceDistance = mockPriceDistance,
                currentPrice = mockPositionData.currentPrice,
                entryPrice = mockPositionData.entryPrice,
                stopLoss = mockPositionData.stopLoss,
                takeProfit = mockPositionData.takeProfit
            )

            // Trade progression
            TradeProgressionTracker(progression = mockProgression)

            // AI Analysis
            AIAnalysisPanel(analysis = mockAnalysis)
        }
    }
}

// Mock data generators
fun generateMockCandles(): List<Candlestick> {
    return listOf(
        Candlestick(1689427200, 1850.0, 1852.5, 1848.0, 1851.0, 1000, false),
        Candlestick(1689427800, 1851.0, 1855.0, 1850.0, 1854.0, 1200, false),
        Candlestick(1689428400, 1854.0, 1858.0, 1852.0, 1857.0, 1100, false),
        Candlestick(1689429000, 1857.0, 1862.0, 1856.0, 1861.0, 1300, true),
        Candlestick(1689429600, 1861.0, 1863.0, 1859.0, 1860.0, 900, false),
        Candlestick(1689430200, 1860.0, 1859.0, 1855.0, 1857.0, 1150, false),
        Candlestick(1689430800, 1857.0, 1858.0, 1854.0, 1855.0, 1050, false),
        Candlestick(1689431400, 1855.0, 1856.0, 1850.0, 1851.0, 1200, false),
    )
}

fun generateMockLevels(): List<TechnicalLevel> {
    return listOf(
        TechnicalLevel("PDH", 1862.5, LevelType.PDH, "1H"),
        TechnicalLevel("PDL", 1840.0, LevelType.PDL, "1H"),
        TechnicalLevel("Entry Zone", 1859.0, LevelType.ENTRY_ZONE, "15M"),
        TechnicalLevel("TP", 1835.0, LevelType.TAKE_PROFIT, "15M"),
        TechnicalLevel("SL", 1870.0, LevelType.STOP_LOSS, "15M"),
    )
}

fun generateMockReplaySteps(): List<TradeReplayStep> {
    return listOf(
        TradeReplayStep(
            1, 1689427200, 1851.0,
            "Initial Setup",
            "Price approaches Previous Day High at 1862.5. Market structure shows potential for reversal.",
            listOf(0)
        ),
        TradeReplayStep(
            2, 1689428400, 1857.0,
            "Liquidity Sweep",
            "Strong bullish candle hits yesterday's high. Buy-side liquidity has been swept by institutional traders.",
            listOf(1, 2, 3)
        ),
        TradeReplayStep(
            3, 1689429000, 1861.0,
            "Bearish Displacement",
            "Large bearish rejection from the high. Price closes at 1861 creating potential selling pressure.",
            listOf(3, 4)
        ),
        TradeReplayStep(
            4, 1689429600, 1860.0,
            "Break of Structure",
            "15-minute chart confirms lower high. Break of structure activated on the downside.",
            listOf(4, 5)
        ),
        TradeReplayStep(
            5, 1689430200, 1857.0,
            "Fair Value Gap",
            "Gap created between candles 3 and 4. This imbalance becomes our retracement target.",
            listOf(5)
        ),
        TradeReplayStep(
            6, 1689430800, 1855.0,
            "5-Minute Confirmation",
            "5-minute chart shows respect of the fair value gap. Price creates lower lows and lower highs.",
            listOf(6, 7)
        ),
        TradeReplayStep(
            7, 1689431400, 1851.0,
            "Entry Activation",
            "Confirmation bar closes below entry zone. 1:2 risk-to-reward setup triggered. Entry is now valid.",
            listOf(7)
        ),
        TradeReplayStep(
            8, 1689431400, 1851.0,
            "Trade Active",
            "Short position active. Stop loss at 1870 (above high). Take profit targets 1835 for 1:2 RR.",
            listOf(7)
        ),
    )
}

fun generateMockSignalDetails(): SignalDetails {
    val now = Instant.now()
    val signalTime = now.minus(3, ChronoUnit.HOURS).minus(25, ChronoUnit.MINUTES)
    return SignalDetails(
        signalGeneratedTime = signalTime.toEpochMilli(),
        signalAgeDuration = "3h 25m ago",
        tradingSession = "London Session",
        status = TradeStatus.RUNNING,
        scanner = "15-minute Liquidity Sweep + BOS",
        entryTriggerTime = now.minus(2, ChronoUnit.HOURS).toEpochMilli(),
        currentMarketTime = now.toEpochMilli(),
        elapsedTimeSinceSignal = "3h 25m"
    )
}

fun generateMockPositionData(): LivePositionData {
    return LivePositionData(
        currentPrice = 1851.5,
        entryPrice = 1859.0,
        stopLoss = 1870.0,
        takeProfit = 1835.0,
        liveRiskReward = 2.14,
        potentialProfitPercent = 1.35,
        potentialLossPercent = 0.61,
        distanceToEntry = 7.5,
        distanceToStop = 18.5,
        distanceToTarget = 16.5,
        unrealizedPnL = 65.25,
        tradeStatus = TradeStatus.RUNNING
    )
}

fun generateMockProgression(): TradeProgression {
    return TradeProgression(
        liquiditySweep = ProgressState.COMPLETED,
        bos = ProgressState.COMPLETED,
        fvgCreated = ProgressState.COMPLETED,
        confirmationM5 = ProgressState.COMPLETED,
        waitingEntry = ProgressState.COMPLETED,
        entryActivated = ProgressState.COMPLETED,
        tradeRunning = ProgressState.ACTIVE,
        tpHit = ProgressState.PENDING
    )
}

fun generateMockAnalysis(): AITradeAnalysis {
    return AITradeAnalysis(
        summary = "ETH swept yesterday's high during the London session, taking external liquidity. A bearish displacement created a Fair Value Gap while simultaneously breaking 15-minute market structure. Price retraced into the imbalance on the 5-minute timeframe, confirming institutional selling pressure. The short setup remains valid while price stays below the structure break.",
        setupExplanation = "The setup is based on institutional price action. Liquidity was pooled above the previous day's high (PDH) at 1862.5. When price swept this level during the London session open, buy-side liquidity was taken. The subsequent bearish reversal from the high indicated institutional selling pressure. This created a clear break of 15-minute market structure.",
        entryRationale = "Entry was triggered when price retraced into the fair value gap on the 5-minute timeframe and confirmed lower highs. The entry zone at 1859 provided a clear invalidation level above. Stop loss was placed above the structure high at 1870, creating a tight 11-pip stop with a 24-pip potential downside for a 1:2.14 risk-reward ratio.",
        riskManagement = "Risk is fixed at 0.61% per trade (11 pips on 1 lot). Profit target is set at 1835 for a 24-pip gain, achieving the 1:2.14 risk-reward setup. Position sizing is adjusted to maintain portfolio risk at 2% maximum. If invalidation level (1870) is breached, position is closed for a small loss.",
        confluenceFactors = listOf(
            "Liquidity sweep at previous day high",
            "Bearish displacement after liquidity grab",
            "Fair value gap imbalance",
            "15-minute break of structure",
            "5-minute lower highs and lower lows",
            "London session high probability window",
            "1:2 risk-reward ratio"
        ),
        timeframeAnalysis = mapOf(
            "1 Hour" to "Structure is down after the liquidation event. Daily context supports downside bias.",
            "15 Min" to "Break of structure confirmed. Lower highs formed. Trend is down.",
            "5 Min" to "Entry timeframe. Lower highs and lows. Price respects fair value gap. Momentum confirms."
        ),
        invalidationLevels = listOf(
            InvalidationLevel(1870.0, "15M", "Structure high. Breach here invalidates the short setup."),
            InvalidationLevel(1865.0, "5M", "If daily close above this level, reconsider bias.")
        ),
        confidenceScore = 0.87f
    )
}

fun generateMockPriceDistance(): PriceDistance {
    return PriceDistance(
        distanceToEntry = 7.5,
        distanceToStop = -18.5,
        distanceToTakeProfit = -16.5,
        entryPercentage = "0.40% below",
        stopPercentage = "0.995% above",
        tpPercentage = "0.89% below"
    )
}
