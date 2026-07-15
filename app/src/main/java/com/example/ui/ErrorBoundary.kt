package com.example.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseManager
import com.example.ui.theme.*

@Composable
fun ErrorBoundary(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    var caughtError by remember { mutableStateOf<Throwable?>(null) }
    var isLogged by remember { mutableStateOf(false) }

    if (caughtError != null) {
        ErrorRecoveryScreen(
            error = caughtError!!,
            email = viewModel.currentUserEmail.collectAsState().value,
            isLogged = isLogged,
            onLogManual = {
                val stackTraceString = caughtError!!.stackTraceToString()
                FirebaseManager.logErrorToFirestore(
                    email = viewModel.currentUserEmail.value,
                    exceptionName = caughtError!!.javaClass.simpleName,
                    message = caughtError!!.localizedMessage ?: "No message details",
                    stackTrace = stackTraceString
                )
                isLogged = true
            },
            onReset = {
                caughtError = null
                isLogged = false
            }
        )
    } else {
        // Intercept uncaught exceptions globally to gracefully display the recovery screen and prevent crashes
        DisposableEffect(Unit) {
            val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                caughtError = throwable
                try {
                    val stackTraceString = throwable.stackTraceToString()
                    FirebaseManager.logErrorToFirestore(
                        email = viewModel.currentUserEmail.value,
                        exceptionName = throwable.javaClass.simpleName,
                        message = throwable.localizedMessage ?: "Uncaught thread exception",
                        stackTrace = stackTraceString
                    )
                } catch (e: Exception) {
                    Log.e("ErrorBoundary", "Logging failed", e)
                }
            }
            onDispose {
                Thread.setDefaultUncaughtExceptionHandler(originalHandler)
            }
        }

        content()
    }
}

@Composable
fun ErrorRecoveryScreen(
    error: Throwable,
    email: String?,
    isLogged: Boolean,
    onLogManual: () -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Glowing alert background
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonRed.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(1.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large styled warning icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(NeonRed.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Alert icon",
                        tint = NeonRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "System Intercepted an Error",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Atlas Trader has gracefully caught an exception to prevent an app crash. This session can be recovered instantly.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                // Error Details Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Exception: ${error.javaClass.simpleName}",
                            color = NeonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = error.localizedMessage ?: "No descriptive error details provided.",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset Button
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reload")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recover & Reload Terminal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Report/Log Error Button
                    OutlinedButton(
                        onClick = onLogManual,
                        enabled = !isLogged,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isLogged) TextMuted else NeonBlue
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isLogged) BorderColor else NeonBlue.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = "Report Bug")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLogged) "Error Logged to Firestore!" else "Report Error to Developer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
