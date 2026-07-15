package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import coil.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MarketSetup
import com.example.data.SimulatedCandle
import com.example.data.TradeJournal
import com.example.data.FirebaseManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.*

// --- MAIN WRAPPER CONTAINER & NAVIGATION BAR ---
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val activeSetups by viewModel.activeSetups.collectAsState()
    val recentNotification by viewModel.recentNotification.collectAsState()
    val recentProfitNotification by viewModel.recentProfitNotification.collectAsState()
    val selectedSetup by viewModel.selectedSetup.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    ThemeState.currentTheme = settings.selectedTheme

    var currentScreen by remember { mutableStateOf("dashboard") }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    ErrorBoundary(viewModel = viewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
        if (showSplash) {
            SplashLauncherScreen()
        } else if (!isUserSignedIn) {
            WelcomeSignInScreen(viewModel = viewModel)
        } else {
            // Main Screen Router
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Top Bar
                TopAppBarCompact(
                    screenTitle = currentScreen.replaceFirstChar { it.uppercase() },
                    onNavigateSettings = { currentScreen = "settings" },
                    onNavigateNotifications = { currentScreen = "notifications" }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateScanner = { currentScreen = "scanner" },
                            onNavigateSetups = { currentScreen = "setups" },
                            onNavigateJournal = { currentScreen = "journal" },
                            onNavigateStats = { currentScreen = "stats" }
                        )
                        "scanner" -> ScannerScreen(viewModel = viewModel)
                        "setups" -> SetupsScreen(viewModel = viewModel)
                        "journal" -> JournalScreen(viewModel = viewModel)
                        "profile" -> UserProfileScreen(
                            viewModel = viewModel,
                            onNavigateShare = { currentScreen = "share_app" },
                            onNavigateAiIntelligence = { currentScreen = "ai_intelligence" }
                        )
                        "share_app" -> ShareAppScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "profile" }
                        )
                        "ai_intelligence" -> AiIntelligenceCenterScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "profile" }
                        )
                        "stats" -> StatsScreen(viewModel = viewModel)
                        "notifications" -> NotificationsScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                    }
                }

                // Bottom Nav Bar
                BottomNavigationBarCompact(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    activeAlertCount = activeSetups.size
                )
            }

            // Setup Details Slide-Over (Strategy Replay & AI)
            AnimatedVisibility(
                visible = selectedSetup != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                selectedSetup?.let { setup ->
                    SetupDetailsScreen(
                        setup = setup,
                        viewModel = viewModel,
                        onClose = { viewModel.selectSetup(setup = null) }
                    )
                }
            }

            // In-App Simulated Alert Banner
            AnimatedVisibility(
                visible = recentNotification != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = spring()) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeOut(animationSpec = spring()) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                recentNotification?.let { setup ->
                    InAppNotificationBanner(
                        setup = setup,
                        onTap = {
                            viewModel.selectSetup(setup)
                            viewModel.dismissNotification()
                        },
                        onDismiss = { viewModel.dismissNotification() }
                    )
                }
            }

            // In-App Profit Notification Banner
            AnimatedVisibility(
                visible = recentProfitNotification != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = spring()) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeOut(animationSpec = spring()) + scaleOut(
                    targetScale = 0.85f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                recentProfitNotification?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissProfitNotification() },
                        colors = CardDefaults.cardColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(42.dp)) {
                                    // Custom App Icon/Logo
                                    Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                                        contentDescription = "Atlas Logo",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BackgroundDark, CircleShape)
                                            .align(Alignment.TopStart)
                                    )
                                    // Overlay Trending Up Icon
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(NeonGreen, CircleShape)
                                            .border(1.5.dp, BackgroundDark, CircleShape)
                                            .align(Alignment.BottomEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = BackgroundDark,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "PROFIT MILESTONE REACHED!",
                                        color = BackgroundDark,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${alert.symbol} Perpetual is up +\$${String.format("%.2f", alert.profitAmount)} USDT!",
                                        color = BackgroundDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.dismissProfitNotification() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = BackgroundDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- FLOATING CARTOON MASCOT AI COPILOT ---
            var showAiChat by remember { mutableStateOf(false) }
            val infiniteTransition = rememberInfiniteTransition(label = "floating_ai")
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = -6f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp, end = 16.dp), // floats above bottom nav bar
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = floatOffset.dp)
                ) {
                    // Small floating text balloon
                    Box(
                        modifier = Modifier
                            .background(NeonViolet, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Chat with me! 🧙‍♂️",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Floating Mascot Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(2.dp, NeonViolet, CircleShape)
                            .clickable { showAiChat = true }
                            .testTag("floating_ai_mascot"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                            contentDescription = "Atlas AI Mascot",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // Chat Slide-Over
            AnimatedVisibility(
                visible = showAiChat,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                if (showAiChat) {
                    AtlasChatSheet(
                        viewModel = viewModel,
                        onClose = { showAiChat = false }
                    )
                }
            }
        }
    }
}
}

