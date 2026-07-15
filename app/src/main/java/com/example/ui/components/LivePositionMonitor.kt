package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.LivePositionData
import kotlin.math.absoluteValue

@Composable
fun LivePositionMonitor(
    positionData: LivePositionData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Live Position Monitor",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Risk-to-Reward display
        RiskRewardDisplay(
            liveRR = positionData.liveRiskReward,
            potentialProfit = positionData.potentialProfitPercent,
            potentialLoss = positionData.potentialLossPercent
        )

        // Price levels
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriceLevelRow(
                label = "Current Price",
                price = positionData.currentPrice,
                isPrimary = true
            )
            PriceLevelRow(
                label = "Entry Price",
                price = positionData.entryPrice,
                isPrimary = false
            )
            PriceLevelRow(
                label = "Take Profit",
                price = positionData.takeProfit,
                color = Color(0xFF00E676),
                isPrimary = false
            )
            PriceLevelRow(
                label = "Stop Loss",
                price = positionData.stopLoss,
                color = Color(0xFFFF5252),
                isPrimary = false
            )
        }

        Divider()

        // Distance metrics
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DistanceMetric(
                label = "Distance to Entry",
                distance = positionData.distanceToEntry,
                percentage = ((positionData.distanceToEntry / positionData.entryPrice) * 100).absoluteValue
            )
            DistanceMetric(
                label = "Distance to Stop",
                distance = positionData.distanceToStop,
                percentage = ((positionData.distanceToStop / positionData.stopLoss) * 100).absoluteValue,
                isNegative = true
            )
            DistanceMetric(
                label = "Distance to Target",
                distance = positionData.distanceToTarget,
                percentage = ((positionData.distanceToTarget / positionData.takeProfit) * 100).absoluteValue
            )
        }

        Divider()

        // PnL and Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Unrealized PnL",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = String.format("%.2f", positionData.unrealizedPnL),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (positionData.unrealizedPnL >= 0) Color(0xFF00E676) else Color(0xFFFF5252)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Status",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = positionData.tradeStatus.name.replace("_", " "),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (positionData.tradeStatus.name) {
                        "RUNNING" -> Color(0xFF2196F3)
                        "TP_HIT" -> Color(0xFF00E676)
                        "SL_HIT" -> Color(0xFFFF5252)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }

        Text(
            text = "Live Risk-to-Reward updates in real time based on current market price and dynamic adjustments.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun RiskRewardDisplay(
    liveRR: Double,
    potentialProfit: Double,
    potentialLoss: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
            .padding(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Risk:Reward",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format("1:%.2f", liveRR),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Divider(modifier = Modifier
                    .width(1.dp)
                    .height(40.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Potential Profit",
                        fontSize = 11.sp,
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = "+${String.format("%.2f", potentialProfit)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }

                Divider(modifier = Modifier
                    .width(1.dp)
                    .height(40.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Potential Loss",
                        fontSize = 11.sp,
                        color = Color(0xFFFF5252)
                    )
                    Text(
                        text = "-${String.format("%.2f", potentialLoss)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceLevelRow(
    label: String,
    price: Double,
    color: Color = MaterialTheme.colorScheme.primary,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
                MaterialTheme.shapes.small
            )
            .padding(if (isPrimary) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = String.format("$%.2f", price),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun DistanceMetric(
    label: String,
    distance: Double,
    percentage: Double,
    isNegative: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = String.format("%s%.4f (%.2f%%)", if (isNegative) "-" else "+", distance.absoluteValue, percentage),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    )
}
