package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProgressState
import com.example.data.models.TradeProgression

@Composable
fun TradeProgressionTracker(
    progression: TradeProgression,
    modifier: Modifier = Modifier
) {
    val progressItems = listOf(
        "Liquidity Sweep" to progression.liquiditySweep,
        "BOS" to progression.bos,
        "FVG Created" to progression.fvgCreated,
        "5m Confirmation" to progression.confirmationM5,
        "Waiting Entry" to progression.waitingEntry,
        "Entry Activated" to progression.entryActivated,
        "Trade Running" to progression.tradeRunning,
        "TP Hit" to progression.tpHit
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Trade Progression",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            progressItems.forEachIndexed { index, (label, state) ->
                ProgressItem(
                    label = label,
                    state = state,
                    isLast = index == progressItems.size - 1
                )
            }
        }
    }
}

@Composable
fun ProgressItem(
    label: String,
    state: ProgressState,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        when (state) {
            ProgressState.COMPLETED -> Color(0xFF4CAF50)
            ProgressState.ACTIVE -> Color(0xFFFFC107)
            ProgressState.PENDING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        }
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(statusColor.copy(alpha = 0.2f), CircleShape)
                .then(
                    if (state == ProgressState.ACTIVE) {
                        Modifier.background(statusColor.copy(alpha = 0.1f), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                ProgressState.COMPLETED -> {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                }
                ProgressState.ACTIVE -> {
                    Icon(
                        Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
        }

        // Timeline content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (state == ProgressState.PENDING)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (state == ProgressState.ACTIVE) {
                Text(
                    text = "In Progress",
                    fontSize = 10.sp,
                    color = Color(0xFFFFC107)
                )
            }
        }

        // Timeline connector
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(
                        if (state == ProgressState.COMPLETED || state == ProgressState.ACTIVE)
                            statusColor
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
            )
        }
    }
}
