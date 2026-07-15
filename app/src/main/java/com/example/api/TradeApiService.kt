package com.example.api

import com.example.data.models.TradeAnalysis
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

interface TradeApiService {
    @GET("/api/trades")
    suspend fun getTrades(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): ApiResponse<List<TradeAnalysis>>

    @POST("/api/trades/analyze")
    suspend fun analyzeTrading(
        @Body request: AnalysisRequest
    ): ApiResponse<TradeAnalysis>

    @GET("/api/trades/{id}")
    suspend fun getTradeById(
        @retrofit2.http.Path("id") id: String
    ): ApiResponse<TradeAnalysis>
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String? = null
)

data class AnalysisRequest(
    val symbol: String,
    val timeframe: String,
    val analysisType: String
)