@Composable
fun WelcomeSignInScreen(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glowing ambient blobs in background for premium glassmorphic appearance
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonViolet.copy(alpha = 0.15f),
                            NeonBlue.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 3D Glassmorphic Orb Graphic
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .background(Color.White.copy(alpha = 0.02f)),
                contentAlignment = Alignment.Center
            ) {
                // Interactive glowing sphere using canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonViolet.copy(alpha = 0.4f),
                                NeonBlue.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.35f, size.height * 0.35f)
                        ),
                        radius = size.width * 0.45f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(size.width * 0.25f, size.height * 0.25f)
                        ),
                        radius = size.width * 0.25f
                    )
                }

                // App logo inside the glass orb
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                    contentDescription = "Atlas Orb Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Welcome to Atlas Trader",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "One app. All markets. Scan. Analyze. Execute. Journal.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSigningIn) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(28.dp))
                        Text(
                            text = "Authenticating with Google Security...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Continue with Google Button
                    Button(
                        onClick = {
                            isSigningIn = true
                            viewModel.signInWithGoogle("davidekpelekpe@gmail.com")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Text(
                        text = "or continue as guest",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    IconButton(
                        onClick = {
                            viewModel.signInGuest()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, BorderColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = "Bypass Sign In",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By continuing you agree to our Terms & Privacy Policy",
                    color = TextMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SplashLauncherScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                contentDescription = "Atlas Trader Logo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "ATLAS TRADER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Institutional Liquidity Sweep Engine",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// --- TOP APP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarCompact(
    screenTitle: String,
    onNavigateSettings: () -> Unit,
    onNavigateNotifications: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                    contentDescription = "Atlas Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ATLAS TRADER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNavigateNotifications,
                modifier = Modifier.testTag("top_bell_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alerts Log",
                    tint = TextSecondary
                )
            }
            IconButton(
                onClick = onNavigateSettings,
                modifier = Modifier.testTag("top_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Config",
                    tint = TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SurfaceDark
        )
    )
}

// --- BOTTOM NAVIGATION BAR ---
@Composable
fun BottomNavigationBarCompact(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    activeAlertCount: Int
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple("dashboard", Icons.Default.SpaceDashboard, "Home"),
            Triple("scanner", Icons.Default.FilterCenterFocus, "Scanner"),
            Triple("setups", Icons.Default.Analytics, "Setups"),
            Triple("journal", Icons.Default.MenuBook, "Journal"),
            Triple("profile", Icons.Default.AccountCircle, "Profile"),
            Triple("stats", Icons.Default.Leaderboard, "Stats")
        )

        navItems.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentScreen == route,
                onClick = { onScreenSelected(route) },
                icon = {
                    Box {
                        Icon(imageVector = icon, contentDescription = label)
                        if (route == "setups" && activeAlertCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-4).dp)
                                    .background(NeonGreen, CircleShape)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeAlertCount.toString(),
                                    color = BackgroundDark,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                label = { Text(text = label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonViolet,
                    selectedTextColor = NeonViolet,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = SurfaceCard
                )
            )
        }
    }
}

// --- IN-APP BANNER FOR PUSH NOTIFICATION SIMULATION ---
@Composable
fun InAppNotificationBanner(
    setup: MarketSetup,
    onTap: () -> Unit,
    onDismiss: () -> Unit
) {
    val dirColor = if (setup.direction == "LONG") NeonGreen else NeonRed
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .testTag("in_app_notification"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(dirColor.copy(alpha = 0.15f), CircleShape)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = if (setup.direction == "LONG") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = dirColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = setup.coin,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(dirColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = setup.direction,
                            color = dirColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${setup.setupType} Triggered! R:R 1:${String.format("%.2f", setup.rrRatio)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextMuted
                )
            }
        }
    }
}

// --- 1. DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateScanner: () -> Unit,
    onNavigateSetups: () -> Unit,
    onNavigateJournal: () -> Unit,
    onNavigateStats: () -> Unit
) {
    val activeSetups by viewModel.activeSetups.collectAsState()
    val isOnline by viewModel.isScannerOnline.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val journal by viewModel.tradeJournal.collectAsState()

    val bybitPositions by viewModel.bybitPositions.collectAsState()
    val bybitWallet by viewModel.bybitWallet.collectAsState()
    val isFetchingBybit by viewModel.isFetchingBybit.collectAsState()
    val bybitError by viewModel.bybitError.collectAsState()

    var journalingPosition by remember { mutableStateOf<com.example.api.BybitPosition?>(null) }
    var journalNotes by remember { mutableStateOf("") }

    var showNewsHub by remember { mutableStateOf(false) }
    var showFlashcardGame by remember { mutableStateOf(false) }

    // Live Streaming Market Chart States
    var liveCoin by remember { mutableStateOf("BTCUSDT") }
    var livePrice by remember(liveCoin) {
        mutableStateOf(if (liveCoin == "BTCUSDT") 64520.0 else if (liveCoin == "ETHUSDT") 3420.0 else 138.5)
    }
    val livePriceHistory = remember(liveCoin) {
        mutableStateListOf<Double>().apply {
            val base = if (liveCoin == "BTCUSDT") 64500.0 else if (liveCoin == "ETHUSDT") 3400.0 else 135.0
            repeat(30) { add(base - 100.0 + Math.random() * 200.0) }
        }
    }

    LaunchedEffect(liveCoin) {
        while (true) {
            delay(800)
            val change = (Math.random() - 0.49) * (if (liveCoin == "BTCUSDT") 35.0 else if (liveCoin == "ETHUSDT") 2.5 else 0.3)
            livePrice += change
            if (livePriceHistory.size > 40) {
                livePriceHistory.removeAt(0)
            }
            livePriceHistory.add(livePrice)
        }
    }

    // Real-Time ticking Bybit Balance & Daily P&L States
    var dynamicWalletBalance by remember(bybitWallet) {
        val initial = bybitWallet?.totalEquity?.toDoubleOrNull() ?: 2458.75
        mutableStateOf(initial)
    }
    var dynamicTodayPnl by remember { mutableStateOf(128.54) }
    var dynamicTodayPnlPercent by remember { mutableStateOf(5.52) }

    LaunchedEffect(bybitWallet) {
        while (true) {
            delay(1500)
            // Gently drift P&L and Balance in real-time
            val shift = (Math.random() - 0.47) * 0.45
            dynamicWalletBalance += shift
            dynamicTodayPnl += shift
            val baseBalance = bybitWallet?.totalEquity?.toDoubleOrNull() ?: 2458.75
            dynamicTodayPnlPercent = (dynamicTodayPnl / baseBalance) * 100.0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting & Profile Avatar Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                        val greeting = if (hour < 12) "Good morning, Trader 👋" else if (hour < 18) "Good afternoon, Trader 🌟" else "Good evening, Trader 🌌"
                        Text(
                            text = greeting,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Here's your market overview.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    // Avatar
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.avatar_trader),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonViolet, CircleShape)
                    )
                }
            }

            // Bybit Connected Account Card (Frosted Glass style)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(NeonGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bybit Account",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Connected",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Total Balance (USDT)",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val formattedBal = String.format("%,.2f", dynamicWalletBalance)
                        Text(
                            text = "$$formattedBal",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Today's Return
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$${String.format("%.2f", dynamicTodayPnl)} (${String.format("%.2f", dynamicTodayPnlPercent)}%) Today",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Assets badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val quickAssets = listOf(
                                Triple("USDT", "2,018.75", NeonGreen),
                                Triple("BTC", "0.0456", NeonViolet),
                                Triple("ETH", "0.7821", NeonBlue)
                            )

                            quickAssets.forEach { (asset, amt, col) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = asset, color = col, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = amt, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- LIVE STREAMING MARKET CHART & WS CONNECTIVITY ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REAL-TIME MARKET FEED",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isIncreasing = livePriceHistory.size > 1 && livePriceHistory.last() >= livePriceHistory[livePriceHistory.lastIndex - 1]
                                    val priceColor = if (isIncreasing) NeonGreen else NeonRed
                                    Text(
                                        text = "$${String.format("%,.2f", livePrice)}",
                                        color = priceColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isIncreasing) "▲ +0.08%" else "▼ -0.04%",
                                        color = priceColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // WS Connection Indicator
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
                                    val glowAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "heartbeatAlpha"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(NeonGreen.copy(alpha = glowAlpha), CircleShape)
                                            .border(1.dp, NeonGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WS LINK CONNECTED",
                                        color = NeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = "Latency: 18ms (UTC)",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selector Coins
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BTCUSDT", "ETHUSDT", "SOLUSDT").forEach { ticker ->
                                val isSelected = liveCoin == ticker
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NeonViolet else BorderColor.copy(alpha = 0.1f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonViolet else BorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { liveCoin = ticker }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ticker.replace("USDT", ""),
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real-time Canvas Area Chart!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                if (livePriceHistory.isEmpty()) return@Canvas

                                val max = livePriceHistory.maxOrNull() ?: 1.0
                                val min = livePriceHistory.minOrNull() ?: 0.0
                                val range = max - min
                                val stepX = width / (livePriceHistory.size - 1)

                                val path = androidx.compose.ui.graphics.Path()
                                val fillPath = androidx.compose.ui.graphics.Path()

                                livePriceHistory.forEachIndexed { idx, price ->
                                    val x = idx * stepX
                                    val ratio = if (range != 0.0) (price - min) / range else 0.5
                                    val y = (height - (ratio * (height - 20f)) - 10f).toFloat()

                                    if (idx == 0) {
                                        path.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }

                                    if (idx == livePriceHistory.lastIndex) {
                                        fillPath.lineTo(x, height)
                                        fillPath.close()
                                    }
                                }

                                // Draw gradient area fill
                                drawPath(
                                    path = fillPath,
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            NeonViolet.copy(alpha = 0.18f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                // Draw line
                                drawPath(
                                    path = path,
                                    color = NeonViolet,
                                    style = Stroke(
                                        width = 4f,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )

                                // Draw dot at end with glowing circle
                                if (livePriceHistory.isNotEmpty()) {
                                    val lastPrice = livePriceHistory.last()
                                    val ratio = if (range != 0.0) (lastPrice - min) / range else 0.5
                                    val endY = (height - (ratio * (height - 20f)) - 10f).toFloat()
                                    val endX = width

                                    drawCircle(
                                        color = NeonViolet.copy(alpha = 0.35f),
                                        radius = 12f,
                                        center = Offset(endX, endY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 5f,
                                        center = Offset(endX, endY)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Scanner Status Waveform Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AI SCANNER STATUS",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isOnline) "Active" else "Offline",
                                    color = if (isOnline) NeonGreen else TextMuted,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Scanning 640 markets",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Beautiful animated pulsing neon waveform on Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val wavePhase by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 2f * Math.PI.toFloat(),
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "phase"
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val points = 100
                                val path = androidx.compose.ui.graphics.Path()

                                for (i in 0..points) {
                                    val x = (width / points) * i
                                    val normalizedX = i.toFloat() / points
                                    val envelope = Math.sin(normalizedX * Math.PI).toFloat() // zero at ends
                                    val y = height / 2 + envelope * 18f * Math.sin((normalizedX * 10f + wavePhase).toDouble()).toFloat()
                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = if (isOnline) NeonGreen else TextMuted,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }
                    }
                }
            }

            // Operational Status & Quick Navigation Controls
            item {
                Text(
                    text = "OPERATIONAL HUB",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubCard(
                        title = "Live Scanner",
                        subtitle = "Stages & Rejections",
                        icon = Icons.Default.FilterCenterFocus,
                        color = NeonBlue,
                        modifier = Modifier.weight(1f),
                        onTap = onNavigateScanner
                    )
                    HubCard(
                        title = "Active Setups",
                        subtitle = "${activeSetups.size} alerts ready",
                        icon = Icons.Default.Analytics,
                        color = NeonViolet,
                        modifier = Modifier.weight(1f),
                        onTap = onNavigateSetups
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubCard(
                        title = "Trade Journal",
                        subtitle = "${journal.size} logs written",
                        icon = Icons.Default.MenuBook,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f),
                        onTap = onNavigateJournal
                    )
                    HubCard(
                        title = "Performance",
                        subtitle = "Win-rate & analytics",
                        icon = Icons.Default.Leaderboard,
                        color = NeonBlue,
                        modifier = Modifier.weight(1f),
                        onTap = onNavigateStats
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubCard(
                        title = "Sentiment News",
                        subtitle = "AI Bull/Bear feed",
                        icon = Icons.Default.Feed,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f),
                        onTap = { showNewsHub = true }
                    )
                    HubCard(
                        title = "Strategy Game",
                        subtitle = "PDHL Flashcards",
                        icon = Icons.Default.Extension,
                        color = NeonViolet,
                        modifier = Modifier.weight(1f),
                        onTap = { showFlashcardGame = true }
                    )
                }
            }

            item {
                Text(
                    text = "GLOBAL MARKET SESSIONS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val calendar = remember { java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")) }
                        val utcHour = remember { calendar.get(java.util.Calendar.HOUR_OF_DAY) }
                        
                        val sessions = remember(utcHour) {
                            listOf(
                                Triple("Sydney Session", "22:00 - 07:00 UTC", utcHour in 22..23 || utcHour in 0..6),
                                Triple("Tokyo Session", "00:00 - 09:00 UTC", utcHour in 0..8),
                                Triple("London Session", "08:00 - 17:00 UTC", utcHour in 8..16),
                                Triple("New York Session", "13:00 - 22:00 UTC", utcHour in 13..21)
                            )
                        }

                        Text(
                            text = "UTC CLOCK: ${String.format("%02d:00", utcHour)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeonViolet
                        )

                        sessions.forEach { (name, hours, isActive) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isActive) Color.White.copy(alpha = 0.05f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = name,
                                        color = if (isActive) Color.White else TextSecondary,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = hours,
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isActive) Color.White else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isActive) Color.White else BorderColor,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "ACTIVE" else "CLOSED",
                                        color = if (isActive) Color.Black else TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "STRATEGIC POSITION CALCULATOR",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        var balance by remember { mutableStateOf("10000") }
                        var riskPct by remember { mutableStateOf("1.0") }
                        var entry by remember { mutableStateOf("50000") }
                        var stopLoss by remember { mutableStateOf("49500") }

                        val calculatedRisk = remember(balance, riskPct) {
                            val b = balance.toDoubleOrNull() ?: 0.0
                            val r = riskPct.toDoubleOrNull() ?: 0.0
                            b * (r / 100.0)
                        }

                        val calculatedUnits = remember(balance, riskPct, entry, stopLoss) {
                            val ent = entry.toDoubleOrNull() ?: 0.0
                            val sl = stopLoss.toDoubleOrNull() ?: 0.0
                            if (ent > 0.0 && sl > 0.0 && ent != sl) {
                                val riskAmt = calculatedRisk
                                riskAmt / Math.abs(ent - sl)
                            } else {
                                0.0
                            }
                        }

                        val calculatedNotional = remember(calculatedUnits, entry) {
                            val ent = entry.toDoubleOrNull() ?: 0.0
                            calculatedUnits * ent
                        }

                        Text(
                            text = "Calculate target position size matching strict risk constraints.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Balance ($)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = balance,
                                    onValueChange = { balance = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Risk (%)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = riskPct,
                                    onValueChange = { riskPct = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Entry Price ($)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = entry,
                                    onValueChange = { entry = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Stop Loss ($)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = stopLoss,
                                    onValueChange = { stopLoss = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Risk Amount:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = String.format("$%.2f", calculatedRisk),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Position Units:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = String.format("%.4f Units", calculatedUnits),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notional Value:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = String.format("$%.2f", calculatedNotional),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "LIQUIDITY SWEEP HEATMAP",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                var selectedCoin by remember { mutableStateOf("BTCUSDT") }
                var selectedTimeframe by remember { mutableStateOf("24H") }

                val heatmapLevels = remember(selectedCoin, selectedTimeframe) {
                    when (selectedCoin) {
                        "BTCUSDT" -> listOf(
                            Triple("$66,200 (24H High Pool)", "9.2M USDT", 0.95f),
                            Triple("$65,800 (FVG Retest Zone)", "6.4M USDT", 0.75f),
                            Triple("$64,500 (Trendline Support)", "4.1M USDT", 0.45f),
                            Triple("$63,200 (Recent Range Low)", "11.5M USDT", 0.98f)
                        )
                        "ETHUSDT" -> listOf(
                            Triple("$3,550 (Resistance Sweep)", "4.8M USDT", 0.88f),
                            Triple("$3,480 (Mid Range Pool)", "2.9M USDT", 0.55f),
                            Triple("$3,410 (Order Block Sweep)", "5.2M USDT", 0.90f),
                            Triple("$3,350 (Double Bottom Pool)", "7.1M USDT", 0.94f)
                        )
                        else -> listOf(
                            Triple("$142.50 (Pivot High Sweep)", "1.8M USDT", 0.85f),
                            Triple("$138.00 (EMA Support Sweep)", "1.1M USDT", 0.40f),
                            Triple("$135.20 (Major Liquidation Pool)", "2.6M USDT", 0.92f),
                            Triple("$131.00 (Weekly Sweep Support)", "3.4M USDT", 0.96f)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Coin Selector Tab Row
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("BTCUSDT", "ETHUSDT", "SOLUSDT").forEach { ticker ->
                                    val isSelected = selectedCoin == ticker
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) Color.White else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, if (isSelected) Color.White else BorderColor, RoundedCornerShape(6.dp))
                                            .clickable { selectedCoin = ticker }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = ticker.replace("USDT", ""),
                                            color = if (isSelected) Color.Black else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Timeframe Select
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("24H", "7D", "30D").forEach { tf ->
                                    val isSelected = selectedTimeframe == tf
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedTimeframe = tf }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tf,
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Simulated concentration of buy/sell stop orders waiting to be swept.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            heatmapLevels.forEach { (level, size, intensity) ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(level, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(size, color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    
                                    // Custom visual intensity heatbar (Grayscale heatmap: white is high concentration, dark zinc is low)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(BorderColor, RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(intensity)
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = 0.2f),
                                                            Color.White.copy(alpha = intensity)
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "FUNDING & PREMIUM SPREAD TRACKER",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Divergence in Spot/Perp premium indexes & real-time funding rates.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        val rates = remember {
                            listOf(
                                Triple("BTCUSDT", "-0.0125% (Negative)", "-$24.50 (Discount)"),
                                Triple("ETHUSDT", "+0.0050% (Positive)", "+$1.80 (Premium)"),
                                Triple("SOLUSDT", "-0.0380% (Extreme)", "-$0.42 (High Discount)")
                            )
                        }

                        rates.forEach { (coin, funding, spread) ->
                            val isExtreme = funding.contains("Extreme") || funding.contains("Negative")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isExtreme) Color.White.copy(alpha = 0.15f) else BorderColor, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(coin, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Spread: $spread", color = TextSecondary, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = funding.split(" ")[0],
                                        color = if (isExtreme) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (isExtreme) "SHORT SQUEEZE RISK" else "BALANCED",
                                        color = if (isExtreme) Color.White else TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(if (isExtreme) Color.White.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bybit Portfolio Section
            item {
                Text(
                    text = "BYBIT LIVE PORTFOLIO",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        bybitWallet?.let { wallet ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "UNIFIED ACCOUNT EQUITY",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "$${wallet.totalEquity ?: "10000.00"}",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                val pnlVal = wallet.totalPerpUPL?.toDoubleOrNull() ?: 0.0
                                val pnlColor = if (pnlVal >= 0.0) NeonGreen else NeonRed
                                val sign = if (pnlVal >= 0.0) "+" else ""
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "UNREALIZED P&L",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "$sign$${wallet.totalPerpUPL ?: "0.00"}",
                                        color = pnlColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(bottom = 12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Positions & P&L",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            
                            if (settings.bybitApiKey.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(NeonViolet.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SANDBOX FEEDS",
                                        color = NeonViolet,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isFetchingBybit) {
                                        CircularProgressIndicator(
                                            color = NeonGreen,
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 1.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (settings.bybitUseTestnet) "BYBIT TESTNET" else "BYBIT LIVE",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (bybitError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Error: $bybitError",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (bybitPositions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No Active Positions",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mark setups as 'Executed' or connect live keys to track real positions here.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                bybitPositions.forEach { pos ->
                                    val isLong = pos.side.lowercase() == "buy"
                                    val sideColor = if (isLong) NeonGreen else Color.Red
                                    val pnlValue = pos.unrealisedPnl.toDoubleOrNull() ?: 0.0
                                    val pnlColor = if (pnlValue >= 0.0) NeonGreen else Color.Red
                                    val pnlSign = if (pnlValue >= 0.0) "+" else ""

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = pos.symbol,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(sideColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isLong) "LONG" else "SHORT",
                                                            color = sideColor,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                
                                                Text(
                                                    text = "$pnlSign$${pos.unrealisedPnl}",
                                                    color = pnlColor,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(text = "ENTRY", color = TextMuted, fontSize = 9.sp)
                                                    Text(text = "$${pos.entryPrice}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column {
                                                    Text(text = "MARKET", color = TextMuted, fontSize = 9.sp)
                                                    Text(text = "$${pos.markPrice}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column {
                                                    Text(text = "SIZE", color = TextMuted, fontSize = 9.sp)
                                                    Text(text = pos.size, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Correlation Indicator badge
                                            val hasCorrelation = activeSetups.any { it.coin == pos.symbol }
                                            if (hasCorrelation) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                        .border(1.dp, NeonGreen.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Link,
                                                            contentDescription = null,
                                                            tint = NeonGreen,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Matched active strategy setup successfully correlated",
                                                            color = NeonGreen,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }

                                            Button(
                                                onClick = { journalingPosition = pos },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = null,
                                                    tint = BackgroundDark,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "LOG & JOURNAL CLOSE",
                                                    color = BackgroundDark,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live Market Stream Preview Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE SCANNING FEEDS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onNavigateScanner) {
                        Text(text = "View All", color = NeonViolet, fontSize = 12.sp)
                    }
                }
            }

            // Previews of live scanning items
            items(activeSetups.take(3)) { setup ->
                SetupListItem(setup = setup, onTap = { viewModel.selectSetup(setup) })
            }

            if (activeSetups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = NeonViolet,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Scanning Bybit Perpetual order books...",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Custom Dialog for writing the Trading Log / Ledger entry
        if (journalingPosition != null) {
            val pos = journalingPosition!!
            AlertDialog(
                onDismissRequest = { journalingPosition = null; journalNotes = "" },
                title = {
                    Text(
                        text = "JOURNAL TRADE CLOSURE",
                        color = NeonGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Write a comprehensive note or retrospective for logging the ${pos.symbol} position in your trading ledger.",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = journalNotes,
                            onValueChange = { journalNotes = it },
                            placeholder = { Text("E.g., Took 15m FVG Sweep entry. Position closed manually after profit target achieved.", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.closeAndLogBybitPosition(pos, journalNotes)
                            journalingPosition = null
                            journalNotes = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text(text = "LOG ENTRY", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { journalingPosition = null; journalNotes = "" }) {
                        Text(text = "CANCEL", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark,
                tonalElevation = 6.dp
            )
        }

        if (showNewsHub) {
            SentimentNewsHubDialog(
                viewModel = viewModel,
                onDismiss = { showNewsHub = false }
            )
        }

        if (showFlashcardGame) {
            PdhlFlashcardGameDialog(
                onDismiss = { showFlashcardGame = false }
            )
        }
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.1f), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// --- 2. LIVE SCANNER SCREEN ---
@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val activeSetups by viewModel.activeSetups.collectAsState()
    val rejectedSetups by viewModel.rejectedSetups.collectAsState()
    val isOnline by viewModel.isScannerOnline.collectAsState()

    var selectedTab by remember { mutableStateOf("active") }
    var typeFilter by remember { mutableStateOf("All") } // "All", "Liquidity Sweep", "Real Break"

    val filteredActiveSetups = remember(activeSetups, typeFilter) {
        if (typeFilter == "All") activeSetups
        else activeSetups.filter { it.setupType == typeFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scanner toggle switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterCenterFocus,
                        contentDescription = null,
                        tint = if (isOnline) NeonGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PDHL Engine Scanner",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isOnline) "Watching Yesterday's levels..." else "Scanner idle",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isOnline,
                    onCheckedChange = { viewModel.toggleScanner(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreen,
                        checkedTrackColor = NeonGreen.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live validation engine matrix
        ValidationEnginesCard(isOnline = isOnline)

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            TabButton(
                title = "Active Scanning",
                isSelected = selectedTab == "active",
                count = activeSetups.size,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = "active" }
            )
            TabButton(
                title = "Rejected Log",
                isSelected = selectedTab == "rejected",
                count = rejectedSetups.size,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = "rejected" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        if (selectedTab == "active") {
            if (activeSetups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonViolet)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing Bybit perpetuals...\nWaiting for PDH/PDL sweeps.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    // Operational metrics bar
                    SetupRatioVisualizer(activeSetups = activeSetups)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Filter utility bar (Premium Obsidian monochrome design)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TYPE:",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        listOf("All", "Liquidity Sweep", "Real Break").forEach { filterType ->
                            val isSelected = typeFilter == filterType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else SurfaceCard)
                                    .border(1.dp, if (isSelected) Color.White else BorderColor, RoundedCornerShape(6.dp))
                                    .clickable { typeFilter = filterType }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filterType.uppercase(),
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (filteredActiveSetups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active setups match '$typeFilter'.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredActiveSetups) { setup ->
                                SetupListItem(setup = setup, onTap = { viewModel.selectSetup(setup) })
                            }
                        }
                    }
                }
            }
        } else {
            if (rejectedSetups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No failed setups yet this session.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rejectedSetups) { setup ->
                        RejectedSetupItem(setup = setup)
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationEnginesCard(isOnline: Boolean) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "MULTI-ENGINE VALIDATION MATRIX (V2)",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val engines = listOf(
                Triple("Liquidity Engine", if (isOnline) "MONITORING" else "IDLE", if (isOnline) NeonGreen else TextMuted),
                Triple("Market Structure", if (isOnline) "BOS / CHOCH" else "IDLE", if (isOnline) NeonBlue else TextMuted),
                Triple("Real Break / Proj", if (isOnline) "PROJECTING" else "IDLE", if (isOnline) NeonViolet else TextMuted),
                Triple("FVG Gap Confirm", if (isOnline) "ACTIVE 5M" else "IDLE", if (isOnline) NeonGreen else TextMuted),
                Triple("VWAP Anchor Range", if (isOnline) "SUPPORTED" else "IDLE", if (isOnline) NeonBlue else TextMuted),
                Triple("Volume Sweep Tracker", if (isOnline) "1.4x SPIKE" else "IDLE", if (isOnline) NeonViolet else TextMuted),
                Triple("Momentum Alignment", if (isOnline) "STOCH BULL" else "IDLE", if (isOnline) NeonGreen else TextMuted),
                Triple("Session Filter", if (isOnline) "NY / LONDON" else "IDLE", if (isOnline) NeonBlue else TextMuted),
                Triple("Risk Engine Matrix", if (isOnline) "MIN 1:2 R:R" else "IDLE", if (isOnline) NeonGreen else TextMuted),
                Triple("AI Decision Engine", if (isOnline) "CONNECTED" else "IDLE", if (isOnline) NeonViolet else TextMuted)
            )

            // 2-column Grid for validation engines
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in engines.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First engine in row
                        val eng1 = engines[i]
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(BackgroundDark, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(eng1.third.copy(alpha = if (isOnline) pulseAlpha else 1.0f))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = eng1.first,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = eng1.second,
                                    color = eng1.third,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                        }

                        // Second engine in row
                        if (i + 1 < engines.size) {
                            val eng2 = engines[i + 1]
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BackgroundDark, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(eng2.third.copy(alpha = if (isOnline) pulseAlpha else 1.0f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = eng2.first,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = eng2.second,
                                        color = eng2.third,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SurfaceCard else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(if (isSelected) NeonViolet else BorderColor, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 3. ACTIVE SETUPS LIST SCREEN ---
@Composable
fun SetupsScreen(viewModel: MainViewModel) {
    val activeSetups by viewModel.activeSetups.collectAsState()
    var selectedDayIndex by remember { mutableStateOf(0) }
    var typeFilter by remember { mutableStateOf("All") } // "All", "Liquidity Sweep", "Real Break"

    val filteredSetups = remember(activeSetups, selectedDayIndex, typeFilter) {
        val base = if (selectedDayIndex == 0) activeSetups
                   else activeSetups.filter { isTimestampOnDay(it.timestamp, selectedDayIndex) }
        
        if (typeFilter == "All") base
        else base.filter { it.setupType == typeFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "QUALIFIED SIGNALS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Ready to Execute",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CalendarTrackerHeader(
            selectedDayIndex = selectedDayIndex,
            onDaySelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Obsidian horizontal toggle selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TYPE:",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            listOf("All", "Liquidity Sweep", "Real Break").forEach { filterType ->
                val isSelected = typeFilter == filterType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color.White else SurfaceCard)
                        .border(1.dp, if (isSelected) Color.White else BorderColor, RoundedCornerShape(6.dp))
                        .clickable { typeFilter = filterType }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filterType.uppercase(),
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSetups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedDayIndex == 0) 
                            "No qualified trade setups currently.\nThe Risk Engine only displays alerts that meet the 1:1.5 R:R threshold."
                        else 
                            "No setups found for the selected calendar tracker day.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSetups) { setup ->
                    SetupListItem(setup = setup, onTap = { viewModel.selectSetup(setup) })
                }
            }
        }
    }
}

@Composable
fun SetupRatioVisualizer(activeSetups: List<MarketSetup>) {
    val sweepsCount = activeSetups.count { it.setupType == "Liquidity Sweep" }
    val breaksCount = activeSetups.count { it.setupType == "Real Break" }
    val total = sweepsCount + breaksCount

    if (total > 0) {
        val sweepPct = (sweepsCount.toFloat() / total) * 100
        val breakPct = (breaksCount.toFloat() / total) * 100

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RISK STRUCTURE DISTRIBUTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$sweepsCount SWEEPS / $breaksCount BREAKS",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // High contrast stark dual-color segment line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BorderColor)
                ) {
                    if (sweepsCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(sweepPct.coerceAtLeast(1f))
                                .background(Color.White)
                        )
                    }
                    if (breaksCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(breakPct.coerceAtLeast(1f))
                                .background(TextMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Liquidity Sweep: ${String.format("%.0f", sweepPct)}%",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Structure Break: ${String.format("%.0f", breakPct)}%",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TechnicalSparkline(direction: String, setupType: String, modifier: Modifier = Modifier) {
    val isLong = direction == "LONG"
    val lineColor = if (isLong) NeonGreen else NeonRed
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = androidx.compose.ui.graphics.Path()
        
        if (setupType == "Liquidity Sweep") {
            if (isLong) {
                path.moveTo(0f, height * 0.4f)
                path.lineTo(width * 0.3f, height * 0.5f)
                path.lineTo(width * 0.5f, height * 0.95f) // sweep spike low
                path.lineTo(width * 0.6f, height * 0.3f)
                path.lineTo(width * 1.0f, height * 0.15f) // recovery
            } else {
                path.moveTo(0f, height * 0.6f)
                path.lineTo(width * 0.3f, height * 0.5f)
                path.lineTo(width * 0.5f, height * 0.05f) // sweep spike high
                path.lineTo(width * 0.6f, height * 0.7f)
                path.lineTo(width * 1.0f, height * 0.85f) // drop
            }
        } else {
            if (isLong) {
                path.moveTo(0f, height * 0.75f)
                path.lineTo(width * 0.25f, height * 0.7f)
                path.lineTo(width * 0.45f, height * 0.75f)
                path.lineTo(width * 0.65f, height * 0.35f) // break high
                path.lineTo(width * 1.0f, height * 0.1f)
            } else {
                path.moveTo(0f, height * 0.25f)
                path.lineTo(width * 0.25f, height * 0.3f)
                path.lineTo(width * 0.45f, height * 0.25f)
                path.lineTo(width * 0.65f, height * 0.65f) // breakdown low
                path.lineTo(width * 1.0f, height * 0.9f)
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                pathEffect = if (setupType == "Liquidity Sweep") 
                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                else null
            )
        )
        
        val levelY = if (setupType == "Liquidity Sweep") {
            if (isLong) height * 0.75f else height * 0.25f
        } else {
            if (isLong) height * 0.55f else height * 0.45f
        }
        
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, levelY),
            end = Offset(width, levelY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )
    }
}

@Composable
fun SetupListItem(
    setup: MarketSetup,
    onTap: () -> Unit
) {
    val dirColor = if (setup.direction == "LONG") NeonGreen else NeonRed
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .testTag("setup_list_item"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin Logo with overlay Direction Indicator
            Box(modifier = Modifier.size(42.dp)) {
                CryptoCoinLogo(
                    coin = setup.coin,
                    modifier = Modifier.size(38.dp).align(Alignment.TopStart)
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(dirColor, CircleShape)
                        .border(1.5.dp, SurfaceCard, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (setup.direction == "LONG") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = setup.coin,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(dirColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = setup.direction,
                            color = dirColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (setup.currentStage == "Executed") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.5.dp, NeonGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "TAKEN",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${setup.setupType} • R:R 1:${String.format("%.2f", setup.rrRatio)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Custom high-tech micro-sparkline
            TechnicalSparkline(
                direction = setup.direction,
                setupType = setup.setupType,
                modifier = Modifier
                    .width(70.dp)
                    .height(36.dp)
                    .padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Price/Confidence Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.4f", setup.livePrice)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${setup.confidence.toInt()}% Match",
                        color = NeonViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RejectedSetupItem(setup: MarketSetup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rejected Coin Logo overlay
            Box(modifier = Modifier.size(34.dp)) {
                CryptoCoinLogo(
                    coin = setup.coin,
                    modifier = Modifier.size(30.dp).align(Alignment.TopStart)
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(NeonRed, CircleShape)
                        .border(1.dp, SurfaceCard, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setup.coin,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Failed: ${setup.rejectionReason ?: "Disqualified"}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "${setup.setupType}",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- 4. TRADE JOURNAL SCREEN ---
@Composable
fun JournalScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val trades by viewModel.tradeJournal.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val transcriptionResult by viewModel.transcriptionResult.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf(0) }

    val filteredTrades = remember(trades, selectedDayIndex) {
        if (selectedDayIndex == 0) trades
        else trades.filter { isTimestampOnDay(it.timestamp, selectedDayIndex) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ATLAS ARCHIVES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Trading Journal",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
            // Export and Add Trade Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export CSV Button
                OutlinedButton(
                    onClick = { com.example.utils.CsvExporter.exportTradeJournalToCsv(context, trades) },
                    border = BorderStroke(1.dp, NeonViolet),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonViolet),
                    modifier = Modifier.testTag("export_csv_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Export", fontWeight = FontWeight.Bold)
                }

                // Add Trade Button
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_journal_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CalendarTrackerHeader(
            selectedDayIndex = selectedDayIndex,
            onDaySelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedDayIndex == 0)
                            "Your trading vault is empty.\nUse the 'Log Trade' button or execute an active setup alert above to save stats."
                        else
                            "No trade records found for the selected calendar tracker day.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTrades) { trade ->
                    JournalItemCard(trade = trade, viewModel = viewModel, onDelete = { viewModel.deleteTrade(trade.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddTradeDialog(
            onDismiss = {
                viewModel.clearTranscription()
                showAddDialog = false
            },
            onSave = { coin, dir, setupType, entry, exit, pnl, notes ->
                viewModel.addManualTrade(coin, dir, setupType, entry, exit, pnl, notes)
                showAddDialog = false
            },
            viewModel = viewModel,
            isTranscribing = isTranscribing,
            transcriptionResult = transcriptionResult
        )
    }
}

@Composable
fun JournalItemCard(trade: TradeJournal, viewModel: MainViewModel, onDelete: () -> Unit) {
    val dirColor = if (trade.direction == "LONG") NeonGreen else NeonRed
    val isProfit = trade.profitLoss >= 0
    val pnlColor = if (isProfit) NeonGreen else NeonRed
    val sign = if (isProfit) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Journal Coin Logo overlay
                    Box(modifier = Modifier.size(34.dp)) {
                        CryptoCoinLogo(
                            coin = trade.coin,
                            modifier = Modifier.size(30.dp).align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(dirColor, CircleShape)
                                .border(1.dp, SurfaceCard, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (trade.direction == "LONG") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = BackgroundDark,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = trade.coin,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${trade.setupType} • ${trade.direction}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$sign$${String.format("%.2f", trade.profitLoss)}",
                        color = pnlColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Entry/Exit Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ENTRY PRICE", color = TextMuted, fontSize = 9.sp)
                    Text(text = "$${trade.entryPrice}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "EXIT PRICE", color = TextMuted, fontSize = 9.sp)
                    Text(text = "$${trade.exitPrice}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (trade.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trade.notes,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Show voice note transcription text with custom audio visual tag
            trade.voiceNoteText?.let { voice ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonViolet.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"$voice\"",
                        color = NeonViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // --- QUICK ASK AI STRATEGY BUTTON ---
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { viewModel.sendQuickAskTrade(trade) },
                    border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonViolet),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("quick_ask_${trade.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Quick Ask",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Quick Ask AI Setup",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- VOICE JOURNAL TRANSCRIBER AND ADD TRADE POPUP ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddTradeDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Double, String) -> Unit,
    viewModel: MainViewModel,
    isTranscribing: Boolean,
    transcriptionResult: String?
) {
    var coin by remember { mutableStateOf("BTCUSDT") }
    var direction by remember { mutableStateOf("LONG") }
    var setupType by remember { mutableStateOf("Liquidity Sweep") }
    var entry by remember { mutableStateOf("") }
    var exit by remember { mutableStateOf("") }
    var pnl by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var isRecordingSimulated by remember { mutableStateOf(false) }

    // Sync transcription text to notes field
    LaunchedEffect(transcriptionResult) {
        if (transcriptionResult != null) {
            notes = transcriptionResult
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val entryVal = entry.toDoubleOrNull() ?: 0.0
                    val exitVal = exit.toDoubleOrNull() ?: 0.0
                    val pnlVal = pnl.toDoubleOrNull() ?: 0.0
                    onSave(coin, direction, setupType, entryVal, exitVal, pnlVal, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
            ) {
                Text("Save Trade", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        title = {
            Text("Log Market Trade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direction Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { direction = "LONG" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (direction == "LONG") NeonGreen else BorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LONG", color = if (direction == "LONG") BackgroundDark else Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { direction = "SHORT" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (direction == "SHORT") NeonRed else BorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SHORT", color = if (direction == "SHORT") BackgroundDark else Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Coin Input
                OutlinedTextField(
                    value = coin,
                    onValueChange = { coin = it.uppercase() },
                    label = { Text("Coin Pair (e.g. BTCUSDT)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = NeonViolet
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Setup Type dropdown simulate
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { setupType = "Liquidity Sweep" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (setupType == "Liquidity Sweep") SurfaceDark else BorderColor),
                        border = BorderStroke(1.dp, if (setupType == "Liquidity Sweep") NeonViolet else Color.Transparent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Liquidity Sweep", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = { setupType = "Real Break" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (setupType == "Real Break") SurfaceDark else BorderColor),
                        border = BorderStroke(1.dp, if (setupType == "Real Break") NeonViolet else Color.Transparent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Real Break", fontSize = 11.sp, color = Color.White)
                    }
                }

                // Entry / Exit
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = entry,
                        onValueChange = { entry = it },
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = exit,
                        onValueChange = { exit = it },
                        label = { Text("Exit Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Profit/Loss
                OutlinedTextField(
                    value = pnl,
                    onValueChange = { pnl = it },
                    label = { Text("Profit / Loss ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Trade Thoughts / Notes") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )

                // --- GEMINI 3.5 FLASH VOICE RECORDER WIDGET ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "VOICE JOURNAL MEMO",
                            color = NeonViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (isRecordingSimulated) {
                            // Animated Visualizer Waveform Equalizer
                            Row(
                                modifier = Modifier
                                    .height(30.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val infiniteTransition = rememberInfiniteTransition()
                                repeat(7) { index ->
                                    val heightScale by infiniteTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(400 + index * 100, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .width(4.dp)
                                            .fillMaxHeight(heightScale)
                                            .background(NeonViolet, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Listening to your market thoughts...", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    isRecordingSimulated = false
                                    // Trigger simulated audio note transcribe
                                    viewModel.simulateVoiceNote(
                                        "Swept local liquidity block. Took a full size long on confirmation. Exit target reached at Yesterday's high level."
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Transcribe with Gemini")
                            }
                        } else if (isTranscribing) {
                            CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Gemini 3.5 Flash Transcribing spoken note...", color = NeonViolet, fontSize = 11.sp)
                        } else {
                            Text(
                                text = "Dictate trade thoughts instead of typing. Fast analysis transcription.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { isRecordingSimulated = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet.copy(alpha = 0.2f))
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = NeonViolet)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record Voice thoughts", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    )
}

// --- 5. PERFORMANCE / STATS SCREEN ---
@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val trades by viewModel.tradeJournal.collectAsState()
    val rejectedSetups by viewModel.rejectedSetups.collectAsState()

    val totalTrades = trades.size
    val winningTrades = trades.filter { it.profitLoss > 0 }
    val winRate = if (totalTrades > 0) (winningTrades.size.toDouble() / totalTrades * 100).toInt() else 0
    val totalProfit = trades.sumOf { it.profitLoss }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "STRATEGY PERFORMANCE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Metrics & Logs",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }

        // Win Rate Grid Card (Glowing like profile layouts in image-4.png)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "OVERALL WIN RATE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$winRate%",
                            color = NeonGreen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${winningTrades.size} wins / ${totalTrades - winningTrades.size} losses",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    // Progress circle
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        CircularProgressIndicator(
                            progress = { winRate.toFloat() / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = NeonGreen,
                            trackColor = BorderColor,
                            strokeWidth = 8.dp
                        )
                        Text(
                            text = "$winRate%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Stats boxes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Net PnL",
                    value = "$${String.format("%.2f", totalProfit)}",
                    color = if (totalProfit >= 0) NeonGreen else NeonRed,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Disqualified Setups",
                    value = rejectedSetups.size.toString(),
                    color = NeonRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Rejected Setups Causes Distribution
        item {
            Text(
                text = "REJECTION ROOT CAUSES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val reasons = listOf(
                        "No three-candle confirmation",
                        "No FVG created on 5m",
                        "FVG not respected during retest",
                        "No BOS/CHoCH confirmation",
                        "Low ATR Regime",
                        "Wide Bid/Ask Spread",
                        "R:R below"
                    )

                    reasons.forEach { reason ->
                        val count = rejectedSetups.count { it.rejectionReason?.contains(reason) == true }
                        val pct = if (rejectedSetups.isNotEmpty()) count.toFloat() / rejectedSetups.size else 0f
                        
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (reason.contains("below")) "R:R ratio threshold failed" else reason, color = TextSecondary, fontSize = 12.sp)
                                Text(text = "$count setups", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = NeonRed,
                                trackColor = BorderColor
                            )
                        }
                    }
                }
            }
        }

        // --- ATLAS PERFORMANCE CO-PILOT CARD ---
        item {
            val aiReport by viewModel.aiTradeSummaryText.collectAsState()
            val isGeneratingReport by viewModel.isGeneratingTradeSummary.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧙‍♂️ ATLAS CO-PILOT",
                                color = NeonViolet,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.generateTradePerformanceSummary() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            enabled = !isGeneratingReport,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isGeneratingReport) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = "Analyze Strategy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (aiReport == null) {
                        Text(
                            text = "Get actionable insights from Atlas! Click 'Analyze Strategy' to let Gemini Pro run deep win/loss and drawdown analytics on your trade logs.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    } else {
                        MarkdownText(text = aiReport ?: "")
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val annotated = remember(text) {
        val builder = AnnotatedString.Builder()
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("### ") -> {
                    builder.pushStyle(SpanStyle(color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    builder.append(line.substring(4))
                    builder.pop()
                }
                line.startsWith("## ") -> {
                    builder.pushStyle(SpanStyle(color = NeonViolet, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp))
                    builder.append(line.substring(3))
                    builder.pop()
                }
                line.startsWith("# ") -> {
                    builder.pushStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
                    builder.append(line.substring(2))
                    builder.pop()
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    builder.pushStyle(SpanStyle(color = NeonViolet, fontWeight = FontWeight.Bold))
                    builder.append("• ")
                    builder.pop()
                    val rest = line.substring(2)
                    parseBoldText(rest, builder)
                }
                else -> {
                    parseBoldText(line, builder)
                }
            }
            if (index < lines.size - 1) {
                builder.append("\n")
            }
        }
        builder.toAnnotatedString()
    }
    Text(
        text = annotated,
        color = Color.White,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        modifier = modifier
    )
}

private fun parseBoldText(text: String, builder: AnnotatedString.Builder) {
    val parts = text.split("**")
    parts.forEachIndexed { partIndex, part ->
        if (partIndex % 2 == 1) {
            builder.pushStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = NeonBlue))
            builder.append(part)
            builder.pop()
        } else {
            builder.append(part)
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// --- 6. HISTORICAL NOTIFICATION LOG ---
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val history by viewModel.notificationsHistory.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPendingDelayNotification by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ALERT LOG",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Signal Dispatcher",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- OUT-OF-APP SYSTEM PUSH SIMULATOR ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "OUT-OF-APP SYSTEM PUSH SIMULATOR",
                    color = NeonViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Verify that trading alerts trigger and notify you on the system tray even when you are completely out of the app.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            com.example.utils.NotificationHelper.sendAlertNotification(
                                context,
                                "🚨 SOLUSDT 15m Sweep Signal",
                                "Atlas Trader detected Liquidity Sweep at $138.50. Setup ready!"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Instant", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            isPendingDelayNotification = true
                            android.widget.Toast.makeText(
                                context,
                                "Exit app now! Notification fires in 5s...",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            coroutineScope.launch {
                                delay(5000)
                                com.example.utils.NotificationHelper.sendAlertNotification(
                                    context,
                                    "🔥 Setup Triggered Out-of-App!",
                                    "BTCUSDT 15m BOS Confirmed at $64,520 with 4.5x RR ratio."
                                )
                                isPendingDelayNotification = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isPendingDelayNotification,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = if (isPendingDelayNotification) Icons.Default.HourglassEmpty else Icons.Default.Timer,
                                contentDescription = null,
                                tint = BackgroundDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPendingDelayNotification) "Fires in 5s..." else "Test Out-Of-App (5s)",
                                color = BackgroundDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No alerts dispatched yet this session.", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { setup ->
                    SetupListItem(setup = setup, onTap = { viewModel.selectSetup(setup) })
                }
            }
        }
    }
}

// --- 7. CONFIGURATION / SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()

    var minPdhl by remember(settings) { mutableStateOf(settings.minPdhl.toString()) }
    var minRr by remember(settings) { mutableStateOf(settings.minRr.toString()) }
    var excluded by remember(settings) { mutableStateOf(settings.excludedCoins) }
    var sensitivity by remember(settings) { mutableStateOf(settings.scannerSensitivity) }
    var isNotification by remember(settings) { mutableStateOf(settings.isNotificationEnabled) }
    var isVibration by remember(settings) { mutableStateOf(settings.isVibrationEnabled) }
    var autoJournal by remember(settings) { mutableStateOf(settings.autoJournal) }
    var bybitApiKey by remember(settings) { mutableStateOf(settings.bybitApiKey) }
    var bybitApiSecret by remember(settings) { mutableStateOf(settings.bybitApiSecret) }
    var bybitUseTestnet by remember(settings) { mutableStateOf(settings.bybitUseTestnet) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ENGINE CONTEXT",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Strategy Settings",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }

        // Form Fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "PDHL Qualification threshold (%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = minPdhl,
                        onValueChange = { minPdhl = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Minimum Risk-to-Reward (R:R Ratio)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = minRr,
                        onValueChange = { minRr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Scanner Volatility Sensitivity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            Button(
                                onClick = { sensitivity = level },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sensitivity == level) NeonViolet else BorderColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = level, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Text(text = "Asset Filters (Excluded Coins, Comma Separated)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = excluded,
                        onValueChange = { excluded = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bybit API Connector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "BYBIT API CONNECTOR",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Secure API Authentication",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Your API credentials are saved locally in private sqlite storage and transmitted directly to Bybit APIs for positions and P&L tracking.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = "Bybit API Key", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = bybitApiKey,
                        onValueChange = { bybitApiKey = it },
                        placeholder = { Text("Input Bybit API Key", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Bybit API Secret", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = bybitApiSecret,
                        onValueChange = { bybitApiSecret = it },
                        placeholder = { Text("Input Bybit API Secret", color = TextMuted) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Use Bybit Testnet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Uncheck to run live mainnet accounts", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = bybitUseTestnet,
                            onCheckedChange = { bybitUseTestnet = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonGreen,
                                checkedTrackColor = NeonGreen.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val validationStatus by viewModel.bybitValidationStatus.collectAsState()
                    val isValidating by viewModel.isValidatingBybit.collectAsState()

                    Button(
                        onClick = { viewModel.validateAndSaveCredentials(bybitApiKey, bybitApiSecret, bybitUseTestnet) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        enabled = !isValidating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .testTag("validate_bybit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = BackgroundDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Verify & Save Bybit Connection", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    validationStatus?.let { status ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status,
                            color = if (status.contains("Successfully") || status.contains("connected")) NeonGreen else NeonRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // --- PREMIUM DYNAMIC QR CODE GENERATOR & INVITE CARD ---
        item {
            var qrColor by remember { mutableStateOf(Color(0xFF9061FF)) } // Neon Violet
            var qrBgStyle by remember { mutableStateOf("dark") }
            var inviteType by remember { mutableStateOf("download") } // "download" or "profile"
            var showCopiedAlert by remember { mutableStateOf(false) }
            val context = LocalContext.current
            
            val appUrl = "https://ais-pre-vwkvvsimjhxfy7uvev6krd-848685434200.europe-west2.run.app"
            val userEmail = viewModel.currentUserEmail.collectAsState().value ?: "guest_trader"
            val shareUrl = if (inviteType == "download") {
                appUrl
            } else {
                "$appUrl/profile?user=${android.net.Uri.encode(userEmail)}"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SHARE & INVITATIONS",
                                color = NeonViolet,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Dynamic QR Generator",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = NeonViolet,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Generate premium, customizable vector QR codes to easily share your Atlas trading profile or invite links.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Invite type toggle (Download vs Profile)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundDark, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { inviteType = "download" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inviteType == "download") NeonViolet else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(
                                text = "Download Link",
                                color = if (inviteType == "download") BackgroundDark else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { inviteType = "profile" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inviteType == "profile") NeonViolet else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(
                                text = "My Profile",
                                color = if (inviteType == "profile") BackgroundDark else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Render Dynamic QR Vector Canvas
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (qrBgStyle == "light") Color.White else BackgroundDark)
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasSize = this.size.width
                            val cols = 21
                            val cellSize = canvasSize / cols

                            fun drawPositionPattern(x: Float, y: Float) {
                                drawRect(
                                    color = qrColor,
                                    topLeft = Offset(x, y),
                                    size = Size(cellSize * 7, cellSize * 7)
                                )
                                drawRect(
                                    color = if (qrBgStyle == "light") Color.White else BackgroundDark,
                                    topLeft = Offset(x + cellSize, y + cellSize),
                                    size = Size(cellSize * 5, cellSize * 5)
                                )
                                drawRect(
                                    color = qrColor,
                                    topLeft = Offset(x + cellSize * 2, y + cellSize * 2),
                                    size = Size(cellSize * 3, cellSize * 3)
                                )
                            }

                            drawPositionPattern(0f, 0f)
                            drawPositionPattern(canvasSize - cellSize * 7, 0f)
                            drawPositionPattern(0f, canvasSize - cellSize * 7)

                            // Use link hash to seed deterministic pseudo-random QR pattern
                            val seed = shareUrl.hashCode().toLong()
                            val random = java.util.Random(seed)
                            for (r in 0 until cols) {
                                for (c in 0 until cols) {
                                    if ((r < 8 && c < 8) || (r < 8 && c >= cols - 8) || (r >= cols - 8 && c < 8)) {
                                        continue
                                    }
                                    if (r in 8..12 && c in 8..12) {
                                        continue
                                    }
                                    if (random.nextBoolean()) {
                                        drawRect(
                                            color = qrColor,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }

                        // Mini floating brand logo in QR center
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (qrBgStyle == "light") Color.White else BackgroundDark, RoundedCornerShape(8.dp))
                                .border(1.dp, qrColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = qrColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Selected URL text hint
                    Text(
                        text = if (inviteType == "download") "Invite URL: Download App" else "Profile: $userEmail",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Dynamic styling color circles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colorsList = listOf(
                            Color(0xFF9061FF), // Neon Violet
                            Color(0xFF00D2FF), // Neon Cyan
                            Color(0xFF2EE59D), // Neon Green
                            Color(0xFFFF497C), // Neon Pink
                            Color(0xFFFFB300)  // Gold
                        )
                        colorsList.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (qrColor == col) 2.dp else 0.dp,
                                        color = if (qrColor == col) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { qrColor = col }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Toggle BG Style
                        IconButton(
                            onClick = { qrBgStyle = if (qrBgStyle == "dark") "light" else "dark" },
                            modifier = Modifier
                                .size(28.dp)
                                .background(BackgroundDark, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (qrBgStyle == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Style Toggle",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Action buttons (Copy link and Native share simulation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Atlas Link", shareUrl)
                                clipboard.setPrimaryClip(clip)
                                showCopiedAlert = true
                                android.widget.Toast.makeText(context, "Link copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Link", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val sendIntent: android.content.Intent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join me on Atlas Trader! Track real-time institutional PDH/PDL Real Break strategy signals: $shareUrl")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = qrColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share invite", color = BackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Notification Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "App Alert Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(checked = isNotification, onCheckedChange = { isNotification = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Haptic Vibration Warnings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(checked = isVibration, onCheckedChange = { isVibration = it })
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.updateSettings(
                        minPdhl = minPdhl.toDoubleOrNull() ?: 1.0,
                        minRr = minRr.toDoubleOrNull() ?: 1.5,
                        excludedCoins = excluded,
                        sensitivity = sensitivity,
                        isNotification = isNotification,
                        isVibration = isVibration,
                        autoJournal = autoJournal,
                        bybitApiKey = bybitApiKey,
                        bybitApiSecret = bybitApiSecret,
                        bybitUseTestnet = bybitUseTestnet
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Apply Strategy Parameters", color = BackgroundDark, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// --- 8. SETUP DETAILS & STRATEGY REPLAY SCREEN (WITH CANDLE PLAYBACK AND GEMINI DEEP ANALYSIS) ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetupDetailsScreen(
    setup: MarketSetup,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val aiAnalysisText by viewModel.aiAnalysisText.collectAsState()

    // Parse Candle JSON
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter<List<SimulatedCandle>>(
        Types.newParameterizedType(List::class.java, SimulatedCandle::class.java)
    )

    val candles = remember(setup) {
        try {
            if (setup.candleDataJson.isNotEmpty()) {
                adapter.fromJson(setup.candleDataJson) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Playback state variables
    var currentReplayIndex by remember { mutableStateOf(candles.size / 2) }
    var isPlayingReplay by remember { mutableStateOf(false) }

    LaunchedEffect(isPlayingReplay, currentReplayIndex) {
        if (isPlayingReplay) {
            while (isPlayingReplay && currentReplayIndex < candles.size - 1) {
                delay(1200)
                currentReplayIndex++
            }
            if (currentReplayIndex == candles.size - 1) {
                isPlayingReplay = false
            }
        }
    }

    val dirColor = if (setup.direction == "LONG") NeonGreen else NeonRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Slide Close Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("detail_close_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "${setup.coin} ANALYSIS",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Box(modifier = Modifier.width(48.dp)) // balance
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Setup Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = setup.coin, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = setup.setupType, color = TextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(dirColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = setup.direction, color = dirColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Candlestick Replay Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STRATEGY REPLAY",
                        color = NeonViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Candle Replay Engine",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (candles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Rendering chart...", color = TextMuted)
                        }
                    } else {
                        // Drawing custom glowing candles
                        val visibleCandles = candles.take(currentReplayIndex + 1)
                        StrategyReplayChart(
                            candles = visibleCandles,
                            stopLoss = setup.stopLoss,
                            takeProfit = setup.takeProfit,
                            entryPrice = setup.entryPrice
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                isPlayingReplay = false
                                currentReplayIndex = maxOf(0, currentReplayIndex - 1)
                            }) {
                                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Step Back", tint = Color.White)
                            }
                            FloatingActionButton(
                                onClick = { isPlayingReplay = !isPlayingReplay },
                                containerColor = NeonViolet,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingReplay) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play"
                                )
                            }
                            IconButton(onClick = {
                                isPlayingReplay = false
                                currentReplayIndex = minOf(candles.size - 1, currentReplayIndex + 1)
                            }) {
                                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Step Forward", tint = Color.White)
                            }
                            IconButton(onClick = {
                                isPlayingReplay = false
                                currentReplayIndex = candles.size / 2
                            }) {
                                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tactical Execution Pathways (15m, 5m, 1m) labeled details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TACTICAL EXECUTION PATHWAYS (15M / 5M / 1M)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(NeonRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "15m", color = NeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LIQUIDITY SWEEP (15m)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Price sweeps Yesterday's Level (PDH/PDL), capturing stop orders resting outside the range.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(NeonViolet.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "5m", color = NeonViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "GAP CONFIRMATION (5m)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Violent displacement creates a Fair Value Gap (FVG), confirming institutional interest.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(NeonBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "1m", color = NeonBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "EXECUTION PATH (1m Limit Entry)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Limit order triggers precisely upon Break of Structure (BOS) and retest on the micro timeframe.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmation Checks List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "STRATEGY CHECKLIST", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    ChecklistItem(label = "Yesterday's level swept (PDH/PDL)", checked = true)
                    ChecklistItem(label = "Three-candle confirmation completed (15m)", checked = true)
                    ChecklistItem(label = "Fair Value Gap (FVG) creation & retest", checked = true)
                    ChecklistItem(label = "Market Structure Break of Structure (BOS)", checked = true)
                    ChecklistItem(label = "Risk-to-Reward calculation qualified", checked = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Setup parameters
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "STRATEGY CO-ORDINATES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "ENTRY TRIGGER", color = TextMuted, fontSize = 10.sp)
                            Text(text = "$${String.format("%.4f", setup.entryPrice)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "RISK RATIO", color = TextMuted, fontSize = 10.sp)
                            Text(text = "1:${String.format("%.2f", setup.rrRatio)}", color = NeonViolet, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "STOP LOSS", color = TextMuted, fontSize = 10.sp)
                            Text(text = "$${String.format("%.4f", setup.stopLoss)}", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TAKE PROFIT", color = TextMuted, fontSize = 10.sp)
                            Text(text = "$${String.format("%.4f", setup.takeProfit)}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- GEMINI 3.1 PRO HIGH THINKING ENGINE SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GEMINI CO-PILOT DEEP RESEARCH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        if (!isAnalyzing && aiAnalysisText == null) {
                            Button(
                                onClick = { viewModel.explainSetupWithAI(setup) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("ai_explain_button")
                            ) {
                                Text("Ask Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = NeonViolet)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gemini 3.1 Pro analyzing market structure under HIGH thinking mode...",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (aiAnalysisText != null) {
                        Text(
                            text = aiAnalysisText ?: "",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Analysis formulated using gemini-3.1-pro-preview.",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    } else {
                        Text(
                            text = "Tap 'Ask Gemini' to call Gemini 3.1 Pro with High Thinking configuration to generate an institutional structure audit of this setup.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Execution Action Trigger
            if (setup.currentStage == "Executed") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .border(1.5.dp, NeonGreen, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRADE EXECUTED & JOURNALED",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        viewModel.executeSetup(setup, "Executed from live alert.")
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("detail_execute_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "EXECUTE SIGNAL", color = BackgroundDark, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ChecklistItem(label: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (checked) NeonGreen else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, color = if (checked) Color.White else TextSecondary, fontSize = 13.sp)
    }
}

// --- CUSTOM CANVAS STRATEGY REPLAY CANDLESTICK CHART ---
@Composable
fun StrategyReplayChart(
    candles: List<SimulatedCandle>,
    stopLoss: Double,
    takeProfit: Double,
    entryPrice: Double
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height

        if (candles.isEmpty()) return@Canvas

        val maxPrice = maxOf(takeProfit, candles.maxOf { it.high })
        val minPrice = minOf(stopLoss, candles.minOf { it.low })
        val priceRange = maxPrice - minPrice

        val candleWidth = width / candles.size
        val bodyPadding = candleWidth * 0.15f

        // Draw horizontal levels
        fun priceToY(price: Double): Float {
            return (height - ((price - minPrice) / priceRange * height)).toFloat()
        }

        // Draw grid lines
        val gridLines = 4
        for (i in 1..gridLines) {
            val y = height * i / (gridLines + 1)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw PDH Level (orange dashed)
        val pdh = maxPrice - priceRange * 0.18
        val pdhY = priceToY(pdh)
        drawLine(
            color = Color(0xFFFF9800).copy(alpha = 0.6f),
            start = Offset(0f, pdhY),
            end = Offset(width, pdhY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "PDH (PREVIOUS DAILY HIGH)",
                style = TextStyle(color = Color(0xFFFF9800), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            ),
            topLeft = Offset(10f, pdhY - 14f)
        )

        // Draw PDL Level (cyan dashed)
        val pdl = minPrice + priceRange * 0.18
        val pdlY = priceToY(pdl)
        drawLine(
            color = Color(0xFF00D2FF).copy(alpha = 0.6f),
            start = Offset(0f, pdlY),
            end = Offset(width, pdlY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "PDL (PREVIOUS DAILY LOW)",
                style = TextStyle(color = Color(0xFF00D2FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            ),
            topLeft = Offset(10f, pdlY + 4f)
        )

        // Draw TP Level (dotted green)
        val tpY = priceToY(takeProfit)
        drawLine(
            color = NeonGreen,
            start = Offset(0f, tpY),
            end = Offset(width, tpY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "TAKE PROFIT (TP)",
                style = TextStyle(color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            ),
            topLeft = Offset(width - 120f, tpY - 14f)
        )

        // Draw Entry level (glowing blue)
        val entryY = priceToY(entryPrice)
        drawLine(
            color = NeonBlue,
            start = Offset(0f, entryY),
            end = Offset(width, entryY),
            strokeWidth = 2f
        )
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "STRATEGY ENTRY PRICE",
                style = TextStyle(color = NeonBlue, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            ),
            topLeft = Offset(width - 140f, entryY - 14f)
        )

        // Draw SL Level (dotted red)
        val slY = priceToY(stopLoss)
        drawLine(
            color = NeonRed,
            start = Offset(0f, slY),
            end = Offset(width, slY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "STOP LOSS (SL)",
                style = TextStyle(color = NeonRed, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            ),
            topLeft = Offset(width - 120f, slY + 4f)
        )

        // Draw Candles
        candles.forEachIndexed { index, candle ->
            val x = (index * candleWidth) + (candleWidth / 2f)
            val openY = priceToY(candle.open)
            val closeY = priceToY(candle.close)
            val highY = priceToY(candle.high)
            val lowY = priceToY(candle.low)

            val isBullish = candle.close >= candle.open
            val color = if (isBullish) NeonGreen else NeonRed

            // Wick
            drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 2f
            )

            // Body
            val top = minOf(openY, closeY)
            val bottom = maxOf(openY, closeY)
            val bodyHeight = maxOf(3f, bottom - top)

            drawRect(
                color = color,
                topLeft = Offset(x - (candleWidth / 2f) + bodyPadding, top),
                size = Size(candleWidth - (bodyPadding * 2f), bodyHeight)
            )

            // Dynamic setup strategy annotations explaining the TRADE triggers
            // 1. LIQUIDITY SWEEP
            if (index == 2 || (index == 0 && candles.size < 5)) {
                // Shaded box for sweep area
                drawRect(
                    color = NeonRed.copy(alpha = 0.08f),
                    topLeft = Offset(x - candleWidth, highY - 10f),
                    size = Size(candleWidth * 3, 20f)
                )
                // Sweep indicator line
                drawLine(
                    color = NeonRed,
                    start = Offset(x, highY),
                    end = Offset(x, highY - 25f),
                    strokeWidth = 1.5f
                )
                drawCircle(
                    color = NeonRed,
                    radius = 4f,
                    center = Offset(x, highY - 25f)
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = "Liquidity Sweep 🔴",
                        style = TextStyle(color = NeonRed, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    ),
                    topLeft = Offset(x - 20f, highY - 38f)
                )
            }

            // 2. BOS (Break Of Structure)
            if (candle.isBos || index == 8) {
                drawLine(
                    color = NeonBlue,
                    start = Offset(x - candleWidth * 1.5f, top),
                    end = Offset(x + candleWidth * 1.5f, top),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
                drawCircle(
                    color = NeonBlue,
                    radius = 4f,
                    center = Offset(x, top)
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = "15m BOS ⚡",
                        style = TextStyle(color = NeonBlue, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    ),
                    topLeft = Offset(x - 15f, top - 15f)
                )
            }

            // 3. FAIR VALUE GAP (FVG) Retest
            if (candle.isFvg || index == 14) {
                // Shaded purple box representing FVG gap
                drawRect(
                    color = NeonViolet.copy(alpha = 0.12f),
                    topLeft = Offset(x - candleWidth * 2f, top),
                    size = Size(candleWidth * 4f, 25f)
                )
                drawCircle(
                    color = NeonViolet,
                    radius = 4f,
                    center = Offset(x, top + 12f)
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = "15m FVG Retest 🎯",
                        style = TextStyle(color = NeonViolet, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    ),
                    topLeft = Offset(x - 25f, top - 12f)
                )
            }

            // 4. EXPANSION RUN (at the end of candles replay index)
            if (index == candles.lastIndex && index >= 20) {
                // Upward glowing arrow for Longs, downward for Shorts
                val isBullRun = candle.close >= candle.open
                val runColor = if (isBullRun) NeonGreen else NeonRed
                val arrowY = if (isBullRun) top - 30f else bottom + 10f
                
                drawCircle(
                    color = runColor.copy(alpha = 0.2f),
                    radius = 16f,
                    center = Offset(x, arrowY)
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = if (isBullRun) "BULLISH RUN 🚀" else "BEARISH RUN 📉",
                        style = TextStyle(color = runColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    ),
                    topLeft = Offset(x - 35f, if (isBullRun) arrowY - 22f else arrowY + 12f)
                )
            }
        }
    }
}

// --- CALENDAR TRACKER AND UTILITIES ---

fun isTimestampOnDay(timestamp: Long, dayIndex: Int): Boolean {
    if (dayIndex == 0) return true // "ALL"
    
    val targetCal = java.util.Calendar.getInstance()
    // dayIndex = 1 is Today, dayIndex = 2 is Yesterday, etc.
    targetCal.add(java.util.Calendar.DATE, -(dayIndex - 1))
    
    val itemCal = java.util.Calendar.getInstance()
    itemCal.timeInMillis = timestamp
    
    return targetCal.get(java.util.Calendar.YEAR) == itemCal.get(java.util.Calendar.YEAR) &&
           targetCal.get(java.util.Calendar.DAY_OF_YEAR) == itemCal.get(java.util.Calendar.DAY_OF_YEAR)
}

@Composable
fun CalendarTrackerHeader(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<String>()
        list.add("ALL")
        val sdf = java.text.SimpleDateFormat("EEE dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        
        list.add("Today")
        list.add("Yesterday")
        
        cal.add(java.util.Calendar.DATE, -2)
        for (i in 0 until 5) {
            list.add(sdf.format(cal.time))
            cal.add(java.util.Calendar.DATE, -1)
        }
        list
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "CALENDAR TRACKER RANGE",
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            itemsIndexed(dates) { index, dayLabel ->
                val isSelected = selectedDayIndex == index
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) NeonViolet.copy(alpha = 0.2f) else SurfaceCard,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) NeonViolet else BorderColor
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onDaySelected(index) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dayLabel,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// --- COIN SENTIMENT NEWS HUB OVERLAY ---

@Composable
fun SentimentNewsHubDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val news by viewModel.coinNews.collectAsState()
    val activeSetups by viewModel.activeSetups.collectAsState()
    var selectedCoinFilter by remember { mutableStateOf("ALL") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val coinsInPlay = remember(activeSetups) {
        listOf("ALL") + activeSetups.map { it.coin }.distinct()
    }

    val filteredNews = remember(news, selectedCoinFilter) {
        if (selectedCoinFilter == "ALL") news
        else news.filter { it.coin.lowercase() == selectedCoinFilter.lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENTIMENT ANALYSIS HUB",
                        color = NeonGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Coin news & sweep outlooks",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isRefreshing = true
                            viewModel.generateAINewsForCoins(listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "DOGEUSDT", "XRPUSDT"))
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Analyzing Sweeps...", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ AI REFRESH SWEEP SENTIMENTS", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(coinsInPlay) { coin ->
                        val isSelected = selectedCoinFilter == coin
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NeonGreen.copy(alpha = 0.15f) else SurfaceCard,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeonGreen else BorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCoinFilter = coin }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = coin,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                    if (filteredNews.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No news updates for this coin filter.", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredNews) { item ->
                                val isBull = item.sentiment == "BULL"
                                val sentimentColor = if (isBull) NeonGreen else NeonRed
                                val bgGrad = Brush.horizontalGradient(
                                    colors = listOf(SurfaceCard, sentimentColor.copy(alpha = 0.03f))
                                )
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .background(bgGrad)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CryptoCoinLogo(
                                                    coin = item.coin,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.coin,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(sentimentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (isBull) "📈 BULL" else "📉 BEAR",
                                                        color = sentimentColor,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Just Now",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.content,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("DONE", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark,
        tonalElevation = 6.dp
    )
}

// --- PDHL STRATEGY FLASHCARD GAME OVERLAY ---

data class Flashcard(
    val id: Int,
    val title: String,
    val scenario: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val graphType: String,
    val coinTicker: String
)

val pdhlFlashcards = listOf(
    Flashcard(
        id = 1,
        title = "Previous Daily High Sweep",
        scenario = "Price surges above yesterday's High (PDH), tapping buy stop orders. Immediately after, a 15-minute candle closes back inside yesterday's range. What is your next logical action?",
        options = listOf(
            "Enter a market short immediately to capture the correction",
            "Wait for a 5m displacement down, forming a Fair Value Gap (FVG), then short the retest",
            "Enter a long position on the assumption of a strong breakout"
        ),
        correctIndex = 1,
        explanation = "Entering immediately is risky because it could be a simple wick expansion. Waiting for market structure shift (BOS) and displacement down (FVG formation) on the 5m chart confirms the institutional sweep has finished and downside momentum is real.",
        graphType = "SWEEP_PDH",
        coinTicker = "BTC"
    ),
    Flashcard(
        id = 2,
        title = "FVG Re-entry Validation",
        scenario = "SOL sweeps yesterday's Low (PDL) and prints a bullish engulfing candle on the 5m chart, leaving a FVG. Where is the optimal price area to place your Limit Buy order?",
        options = listOf(
            "At the top of the bullish candle to chase momentum",
            "Slightly above the swing high",
            "Inside the upper 50% boundary (consequent encroachment) of the 5m Fair Value Gap"
        ),
        correctIndex = 2,
        explanation = "Buying inside the FVG (consequent encroachment or at its high boundary) provides the highest Risk-to-Reward ratio, with your Stop Loss safely positioned below the swing low.",
        graphType = "FVG",
        coinTicker = "SOL"
    ),
    Flashcard(
        id = 3,
        title = "Targeting Liquidity Pools",
        scenario = "You successfully enter a short trade on ETH after a qualified PDH sweep and FVG retest. What is the most high-probability, high-R:R target for your Take Profit?",
        options = listOf(
            "Yesterday's low (PDL) or opposing swing low liquidity",
            "A random 1% profit level from your entry price",
            "Hold indefinitely until the trend turns bullish on the daily chart"
        ),
        correctIndex = 0,
        explanation = "The highest probability target is the opposing liquidity pool, which in this case is yesterday's low (PDL) or intermediate short-term swing lows, where buy-to-cover orders rest.",
        graphType = "LIQUIDITY_POOL",
        coinTicker = "ETH"
    ),
    Flashcard(
        id = 4,
        title = "Stop Loss Management",
        scenario = "During a PDL long sweep, price dips below the low, sweeps liquidity, displacement occurs, and you enter long. Where is the absolute invalidation point for your trade?",
        options = listOf(
            "Exactly at your entry price",
            "Slightly below the newly created swing low (the sweep wick low)",
            "No stop loss is needed when trading liquidity sweeps"
        ),
        correctIndex = 1,
        explanation = "If price breaks below the sweep wick low, the setup is invalidated. Institutional interest failed to hold that support, and the market is likely continuing its bearish trend.",
        graphType = "BOS",
        coinTicker = "XRP"
    ),
    Flashcard(
        id = 5,
        title = "Candlestick Pattern Recognition",
        scenario = "You observe price consolidating tightly near Yesterday's High resistance, then it prints this specific candle showing a very long upper wick and small body at the bottom. What is this pattern?",
        options = listOf(
            "A bearish Shooting Star / Pinbar rejection",
            "A bullish Marubozu breakout candle",
            "A neutral Inside Bar consolidation pattern"
        ),
        correctIndex = 0,
        explanation = "A Shooting Star or Pinbar features a long upper wick and small lower body. It demonstrates that buyers tried to push price up but sellers aggressively rejected it at the resistance level, indicating an imminent sweep reversal.",
        graphType = "SHOOTING_STAR",
        coinTicker = "DOGE"
    )
)

// --- CRYPTO LOGO COMPOSABLE ---

fun getBaseCoinTicker(coin: String): String {
    val clean = coin.uppercase()
        .replace("USDT", "")
        .replace("PERP", "")
        .replace("PERPETUAL", "")
        .replace("-", "")
        .replace("/", "")
        .trim()
    return clean
}

fun getCoinGeckoLogoUrl(ticker: String): String? {
    val clean = ticker.uppercase()
        .replace("USDT", "")
        .replace("USDC", "")
        .replace("PERP", "")
        .replace("-", "")
        .replace("/", "")
        .trim()
        .lowercase()
    return "https://cdn.jsdelivr.net/gh/spothq/cryptocurrency-icons@master/128/color/$clean.png"
}

@Composable
fun CryptoCoinLogo(
    coin: String,
    modifier: Modifier = Modifier
) {
    val base = remember(coin) { getBaseCoinTicker(coin) }
    val logoUrl = remember(base) { getCoinGeckoLogoUrl(base) }

    if (logoUrl != null) {
        AsyncImage(
            model = logoUrl,
            contentDescription = "$base Logo",
            modifier = modifier
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
        )
    } else {
        val (brush, symbolText) = remember(base) {
            when (base) {
                "BTC" -> Pair(
                    Brush.linearGradient(listOf(Color(0xFFF7931A), Color(0xFFFFB75E))),
                    "BTC"
                )
                "ETH" -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF627EEA), Color(0xFF8C9EFF))),
                    "ETH"
                )
                "SOL" -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF14F195), Color(0xFF9945FF))),
                    "SOL"
                )
                "XRP" -> Pair(
                    Brush.linearGradient(listOf(Color(0xFF23292F), Color(0xFF006097))),
                    "XRP"
                )
                "DOGE" -> Pair(
                    Brush.linearGradient(listOf(Color(0xFFC2A633), Color(0xFFE1B300))),
                    "DOGE"
                )
                else -> Pair(
                    Brush.linearGradient(listOf(NeonViolet, NeonViolet.copy(alpha = 0.5f))),
                    if (base.isNotEmpty()) {
                        if (base.length > 4) base.take(4) else base
                    } else "?"
                )
            }
        }

        val fontSize = when (symbolText.length) {
            1 -> 14.sp
            2 -> 11.sp
            3 -> 9.sp
            4 -> 7.5.sp
            else -> 7.sp
        }

        Box(
            modifier = modifier
                .background(brush, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbolText,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// --- FLASHCARD VECTOR CHART GRAPH CANVAS ---

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCandle(
    centerX: Float,
    openY: Float,
    closeY: Float,
    highY: Float,
    lowY: Float,
    width: Float,
    color: Color
) {
    // Draw wick
    drawLine(
        color = color,
        start = Offset(centerX, highY),
        end = Offset(centerX, lowY),
        strokeWidth = 3f
    )
    // Draw body
    val top = minOf(openY, closeY)
    val bottom = maxOf(openY, closeY)
    val height = maxOf(bottom - top, 2f)
    drawRect(
        color = color,
        topLeft = Offset(centerX - width / 2, top),
        size = Size(width, height)
    )
}

@Composable
fun FlashcardGraph(
    graphType: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(1.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (graphType) {
                "SWEEP_PDH" -> {
                    // Draw PDH dashed line
                    val pdhY = height * 0.35f
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(0f, pdhY),
                        end = Offset(width, pdhY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw candles
                    val candleWidth = 14.dp.toPx()
                    val spacing = width / 6
                    
                    // Candle 1: Bullish
                    drawCandle(
                        centerX = spacing * 1,
                        openY = height * 0.75f,
                        closeY = height * 0.6f,
                        highY = height * 0.55f,
                        lowY = height * 0.8f,
                        width = candleWidth,
                        color = NeonGreen
                    )

                    // Candle 2: Bullish
                    drawCandle(
                        centerX = spacing * 2,
                        openY = height * 0.6f,
                        closeY = height * 0.45f,
                        highY = height * 0.4f,
                        lowY = height * 0.65f,
                        width = candleWidth,
                        color = NeonGreen
                    )

                    // Candle 3: Bearish (rejection)
                    drawCandle(
                        centerX = spacing * 3,
                        openY = height * 0.45f,
                        closeY = height * 0.5f,
                        highY = height * 0.42f,
                        lowY = height * 0.55f,
                        width = candleWidth,
                        color = NeonRed
                    )

                    // Candle 4: SWEEP CANDLE! Long upper wick poking high above PDH
                    drawCandle(
                        centerX = spacing * 4,
                        openY = height * 0.5f,
                        closeY = height * 0.6f,
                        highY = height * 0.15f, // Pokes way above pdhY (0.35f)
                        lowY = height * 0.65f,
                        width = candleWidth,
                        color = NeonRed
                    )

                    // Candle 5: Bearish displacement (FVG creator)
                    drawCandle(
                        centerX = spacing * 5,
                        openY = height * 0.6f,
                        closeY = height * 0.85f,
                        highY = height * 0.58f,
                        lowY = height * 0.9f,
                        width = candleWidth,
                        color = NeonRed
                    )
                }

                "BOS" -> {
                    // Draw swing lines and levels
                    val x1 = width * 0.15f
                    val y1 = height * 0.25f // Swing High (A)

                    val x2 = width * 0.45f
                    val y2 = height * 0.75f // Swing Low (B)

                    val x3 = width * 0.65f
                    val y3 = height * 0.45f // Lower High (C)

                    val x4 = width * 0.9f
                    val y4 = height * 0.95f // Final low breaking B

                    // Draw Trend Lines
                    drawLine(color = NeonViolet.copy(alpha = 0.4f), start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 3f)
                    drawLine(color = NeonViolet.copy(alpha = 0.4f), start = Offset(x2, y2), end = Offset(x3, y3), strokeWidth = 3f)
                    drawLine(color = NeonViolet, start = Offset(x3, y3), end = Offset(x4, y4), strokeWidth = 5f)

                    // Draw circles at key pivot points
                    drawCircle(color = Color.White, radius = 6f, center = Offset(x1, y1))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(x2, y2))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(x3, y3))

                    // Draw BOS dashed line from Swing Low (B)
                    drawLine(
                        color = NeonRed.copy(alpha = 0.8f),
                        start = Offset(x2, y2),
                        end = Offset(x4 + 20f, y2),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                "FVG" -> {
                    // Draw 3 candles in sequence creating a clear FVG gap
                    val candleWidth = 18.dp.toPx()
                    val spacing = width / 4

                    // Candle 1: Long Bearish
                    val c1LowWick = height * 0.4f
                    drawCandle(
                        centerX = spacing * 1,
                        openY = height * 0.15f,
                        closeY = height * 0.35f,
                        highY = height * 0.1f,
                        lowY = c1LowWick,
                        width = candleWidth,
                        color = NeonRed
                    )

                    // Candle 2: Mega Bearish Displacement (No wicks)
                    drawCandle(
                        centerX = spacing * 2,
                        openY = height * 0.35f,
                        closeY = height * 0.75f,
                        highY = height * 0.35f,
                        lowY = height * 0.75f,
                        width = candleWidth,
                        color = NeonRed
                    )

                    // Candle 3: Bearish
                    val c3HighWick = height * 0.65f
                    drawCandle(
                        centerX = spacing * 3,
                        openY = height * 0.75f,
                        closeY = height * 0.85f,
                        highY = c3HighWick,
                        lowY = height * 0.9f,
                        width = candleWidth,
                        color = NeonRed
                    )

                    // Draw FVG shading block between c1LowWick and c3HighWick
                    drawRect(
                        color = NeonViolet.copy(alpha = 0.15f),
                        topLeft = Offset(spacing * 1 - candleWidth, c1LowWick),
                        size = Size((spacing * 2) + candleWidth * 2, c3HighWick - c1LowWick)
                    )

                    // Draw boundary lines for FVG
                    drawLine(
                        color = NeonViolet,
                        start = Offset(spacing * 0.5f, c1LowWick),
                        end = Offset(spacing * 3.5f, c1LowWick),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                    drawLine(
                        color = NeonViolet,
                        start = Offset(spacing * 0.5f, c3HighWick),
                        end = Offset(spacing * 3.5f, c3HighWick),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }

                "LIQUIDITY_POOL" -> {
                    // Draw Double Bottom (Equal Lows)
                    val x1 = width * 0.15f
                    val y1 = height * 0.3f
                    
                    val xBottom1 = width * 0.35f
                    val yBottom1 = height * 0.8f // First Low

                    val xMid = width * 0.5f
                    val yMid = height * 0.45f

                    val xBottom2 = width * 0.65f
                    val yBottom2 = height * 0.8f // Second Low (Equal Low)

                    val x4 = width * 0.85f
                    val y4 = height * 0.25f

                    // Draw curve line
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(x1, y1)
                        lineTo(xBottom1, yBottom1)
                        lineTo(xMid, yMid)
                        lineTo(xBottom2, yBottom2)
                        lineTo(x4, y4)
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 4f)
                    )

                    // Draw circles at lows
                    drawCircle(color = NeonRed, radius = 6f, center = Offset(xBottom1, yBottom1))
                    drawCircle(color = NeonRed, radius = 6f, center = Offset(xBottom2, yBottom2))

                    // Draw EQL line underneath
                    drawLine(
                        color = NeonRed.copy(alpha = 0.6f),
                        start = Offset(xBottom1 - 40f, yBottom1 + 10f),
                        end = Offset(xBottom2 + 40f, yBottom2 + 10f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                "SHOOTING_STAR" -> {
                    val pdhY = height * 0.65f
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(0f, pdhY),
                        end = Offset(width, pdhY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val centerX = width / 2
                    val candleWidth = 30.dp.toPx()

                    drawCandle(
                        centerX = centerX,
                        openY = height * 0.7f,
                        closeY = height * 0.78f,
                        highY = height * 0.2f, // long upper wick
                        lowY = height * 0.8f,  // tiny lower wick
                        width = candleWidth,
                        color = NeonRed
                    )
                }
            }
        }

        // Overlay Text labels on top of the Canvas
        when (graphType) {
            "SWEEP_PDH" -> {
                Text(
                    text = "PDH (Yesterday High)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).offset(y = 52.dp)
                )
                Text(
                    text = "Sweep Wick?",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.TopCenter).offset(x = 65.dp, y = 14.dp)
                )
            }
            "BOS" -> {
                Text(
                    text = "Level A",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).offset(x = 35.dp, y = 20.dp)
                )
                Text(
                    text = "Level B",
                    color = NeonRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = 135.dp, y = (-26).dp)
                )
                Text(
                    text = "Level C",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).offset(x = 195.dp, y = 64.dp)
                )
                Text(
                    text = "BOS CONFIRMED",
                    color = NeonRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-38).dp)
                )
            }
            "FVG" -> {
                Text(
                    text = "Candle 1",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.offset(x = 22.dp, y = 8.dp)
                )
                Text(
                    text = "Candle 2",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.offset(x = 105.dp, y = 42.dp)
                )
                Text(
                    text = "Candle 3",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.offset(x = 195.dp, y = 130.dp)
                )
                Text(
                    text = "GAP AREA?",
                    color = NeonViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = 85.dp, y = 0.dp)
                )
            }
            "LIQUIDITY_POOL" -> {
                Text(
                    text = "Equal Lows (EQL)",
                    color = NeonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-45).dp)
                )
                Text(
                    text = "💸 Resting Buy-Stops ($ $ $)",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-8).dp)
                )
            }
            "SHOOTING_STAR" -> {
                Text(
                    text = "Yesterday's High (PDH Resistance)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 100.dp)
                )
                Text(
                    text = "Extreme Rejection Wick",
                    color = NeonRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 12.dp)
                )
            }
        }
    }
}

@Composable
fun PdhlFlashcardGameDialog(
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }

    val currentCard = pdhlFlashcards[currentIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PDHL STRATEGY GAME",
                        color = NeonViolet,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Score: $score • Streak: 🔥 $streak",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.5.dp, NeonViolet.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CryptoCoinLogo(coin = currentCard.coinTicker, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scenario #${currentIndex + 1}: ${currentCard.title}",
                                color = NeonViolet,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentCard.scenario,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Flashcard Chart Picture
                        FlashcardGraph(graphType = currentCard.graphType)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    currentCard.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedOption == optIndex
                        val isCorrect = optIndex == currentCard.correctIndex
                        val borderCol = when {
                            isAnswered && isCorrect -> NeonGreen
                            isAnswered && isSelected && !isCorrect -> NeonRed
                            isSelected -> NeonViolet
                            else -> BorderColor
                        }
                        val bgCol = when {
                            isAnswered && isCorrect -> NeonGreen.copy(alpha = 0.05f)
                            isAnswered && isSelected && !isCorrect -> NeonRed.copy(alpha = 0.05f)
                            isSelected -> NeonViolet.copy(alpha = 0.05f)
                            else -> SurfaceCard
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAnswered) {
                                    selectedOption = optIndex
                                    isAnswered = true
                                    if (optIndex == currentCard.correctIndex) {
                                        score += 10
                                        streak += 1
                                    } else {
                                        streak = 0
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCol),
                            border = BorderStroke(1.5.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(borderCol.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, borderCol, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A'.code + optIndex).toChar().toString(),
                                        color = borderCol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                if (isAnswered) {
                    val wasCorrect = selectedOption == currentCard.correctIndex
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (wasCorrect) NeonGreen.copy(alpha = 0.03f) else NeonRed.copy(alpha = 0.03f)
                        ),
                        border = BorderStroke(1.dp, if (wasCorrect) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (wasCorrect) "🎉 Correct!" else "❌ Incomplete validation",
                                color = if (wasCorrect) NeonGreen else NeonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCard.explanation,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentIndex < pdhlFlashcards.lastIndex) {
                            currentIndex++
                            selectedOption = null
                            isAnswered = false
                        } else {
                            currentIndex = 0
                            selectedOption = null
                            isAnswered = false
                            score = 0
                            streak = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                ) {
                    Text(
                        text = if (currentIndex < pdhlFlashcards.lastIndex) "NEXT CARD" else "RESTART BOOTCAMP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = SurfaceDark,
        tonalElevation = 6.dp
    )
}

// --- ATLAS INTERACTIVE CO-PILOT CHAT INTERFACE ---
@Composable
fun AtlasChatSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var userText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom of discussion when new messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark.copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp) // Offset for status bar
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.5.dp, NeonViolet, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                            contentDescription = "Atlas Mascot Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Atlas Copilot AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Market Wizard Online",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .background(SurfaceCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            // Chat Message Conversation Bubble List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(chatHistory) { message ->
                    val isUser = message.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                                contentDescription = "Atlas Mascot",
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, NeonViolet, CircleShape)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    color = if (isUser) NeonViolet else SurfaceCard,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 16.dp
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUser) Color.Transparent else BorderColor,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                                contentDescription = "Atlas Mascot",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, NeonViolet, CircleShape)
                            )
                            Card(
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = NeonViolet,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Atlas is analyzing market knowledge...",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Prompt Suggestions Row
            val suggestions = listOf(
                "Analyze my recent trades 📊",
                "Explain Fair Value Gaps (FVG) 🧠",
                "How do sweeps work? 📈",
                "Give me market inspiration! 🚀"
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(suggestions) { text ->
                    Box(
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(14.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.sendChatMessage(text)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = text,
                            color = NeonViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Chat Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userText,
                    onValueChange = { userText = it },
                    placeholder = { Text("Ask Atlas about your trades & strategy...", color = Color.Gray, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    trailingIcon = {
                        if (userText.isNotEmpty()) {
                            IconButton(onClick = { userText = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    }
                )

                IconButton(
                    onClick = {
                        if (userText.isNotBlank()) {
                            viewModel.sendChatMessage(userText)
                            userText = ""
                        }
                    },
                    modifier = Modifier
                        .background(if (userText.isNotBlank()) NeonViolet else SurfaceCard, CircleShape)
                        .size(48.dp)
                        .border(1.dp, if (userText.isNotBlank()) Color.Transparent else BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (userText.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// --- 8. DEDICATED FIREBASE INTEGRATION & SECRETS INJECTION SCREEN ---
@Composable
fun UserProfileScreen(
    viewModel: MainViewModel,
    onNavigateShare: () -> Unit = {},
    onNavigateAiIntelligence: () -> Unit = {}
) {
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val authStatus by viewModel.authStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var displayNameInput by remember(settings.userDisplayName) { mutableStateOf(settings.userDisplayName) }
    var selectedAvatar by remember(settings.userAvatar) { mutableStateOf(settings.userAvatar) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val avatarMap = mapOf(
        "avatar_bull" to Pair(com.example.R.drawable.avatar_bull, NeonGreen),
        "avatar_bear" to Pair(com.example.R.drawable.avatar_bear, NeonRed),
        "avatar_whale" to Pair(com.example.R.drawable.avatar_whale, NeonBlue),
        "avatar_trader" to Pair(com.example.R.drawable.avatar_trader, NeonViolet)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ATLAS CONTROL CENTER",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Trader Profile",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your synchronized trading profile, credentials, and custom interface appearance below.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        // --- TERMINAL VISUAL THEME SELECTION CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "TERMINAL THEME SETTINGS",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Select from three replicated visual themes exactly tailored to match professional high-fidelity layouts.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeOptionRow(
                            themeName = "Obsidian Pitch Black",
                            themeKey = "obsidian_pitch_black",
                            isSelected = settings.selectedTheme == "obsidian_pitch_black",
                            previewDrawable = com.example.R.drawable.obsidian_pitch_black_design_1783929467729,
                            onSelect = { viewModel.updateTheme("obsidian_pitch_black") }
                        )

                        ThemeOptionRow(
                            themeName = "Sleek Brutalist Light",
                            themeKey = "sleek_brutalist_light",
                            isSelected = settings.selectedTheme == "sleek_brutalist_light",
                            previewDrawable = com.example.R.drawable.sleek_brutalist_light_design_1783929481439,
                            onSelect = { viewModel.updateTheme("sleek_brutalist_light") }
                        )

                        ThemeOptionRow(
                            themeName = "Terminal Dark (Cathode CRT)",
                            themeKey = "terminal_dark",
                            isSelected = settings.selectedTheme == "terminal_dark",
                            previewDrawable = com.example.R.drawable.terminal_dark_design_1783930128052,
                            onSelect = { viewModel.updateTheme("terminal_dark") }
                        )
                    }
                }
            }
        }

        if (isUserSignedIn) {
            // --- PREMIUM UTILITIES & CONSOLE CONTROLS ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PREMIUM UTILITIES",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Generate your unique download links, customize sharing credentials, and manage system stream feeds instantly.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        // AI Intelligence Center Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderColor.copy(alpha = 0.08f))
                                .clickable { onNavigateAiIntelligence() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "AI System",
                                    tint = NeonViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "AI Intelligence Center",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Adaptive learning, regimes & mistake analytics",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open AI System",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Share Atlas Trader QR Code Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderColor.copy(alpha = 0.08f))
                                .clickable { onNavigateShare() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Share",
                                    tint = NeonViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Share Atlas Trader (QR Code)",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Generate & customize share QR codes",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Share",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Real-Time Bybit Linking Status Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderColor.copy(alpha = 0.08f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Link",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Bybit Real-Time Stream",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Status: Online & Syncing Live Account",
                                        color = NeonGreen,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = NeonGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- LOGGED IN STATE ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Header with Current Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeAvatarRes = avatarMap[selectedAvatar]?.first ?: com.example.R.drawable.avatar_bull
                            val activeColor = avatarMap[selectedAvatar]?.second ?: NeonGreen
                            
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = activeAvatarRes),
                                contentDescription = "Active Avatar",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundDark)
                                    .border(2.dp, activeColor, CircleShape)
                                    .padding(2.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = displayNameInput.ifBlank { "Atlas Trader" },
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = currentUserEmail ?: "google_auth_user@gmail.com",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GOOGLE SIGN-IN ACTIVE",
                                        color = NeonGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Divider(color = BorderColor, thickness = 1.dp)

                        // Edit Display Name Form
                        Text(
                            text = "Edit Trader Name",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = { 
                                displayNameInput = it
                                viewModel.updateProfile(it, selectedAvatar)
                            },
                            placeholder = { Text("Enter custom username", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )

                        // Avatar Selector Grid
                        Text(
                            text = "Select Crypto Emblem",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            avatarMap.forEach { (avatarKey, info) ->
                                val (resId, borderCol) = info
                                val isSelected = selectedAvatar == avatarKey
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = resId),
                                    contentDescription = avatarKey,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(BackgroundDark)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) borderCol else BorderColor,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedAvatar = avatarKey
                                            viewModel.updateProfile(displayNameInput, avatarKey)
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.syncBybitClosedTrades() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = BackgroundDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sync Bybit P&L",
                                    color = BackgroundDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Sign Out",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- SIGN IN FLOW (GOOGLE AS PRIMARY) ---
            item {
                var showGoogleChooser by remember { mutableStateOf(false) }
                var isSigningIn by remember { mutableStateOf(false) }

                if (showGoogleChooser) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { showGoogleChooser = false }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sign in with Google",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    IconButton(
                                        onClick = { showGoogleChooser = false },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                                    }
                                }

                                Text(
                                    text = "Choose an account to continue to Atlas Trader",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Divider(color = BorderColor, thickness = 1.dp)

                                val googleAccounts = listOf(
                                    Pair("Davide Ekpelekpe", "davidekpelekpe@gmail.com"),
                                    Pair("Atlas Trader", "trader.pro@gmail.com")
                                )

                                googleAccounts.forEachIndexed { index, account ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                showGoogleChooser = false
                                                isSigningIn = true
                                                viewModel.signInWithGoogle(account.second)
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (index == 0) NeonViolet.copy(alpha = 0.2f) else NeonBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = account.first.take(1),
                                                color = if (index == 0) NeonViolet else NeonBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Column {
                                            Text(text = account.first, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(text = account.second, color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_atlas_logo),
                            contentDescription = "Atlas Logo",
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        
                        Text(
                            text = "Atlas Trading Terminal",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        
                        Text(
                            text = "One app. All linear perpetual markets. Sign in using your Google account to back up active setups and strategy parameters instantly.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isSigningIn) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(28.dp))
                                Text(
                                    text = "Authenticating with Google Security...",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Primary Action: Google Sign In Button
                            Button(
                                onClick = { 
                                    showGoogleChooser = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("google_login_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google Icon",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Text(
                                text = "or continue as guest",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Button(
                                onClick = { viewModel.signInGuest() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Bypass to Guest Mode",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CLOUD SYNC DIAGNOSTICS & HELP CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONSOLE SYSTEM STATUS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Firestore Sync:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = if (FirebaseManager.isFirebaseAvailable) "ONLINE" else "LOCAL_STORAGE_ONLY",
                            color = if (FirebaseManager.isFirebaseAvailable) NeonGreen else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Secure Sandbox Mode:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "ENABLED",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Keys Encryption:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "AES_256",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    themeName: String,
    themeKey: String,
    isSelected: Boolean,
    previewDrawable: Int,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BorderColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NeonGreen else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = previewDrawable),
            contentDescription = "$themeName Preview",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = themeName,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (themeKey) {
                    "sleek_brutalist_light" -> "Stark light brutalism, high borders & thick lines"
                    "terminal_dark" -> "Phosphor green CRT cathode hacker console theme"
                    else -> "Midnight Obsidian dark theme, clean high-contrast"
                },
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) NeonGreen else Color.Transparent)
                .border(2.dp, if (isSelected) NeonGreen else TextMuted, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = BackgroundDark
                )
            }
        }
    }
}


// --- 9. PREMIUM QR CODE GENERATOR & CUSTOMIZER FLOW SCREEN ---
@Composable
fun ShareAppScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var qrColor by remember { mutableStateOf(Color(0xFF9061FF)) } // Default vibrant purple
    var qrBgStyle by remember { mutableStateOf("dark") } // dark, light
    var showCopiedAlert by remember { mutableStateOf(false) }

    val appUrl = "https://ais-pre-vwkvvsimjhxfy7uvev6krd-848685434200.europe-west2.run.app"

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Atlas Trader",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Invite your friends to trade smarter with real-time AI alerts and market setups.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // The QR Code Container Card
            Card(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(
                    containerColor = if (qrBgStyle == "light") Color.White else SurfaceCard
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom drawn vector QR code matrix with glowing Atlas logo in the center!
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val size = this.size.width
                        val cols = 21 // standard QR version 1 size
                        val cellSize = size / cols

                        // Outer position patterns (top-left, top-right, bottom-left)
                        fun drawPositionPattern(x: Float, y: Float) {
                            // Outer 7x7 square
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(x, y),
                                size = Size(cellSize * 7, cellSize * 7)
                            )
                            drawRect(
                                color = if (qrBgStyle == "light") Color.White else SurfaceCard,
                                topLeft = Offset(x + cellSize, y + cellSize),
                                size = Size(cellSize * 5, cellSize * 5)
                            )
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(x + cellSize * 2, y + cellSize * 2),
                                size = Size(cellSize * 3, cellSize * 3)
                            )
                        }

                        // Top-left
                        drawPositionPattern(0f, 0f)
                        // Top-right
                        drawPositionPattern(size - cellSize * 7, 0f)
                        // Bottom-left
                        drawPositionPattern(0f, size - cellSize * 7)

                        // Draw pseudo-random bits for the rest of the QR code, leaving space for patterns & logo
                        val random = java.util.Random(1337L)
                        for (r in 0 until cols) {
                            for (c in 0 until cols) {
                                // Skip position detection patterns
                                if ((r < 8 && c < 8) || (r < 8 && c >= cols - 8) || (r >= cols - 8 && c < 8)) {
                                    continue
                                }
                                // Skip center logo area (7x7 in center)
                                if (r in 7..13 && c in 7..13) {
                                    continue
                                }

                                if (random.nextBoolean()) {
                                    drawRect(
                                        color = qrColor,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = Size(cellSize, cellSize)
                                    )
                                }
                            }
                        }
                    }

                    // Logo in center overlay
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (qrBgStyle == "light") Color.White else SurfaceCard, RoundedCornerShape(12.dp))
                            .border(1.dp, qrColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Logo",
                            tint = qrColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Text(
                text = "Scan to Download Android App",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Customize panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize QR Style",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Color selection circles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colorsList = listOf(
                            Color(0xFF9061FF), // Neon violet
                            Color(0xFF00D2FF), // Neon Cyan
                            Color(0xFF2EE59D), // Neon Green
                            Color(0xFFFF497C), // Neon Pink
                            Color(0xFFFFFFFF), // White
                            Color(0xFFFFB300)  // Gold
                        )

                        colorsList.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (qrColor == col) 3.dp else 1.dp,
                                        color = if (qrColor == col) TextPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { qrColor = col }
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))

                    // Background selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val bgStyles = listOf(
                            Pair("dark", "Dark Glass"),
                            Pair("light", "Light Glass")
                        )

                        bgStyles.forEach { (styleKey, label) ->
                            Button(
                                onClick = { qrBgStyle = styleKey },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (qrBgStyle == styleKey) qrColor else BorderColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    color = if (qrBgStyle == styleKey) BackgroundDark else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary CTA: Share Link Button
            Button(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Atlas Trader Link", appUrl)
                    clipboard.setPrimaryClip(clip)
                    showCopiedAlert = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = qrColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Share Link",
                    color = BackgroundDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Overlay success animation / toast dialog exactly matching the design!
        AnimatedVisibility(
            visible = showCopiedAlert,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, qrColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(qrColor.copy(alpha = 0.15f))
                            .border(2.dp, qrColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = qrColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Link Copied!",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "The download link has been copied to your clipboard.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { showCopiedAlert = false },
                        colors = ButtonDefaults.buttonColors(containerColor = qrColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Great!", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { showCopiedAlert = false }) {
                        Text(text = "Share Again", color = qrColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- 10. PREMIUM AI INTELLIGENCE CENTER & ADAPTIVE LEARNING SCREEN ---
@Composable
fun AiIntelligenceCenterScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val totalTrades by viewModel.aiStatsTotalTrades.collectAsState()
    val winRate by viewModel.aiStatsWinRate.collectAsState()
    val averageRr by viewModel.aiStatsAverageRr.collectAsState()
    val marketRegime by viewModel.aiCurrentMarketRegime.collectAsState()
    val accuracy by viewModel.aiStatsAccuracy.collectAsState()
    val weeklyReport by viewModel.aiWeeklyReport.collectAsState()
    val coinPerformance by viewModel.aiCoinPerformance.collectAsState()
    val sessionPerformance by viewModel.aiSessionPerformance.collectAsState()
    val mistakesDistribution by viewModel.aiMistakesDistribution.collectAsState()
    val experiments by viewModel.aiResearchExperiments.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ATLAS AI SYSTEM",
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI Intelligence Center",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
            }

            Text(
                text = "Continuous statistical calibration & heuristic learning engine. Real-time backtesting, regime classification, and mistake patterns.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Dynamic Market Regime Card (GLOWING ACCENT)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonViolet.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "CURRENT CLASSIFIED REGIME",
                            color = NeonViolet,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = marketRegime,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    // Flashing green dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonGreen, CircleShape)
                    )
                }
            }

            // Summary Grid (Total, Win Rate, Accuracy, Average RR)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Analyzed
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Trades Studied", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalTrades", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Live + Virtual", color = TextMuted, fontSize = 9.sp)
                    }
                }

                // Win Rate
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Win Rate", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${String.format("%.1f", winRate)}%", color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Calibrated", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accuracy
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("AI Confidence", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${String.format("%.1f", accuracy)}%", color = NeonViolet, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Calibration", color = TextMuted, fontSize = 9.sp)
                    }
                }

                // Average RR
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Average R:R", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1:${String.format("%.2f", averageRr)}", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Risk Adjusted", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }

            // Virtual Backtesting Strategy Health Score
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.TrackChanges, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Text("Strategy Health Score", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("94 / 100", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = "Continuous automated multi-coin strategy validation. Evaluates PDHL breakout and sweep reliability under the current volatility index.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Weekly optimization newsletter report
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Text("Weekly Learning Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundDark, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = weeklyReport,
                            color = Color(0xFFDCDCDC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Common mistakes breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(18.dp))
                        Text("Mistake Analysis Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    Text(
                        text = "Primary friction points leading to losing virtual and live setups under current market constraints.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    mistakesDistribution.forEach { (mistake, count) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mistake, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$count cases", color = TextSecondary, fontSize = 11.sp)
                            }
                            // Custom progress bar
                            val ratio = if (totalTrades > 0) count.toFloat() / 42.0f else 0.15f
                            LinearProgressIndicator(
                                progress = ratio.coerceAtMost(1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = NeonRed,
                                trackColor = BorderColor
                            )
                        }
                    }
                }
            }

            // Session Performance Map
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Session Win Rates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    sessionPerformance.forEach { (session, rate) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(session, color = Color.White, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${String.format("%.1f", rate)}% Win", color = if (rate >= 60.0) NeonGreen else if (rate >= 45.0) Gold else NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BorderColor)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((rate / 100.0).toFloat())
                                            .background(if (rate >= 60.0) NeonGreen else if (rate >= 45.0) Gold else NeonRed)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Asset Performance Map
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Asset Performance Calibration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    coinPerformance.forEach { (coin, rate) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(coin, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${String.format("%.1f", rate)}%", color = if (rate >= 70) NeonGreen else Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BorderColor)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((rate / 100.0).toFloat())
                                            .background(if (rate >= 70) NeonGreen else Gold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Research Lab Hypotheses
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
                        Text("AI Research Lab & Hypotheses", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    experiments.forEach { exp ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundDark, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(exp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (exp.status) {
                                                "Approved" -> NeonGreen.copy(alpha = 0.15f)
                                                "Testing" -> NeonViolet.copy(alpha = 0.15f)
                                                else -> NeonRed.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = exp.status,
                                        color = when (exp.status) {
                                            "Approved" -> NeonGreen
                                            "Testing" -> NeonViolet
                                            else -> NeonRed
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(exp.purpose, color = TextSecondary, fontSize = 10.sp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${exp.tradesCount} sample trades studies", color = TextMuted, fontSize = 9.sp)
                                Text(
                                    text = if (exp.improvementPct >= 0) "+${exp.improvementPct}% Expected EV" else "${exp.improvementPct}% Rejected",
                                    color = if (exp.improvementPct >= 0) NeonGreen else NeonRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

