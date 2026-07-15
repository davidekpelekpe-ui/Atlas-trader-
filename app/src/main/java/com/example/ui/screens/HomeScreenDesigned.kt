package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset

@Composable
fun HomeScreenDesigned(
    onShareClick: () -> Unit,
    onQrCodeClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.foundation.background(
                    color = MaterialTheme.colorScheme.background
                ).brush
                    ?: androidx.compose.foundation.background(MaterialTheme.colorScheme.background).brush
                    ?: androidx.compose.material3.LocalAbsoluteTonalElevation.current.let {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
            )
    ) {
        // Animated background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                radius = 300f,
                center = Offset(size.width * 0.2f, size.height * 0.3f)
            )
            drawCircle(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                radius = 250f,
                center = Offset(size.width * 0.8f, size.height * 0.7f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with premium styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Glowing orb icon placeholder
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "⚡",
                                fontSize = 64.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Text(
                        text = "ATLAS TRADER",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "AI-Powered Strategy Driven",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(6.dp, 2.dp),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "🟢 PREMIUM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp, 4.dp)
                        )
                    }
                }
            }

            // Main action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share via QR Code button
                Button(
                    onClick = onQrCodeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📱 Share via QR Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Share App button
                Button(
                    onClick = onShareClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔗 Share App",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Feature cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = "📊",
                    title = "Live Scanner",
                    description = "Real-time trade signals"
                )
                FeatureCard(
                    icon = "🤖",
                    title = "AI Analysis",
                    description = "Institutional-grade insights"
                )
                FeatureCard(
                    icon = "⚙️",
                    title = "Settings",
                    description = "Customize your experience",
                    onClick = onSettingsClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                    )
                )
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                fontSize = 32.sp
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                "→",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
