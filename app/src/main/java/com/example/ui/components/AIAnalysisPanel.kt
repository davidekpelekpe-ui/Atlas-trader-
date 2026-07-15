package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AITradeAnalysis

@Composable
fun AIAnalysisPanel(
    analysis: AITradeAnalysis,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Atlas AI Analysis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Confidence: ${String.format("%.0f", analysis.confidenceScore * 100)}%",
                        modifier = Modifier.padding(8.dp, 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Text(
                text = analysis.summary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Divider()

        // Setup Explanation
        AnalysisSection(
            title = "Setup Explanation",
            content = analysis.setupExplanation
        )

        // Entry Rationale
        AnalysisSection(
            title = "Entry Rationale",
            content = analysis.entryRationale
        )

        // Risk Management
        AnalysisSection(
            title = "Risk Management",
            content = analysis.riskManagement
        )

        // Confluence Factors
        if (analysis.confluenceFactors.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Confluence Factors",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    analysis.confluenceFactors.forEach { factor ->
                        ConfluenceFactor(text = factor)
                    }
                }
            }
        }

        // Timeframe Analysis
        if (analysis.timeframeAnalysis.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Timeframe Analysis",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                analysis.timeframeAnalysis.forEach { (timeframe, analysis) ->
                    TimeframeAnalysisRow(timeframe = timeframe, analysis = analysis)
                }
            }
        }

        // Invalidation Levels
        if (analysis.invalidationLevels.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Invalidation Levels",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                analysis.invalidationLevels.forEach { level ->
                    InvalidationLevelRow(
                        price = level.level,
                        timeframe = level.timeframe,
                        explanation = level.explanation
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = content,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ConfluenceFactor(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .align(androidx.compose.ui.Alignment.CenterVertically)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun TimeframeAnalysisRow(
    timeframe: String,
    analysis: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = timeframe,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = analysis,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun InvalidationLevelRow(
    price: Double,
    timeframe: String,
    explanation: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFF5252).copy(alpha = 0.1f), MaterialTheme.shapes.small)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format("%.2f", price),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF5252)
            )
            Text(
                text = timeframe,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = explanation,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )
    }
}
