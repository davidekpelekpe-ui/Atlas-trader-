package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TradeReplayStep
import kotlinx.coroutines.delay

@Composable
fun TradeReplayPanel(
    steps: List<TradeReplayStep>,
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var replaySpeed by remember { mutableStateOf(1f) }
    var autoProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && currentStepIndex < steps.size - 1) {
            while (isPlaying && currentStepIndex < steps.size - 1) {
                delay((2000 / replaySpeed).toLong()) // Adjust speed
                onStepChange((currentStepIndex + 1).coerceAtMost(steps.size - 1))
                if (currentStepIndex >= steps.size - 1) {
                    isPlaying = false
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step information
        if (currentStepIndex < steps.size) {
            val step = steps[currentStepIndex]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step ${step.stepNumber} of ${steps.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = step.title,
                            modifier = Modifier.padding(6.dp, 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = step.explanation,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Timeline slider
        Slider(
            value = currentStepIndex.toFloat(),
            onValueChange = { onStepChange(it.toInt()) },
            valueRange = 0f..(steps.size - 1).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )

        // Speed controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(0.5f, 1f, 2f, 4f).forEach { speed ->
                FilterChip(
                    selected = replaySpeed == speed,
                    onClick = { replaySpeed = speed },
                    label = { Text("${speed}x", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                )
            }
        }

        // Playback controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onStepChange((currentStepIndex - 1).coerceAtLeast(0))
                    isPlaying = false
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    onStepChange((currentStepIndex + 1).coerceAtMost(steps.size - 1))
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    onStepChange(0)
                    isPlaying = false
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Replay",
                    tint = Color.White
                )
            }
        }
    }
}
