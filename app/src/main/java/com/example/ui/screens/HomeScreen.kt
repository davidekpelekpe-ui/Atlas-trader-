package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onShareClick: () -> Unit,
    onQrCodeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Atlas Trader",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Professional Trade Analysis",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 48.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // QR Code Button
        Button(
            onClick = onQrCodeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "QR Code",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Share via QR Code",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Share Button
        Button(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Share App",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
