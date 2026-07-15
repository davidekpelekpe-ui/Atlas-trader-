package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Candlestick
import com.example.data.models.LevelType
import com.example.data.models.TechnicalLevel

@Composable
fun CandlestickChart(
    candles: List<Candlestick>,
    levels: List<TechnicalLevel>,
    currentPrice: Double,
    modifier: Modifier = Modifier,
    animationProgress: Float = 1f,
    highlightedCandleIndices: List<Int> = emptyList()
) {
    val textMeasurer = rememberTextMeasurer()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            if (candles.isEmpty()) return@Canvas

            // Calculate dimensions
            val chartPadding = 60.dp.toPx()
            val chartWidth = size.width - chartPadding * 2
            val chartHeight = size.height - chartPadding * 2

            // Calculate price range
            val highestPrice = candles.maxOf { it.high }
            val lowestPrice = candles.minOf { it.low }
            val priceRange = highestPrice - lowestPrice
            val priceScale = chartHeight / priceRange

            // Draw grid lines
            drawGridLines(
                chartWidth,
                chartHeight,
                chartPadding,
                lowestPrice,
                priceRange,
                textMeasurer
            )

            // Draw technical levels
            levels.forEach { level ->
                drawLevel(
                    level,
                    chartWidth,
                    chartHeight,
                    chartPadding,
                    lowestPrice,
                    priceScale,
                    textMeasurer
                )
            }

            // Draw candlesticks
            val candleWidth = (chartWidth / candles.size * 0.8f).coerceAtLeast(8f)
            val candleSpacing = chartWidth / candles.size

            candles.forEachIndexed { index, candle ->
                val x = chartPadding + (index * candleSpacing) + candleSpacing / 2
                val isHighlighted = index in highlightedCandleIndices
                val progressAdjusted = (index.toFloat() / candles.size).coerceAtMost(animationProgress)

                drawCandlestick(
                    candle,
                    x,
                    chartPadding,
                    candleWidth,
                    lowestPrice,
                    priceScale,
                    isHighlighted,
                    progressAdjusted
                )
            }

            // Draw current price line
            drawCurrentPriceLine(
                currentPrice,
                chartWidth,
                chartHeight,
                chartPadding,
                lowestPrice,
                priceScale,
                textMeasurer
            )

            // Draw axes
            drawAxis(chartPadding, chartWidth, chartHeight)
        }
    }
}

