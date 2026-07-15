package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "trades")
data class TradeAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symbol: String,
    val timeframe: String,
    val entryPrice: Double,
    val exitPrice: Double?,
    val riskReward: Double,
    val confidence: Float,
    val notes: String,
    val timestamp: Long,
    @Json(name = "created_at")
    val createdAt: String
)

data class TradeStatistics(
    val totalTrades: Int,
    val winRate: Float,
    val averageRiskReward: Double,
    val profitLoss: Double,
    val bestTrade: Double,
    val worstTrade: Double
)
