package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    alpha: Float = 0.1f,
    borderAlpha: Float = 0.2f,
    cornerRadius: Int = 20,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = androidx.compose.foundation.background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                ).brush
                    ?: androidx.compose.foundation.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)).brush
                    ?: androidx.compose.material3.LocalAbsoluteTonalElevation.current.let {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = alpha * 0.1f)
                            )
                        )
                    }
            ),
        color = Color.Transparent,
        content = content
    )
}

@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        content = content
    )
}
