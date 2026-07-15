package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    val allSetups: Flow<List<MarketSetup>> = dao.getAllSetups()
    val activeSetups: Flow<List<MarketSetup>> = dao.getActiveSetups()
    val rejectedSetups: Flow<List<MarketSetup>> = dao.getRejectedSetups()
    val tradeJournal: Flow<List<TradeJournal>> = dao.getTradeJournal()
    val settingsFlow: Flow<AppSettings?> = dao.getSettingsFlow()

    suspend fun getSetupById(id: Int): MarketSetup? = dao.getSetupById(id)

    suspend fun insertSetup(setup: MarketSetup): Long = dao.insertSetup(setup)

    suspend fun updateSetup(setup: MarketSetup) = dao.updateSetup(setup)

    suspend fun clearAllSetups() = dao.clearAllSetups()

    suspend fun insertTrade(trade: TradeJournal): Long = dao.insertTrade(trade)

    suspend fun deleteTrade(id: Int) = dao.deleteTrade(id)

    suspend fun getSettingsDirect(): AppSettings? = dao.getSettingsDirect()

    suspend fun saveSettings(settings: AppSettings) = dao.saveSettings(settings)
}
