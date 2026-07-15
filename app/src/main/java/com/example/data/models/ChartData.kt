package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Candlestick(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val isHighlighted: Boolean = false
)

data class TechnicalLevel(
    val name: String,
    val price: Double,
    val type: LevelType,
    val timeframe: String
)

enum class LevelType {
    PDH, // Previous Day High
    PDL, // Previous Day Low
    ENTRY_ZONE,
    STOP_LOSS,
    TAKE_PROFIT,
    LIQUIDITY_SWEEP,
    BREAK_OF_STRUCTURE,
    FAIR_VALUE_GAP,
    CURRENT_PRICE
}

data class ChartAnnotation(
    val timestamp: Long,
    val price: Double,
    val type: AnnotationType,
    val label: String,
    val description: String
)

enum class AnnotationType {
    SIGNAL_GENERATED,
    ENTRY_TRIGGER,
    LIQUIDITY_SWEEP,
    BOS,
    CHOCH,
    FVG
}

data class TradeReplayStep(
    val stepNumber: Int,
    val timestamp: Long,
    val price: Double,
    val title: String,
    val explanation: String,
    val chartHighlight: List<Int> // Indices of candles to highlight
)

data class SignalDetails(
    val signalGeneratedTime: Long,
    val signalAgeDuration: String,
    val tradingSession: String,
    val status: TradeStatus,
    val scanner: String,
    val entryTriggerTime: Long?,
    val currentMarketTime: Long,
    val elapsedTimeSinceSignal: String
)

enum class TradeStatus {
    ACTIVE,
    WAITING,
    RUNNING,
    TP_HIT,
    SL_HIT,
    EXPIRED
}

data class LivePositionData(
    val currentPrice: Double,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val liveRiskReward: Double,
    val potentialProfitPercent: Double,
    val potentialLossPercent: Double,
    val distanceToEntry: Double,
    val distanceToStop: Double,
    val distanceToTarget: Double,
    val unrealizedPnL: Double,
    val tradeStatus: TradeStatus
)

data class TradeProgression(
    val liquiditySweep: ProgressState = ProgressState.PENDING,
    val bos: ProgressState = ProgressState.PENDING,
    val fvgCreated: ProgressState = ProgressState.PENDING,
    val confirmationM5: ProgressState = ProgressState.PENDING,
    val waitingEntry: ProgressState = ProgressState.PENDING,
    val entryActivated: ProgressState = ProgressState.PENDING,
    val tradeRunning: ProgressState = ProgressState.PENDING,
    val tpHit: ProgressState = ProgressState.PENDING
)

enum class ProgressState {
    PENDING,
    ACTIVE,
    COMPLETED
}

data class PriceDistance(
    val distanceToEntry: Double,
    val distanceToStop: Double,
    val distanceToTakeProfit: Double,
    val entryPercentage: String,
    val stopPercentage: String,
    val tpPercentage: String
)
