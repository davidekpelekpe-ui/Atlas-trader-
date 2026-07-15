package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.SignalDetails
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SignalDetailsCard(
    details: SignalDetails,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Signal Information",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Grid layout for signal details
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(
                label = "Signal Generated",
                value = SimpleDateFormat("dd MMM yyyy HH:mm UTC", Locale.US).format(
                    Date(details.signalGeneratedTime)
                ),
                highlighted = true
            )

            DetailRow(
                label = "Signal Age",
                value = details.signalAgeDuration,
                highlighted = false
            )

            DetailRow(
                label = "Trading Session",
                value = details.tradingSession,
                highlighted = false
            )

            DetailRow(
                label = "Status",
                value = details.status.name,
                highlighted = true,
                statusColor = when (details.status.name) {
                    "ACTIVE" -> Color(0xFF4CAF50)
                    "RUNNING" -> Color(0xFF2196F3)
                    "TP_HIT" -> Color(0xFF00E676)
                    "SL_HIT" -> Color(0xFFFF5252)
                    else -> Color(0xFFFFC107)
                }
            )

            DetailRow(
                label = "Scanner",
                value = details.scanner,
                highlighted = false
            )

            DetailRow(
                label = "Elapsed Time",
                value = details.elapsedTimeSinceSignal,
                highlighted = false
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    highlighted: Boolean = false,
    statusColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (highlighted) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
                MaterialTheme.shapes.small
            )
            .padding(if (highlighted) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (statusColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, MaterialTheme.shapes.small)
                )
            }
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
