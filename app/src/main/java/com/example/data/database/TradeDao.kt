package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.TradeAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Insert
    suspend fun insertTrade(trade: TradeAnalysis)

    @Update
    suspend fun updateTrade(trade: TradeAnalysis)

    @Delete
    suspend fun deleteTrade(trade: TradeAnalysis)

    @Query("SELECT * FROM trades ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getTrades(limit: Int = 10, offset: Int = 0): Flow<List<TradeAnalysis>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: Int): TradeAnalysis?

    @Query("SELECT COUNT(*) FROM trades")
    suspend fun getTotalTradeCount(): Int

    @Query("SELECT * FROM trades ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeAnalysis>>

    @Query("DELETE FROM trades")
    suspend fun deleteAllTrades()
}
