package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_setups")
data class MarketSetup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coin: String,
    val direction: String, // "LONG", "SHORT"
    val setupType: String, // "Liquidity Sweep", "Real Break"
    val livePrice: Double,
    val pdhlPct: Double,
    val confidence: Double, // percentage, e.g. 85.0
    val rrRatio: Double, // e.g. 2.3
    val stopLoss: Double,
    val takeProfit: Double,
    val entryPrice: Double,
    val currentStage: String, // "Waiting for 15m confirmation", "Waiting for FVG Retest", "Waiting for BOS", "Ready for 1m Entry", "Executed", "Expired", "Missed"
    val isRejected: Boolean = false,
    val rejectionReason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val reasonQualified: String = "",
    val candleDataJson: String = "" // For Strategy Replay
)

@Entity(tableName = "trade_journal")
data class TradeJournal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coin: String,
    val direction: String,
    val setupType: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val profitLoss: Double, // Positive or negative
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val screenshotBase64: String? = null,
    val voiceNoteText: String? = null, // For transcribed thoughts
    val aiMetricsJson: String = "" // For advanced statistical learning metrics
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val minPdhl: Double = 1.0,
    val minRr: Double = 1.5,
    val excludedCoins: String = "USDC,BUSD",
    val scannerSensitivity: String = "Medium", // "Low", "Medium", "High"
    val isDarkMode: Boolean = true,
    val isNotificationEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val autoJournal: Boolean = true,
    val bybitApiKey: String = "",
    val bybitApiSecret: String = "",
    val bybitUseTestnet: Boolean = true,
    val userDisplayName: String = "Trader Pro",
    val userAvatar: String = "avatar_bull",
    val selectedTheme: String = "obsidian_pitch_black"
)
