package com.example.data.models

data class AITradeAnalysis(
    val summary: String,
    val setupExplanation: String,
    val entryRationale: String,
    val riskManagement: String,
    val confluenceFactors: List<String>,
    val timeframeAnalysis: Map<String, String>,
    val invalidationLevels: List<InvalidationLevel>,
    val confidenceScore: Float
)

data class InvalidationLevel(
    val level: Double,
    val timeframe: String,
    val explanation: String
)
