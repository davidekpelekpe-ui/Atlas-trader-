package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PriceDistance

@Composable
fun PricePositionIndicator(
    priceDistance: PriceDistance,
    currentPrice: Double,
    entryPrice: Double,
    stopLoss: Double,
    takeProfit: Double,
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
            text = "Current Market Position",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Price position visualization
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceLevelBox(
                label = "Current Price",
                price = currentPrice,
                color = Color(0xFF00E676),
                isCurrent = true
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            PriceLevelBox(
                label = "Entry Zone",
                price = entryPrice,
                color = Color(0xFF2196F3),
                distance = priceDistance.distanceToEntry,
                distanceText = priceDistance.entryPercentage
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            PriceLevelBox(
                label = "Take Profit",
                price = takeProfit,
                color = Color(0xFF00E676),
                distance = priceDistance.distanceToTakeProfit,
                distanceText = priceDistance.tpPercentage
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            PriceLevelBox(
                label = "Stop Loss",
                price = stopLoss,
                color = Color(0xFFFF5252),
                distance = priceDistance.distanceToStop,
                distanceText = priceDistance.stopPercentage
            )
        }

        // Summary text
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Price is:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "• ${priceDistance.entryPercentage} from Entry\n• ${priceDistance.stopPercentage} from Stop Loss\n• ${priceDistance.tpPercentage} from Take Profit",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PriceLevelBox(
    label: String,
    price: Double,
    color: Color,
    distance: Double = 0.0,
    distanceText: String = "",
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) color.copy(alpha = 0.2f) else Color.Transparent,
                MaterialTheme.shapes.small
            )
            .padding(if (isCurrent) 8.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = String.format("$%.2f", price),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        if (distanceText.isNotEmpty()) {
            Text(
                text = distanceText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .padding(8.dp, 4.dp)
            )
        }
    }
}