private fun DrawScope.drawCandlestick(
    candle: Candlestick,
    x: Float,
    chartPadding: Float,
    width: Float,
    lowestPrice: Double,
    priceScale: Float,
    isHighlighted: Boolean,
    progressFactor: Float
) {
    val open = (candle.open - lowestPrice) * priceScale * progressFactor
    val close = (candle.close - lowestPrice) * priceScale * progressFactor
    val high = (candle.high - lowestPrice) * priceScale
    val low = (candle.low - lowestPrice) * priceScale

    val yBase = size.height - chartPadding
    val isBullish = candle.close >= candle.open
    val color = if (isBullish) Color(0xFF26a69a) else Color(0xFFef5350)
    val highlightColor = if (isHighlighted) Color(0xFFffd54f) else color

    // Draw wick
    drawLine(
        color = highlightColor.copy(alpha = 0.6f),
        start = Offset(x, yBase - high),
        end = Offset(x, yBase - low),
        strokeWidth = 1.5f
    )

    // Draw body
    val bodyOpen = yBase - open
    val bodyClose = yBase - close
    val bodyHeight = (bodyOpen - bodyClose).coerceAtLeast(1f)
    val bodyY = bodyClose.coerceAtMost(bodyOpen)

    drawRect(
        color = highlightColor,
        topLeft = Offset(x - width / 2, bodyY),
        size = androidx.compose.ui.geometry.Size(width, bodyHeight)
    )

    // Draw highlight border if important
    if (isHighlighted) {
        drawRect(
            color = highlightColor,
            topLeft = Offset(x - width / 2, bodyY),
            size = androidx.compose.ui.geometry.Size(width, bodyHeight),
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawLevel(
    level: TechnicalLevel,
    chartWidth: Float,
    chartHeight: Float,
    chartPadding: Float,
    lowestPrice: Double,
    priceScale: Float,
    textMeasurer: TextMeasurer
) {
    val y = size.height - chartPadding - ((level.price - lowestPrice) * priceScale).toFloat()
    val color = when (level.type) {
        LevelType.PDH -> Color(0xFF2196F3)
        LevelType.PDL -> Color(0xFF1976D2)
        LevelType.ENTRY_ZONE -> Color(0xFF4CAF50)
        LevelType.STOP_LOSS -> Color(0xFFFF5252)
        LevelType.TAKE_PROFIT -> Color(0xFF00E676)
        LevelType.LIQUIDITY_SWEEP -> Color(0xFFFF9800)
        LevelType.BREAK_OF_STRUCTURE -> Color(0xFF9C27B0)
        LevelType.FAIR_VALUE_GAP -> Color(0xFFFFEB3B)
        LevelType.CURRENT_PRICE -> Color(0xFFFFFFFF)
    }

    val strokeWidth = if (level.type == LevelType.CURRENT_PRICE) 2f else 1.5f
    val dashWidth = if (level.type == LevelType.CURRENT_PRICE) 0f else 4f

    // Draw line
    if (dashWidth == 0f) {
        drawLine(
            color = color,
            start = Offset(chartPadding, y),
            end = Offset(chartPadding + chartWidth, y),
            strokeWidth = strokeWidth
        )
    } else {
        drawDashedLine(
            Offset(chartPadding, y),
            Offset(chartPadding + chartWidth, y),
            color,
            strokeWidth
        )
    }

    // Draw label
    val labelText = "${level.name} ${String.format("%.2f", level.price)}"
    val textSize = textMeasurer.measure(labelText, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp))
    
    drawRect(
        color = color.copy(alpha = 0.2f),
        topLeft = Offset(chartPadding + 8, y - textSize.size.height / 2 - 4),
        size = androidx.compose.ui.geometry.Size(textSize.size.width + 8, textSize.size.height + 8f)
    )
}

private fun DrawScope.drawCurrentPriceLine(
    currentPrice: Double,
    chartWidth: Float,
    chartHeight: Float,
    chartPadding: Float,
    lowestPrice: Double,
    priceScale: Float,
    textMeasurer: TextMeasurer
) {
    val y = size.height - chartPadding - ((currentPrice - lowestPrice) * priceScale).toFloat()

    // Draw thick line
    drawLine(
        color = Color(0xFF00E676),
        start = Offset(chartPadding, y),
        end = Offset(chartPadding + chartWidth, y),
        strokeWidth = 2.5f
    )

    // Draw dot on the right
    drawCircle(
        color = Color(0xFF00E676),
        radius = 6f,
        center = Offset(chartPadding + chartWidth + 10, y)
    )
}

private fun DrawScope.drawGridLines(
    chartWidth: Float,
    chartHeight: Float,
    chartPadding: Float,
    lowestPrice: Double,
    priceRange: Double,
    textMeasurer: TextMeasurer
) {
    val gridCount = 5
    for (i in 0..gridCount) {
        val y = chartPadding + (chartHeight / gridCount) * i
        val price = lowestPrice + (priceRange / gridCount) * (gridCount - i)
        
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(chartPadding, y),
            end = Offset(chartPadding + chartWidth, y),
            strokeWidth = 0.5f
        )
    }
}

private fun DrawScope.drawAxis(
    chartPadding: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    // Y-axis
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(chartPadding, chartPadding),
        end = Offset(chartPadding, size.height - chartPadding),
        strokeWidth = 1.5f
    )

    // X-axis
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(chartPadding, size.height - chartPadding),
        end = Offset(chartPadding + chartWidth, size.height - chartPadding),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawDashedLine(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    dashWidth: Float = 4f,
    gapWidth: Float = 4f
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
    val steps = (distance / (dashWidth + gapWidth)).toInt()

    for (i in 0..steps) {
        val t1 = (i * (dashWidth + gapWidth)) / distance
        val t2 = ((i + 1) * dashWidth + i * gapWidth) / distance

        val p1 = Offset(
            start.x + dx * t1,
            start.y + dy * t1
        )
        val p2 = Offset(
            (start.x + dx * t2).coerceAtMost(end.x),
            (start.y + dy * t2).coerceAtMost(end.y)
        )

        drawLine(color, p1, p2, strokeWidth)
    }
}
