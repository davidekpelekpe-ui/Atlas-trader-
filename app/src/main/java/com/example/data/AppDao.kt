package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Market Setups ---
    @Query("SELECT * FROM market_setups ORDER BY timestamp DESC")
    fun getAllSetups(): Flow<List<MarketSetup>>

    @Query("SELECT * FROM market_setups WHERE isRejected = 0 ORDER BY timestamp DESC")
    fun getActiveSetups(): Flow<List<MarketSetup>>

    @Query("SELECT * FROM market_setups WHERE isRejected = 1 ORDER BY timestamp DESC")
    fun getRejectedSetups(): Flow<List<MarketSetup>>

    @Query("SELECT * FROM market_setups WHERE id = :id LIMIT 1")
    suspend fun getSetupById(id: Int): MarketSetup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: MarketSetup): Long

    @Update
    suspend fun updateSetup(setup: MarketSetup)

    @Query("DELETE FROM market_setups")
    suspend fun clearAllSetups()

    // --- Trade Journal ---
    @Query("SELECT * FROM trade_journal ORDER BY timestamp DESC")
    fun getTradeJournal(): Flow<List<TradeJournal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeJournal): Long

    @Query("DELETE FROM trade_journal WHERE id = :id")
    suspend fun deleteTrade(id: Int)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}
