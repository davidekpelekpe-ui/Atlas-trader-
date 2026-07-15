package com.example.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.TradeJournal
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportTradeJournalToCsv(context: Context, trades: List<TradeJournal>) {
        if (trades.isEmpty()) {
            Toast.makeText(context, "No trades in journal to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val cacheDir = context.cacheDir
            val csvFile = File(cacheDir, "atlas_trading_journal.csv")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            csvFile.bufferedWriter().use { writer ->
                // Write Header
                writer.write("ID,Coin,Direction,Setup Type,Entry Price,Exit Price,Profit/Loss (USDT),Timestamp,Notes,Voice Note Text\n")
                
                // Write Data rows
                trades.forEach { trade ->
                    val id = trade.id
                    val coin = escapeCsvValue(trade.coin)
                    val direction = escapeCsvValue(trade.direction)
                    val setupType = escapeCsvValue(trade.setupType)
                    val entryPrice = trade.entryPrice
                    val exitPrice = trade.exitPrice
                    val pnl = trade.profitLoss
                    val formattedDate = sdf.format(Date(trade.timestamp))
                    val notes = escapeCsvValue(trade.notes)
                    val voiceNote = escapeCsvValue(trade.voiceNoteText ?: "")
                    
                    writer.write("$id,$coin,$direction,$setupType,$entryPrice,$exitPrice,$pnl,$formattedDate,$notes,$voiceNote\n")
                }
            }

            // Share File via intent
            val contentUri = FileProvider.getUriForFile(context, "com.example.fileprovider", csvFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Atlas Trading Journal Export")
                putExtra(Intent.EXTRA_TEXT, "Attached is the exported trading journal CSV file containing ${trades.size} entries from Atlas Trader.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share Trading Journal CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsvValue(value: String): String {
        var clean = value.replace("\n", " ").replace("\r", " ")
        if (clean.contains(",") || clean.contains("\"") || clean.contains("'")) {
            clean = clean.replace("\"", "\"\"")
            clean = "\"$clean\""
        }
        return clean
    }
}
