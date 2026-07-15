package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.TradeAnalysis

@Database(entities = [TradeAnalysis::class], version = 1, exportSchema = false)
abstract class TradeDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var instance: TradeDatabase? = null

        fun getInstance(context: Context): TradeDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TradeDatabase::class.java,
                    "trade_database"
                ).build().also { instance = it }
            }
        }
    }
}
