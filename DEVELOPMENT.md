# Development Guide - Atlas Trader

## Architecture Overview

Atlas Trader follows a clean architecture pattern:

```
app/src/main/java/com/example/
├── api/                    # Network layer (Retrofit)
│   ├── TradeApiService.kt  # API endpoints
│   └── RetrofitClient.kt   # HTTP client setup
├── data/                   # Data layer
│   ├── database/           # Room database
│   │   ├── TradeDatabase.kt
│   │   └── TradeDao.kt
│   └── models/             # Data models
│       ├── Trade.kt
│       ├── ChartData.kt
│       └── AIAnalysis.kt
├── ui/                     # Presentation layer
│   ├── screens/            # Full screens
│   │   ├── HomeScreenDesigned.kt
│   │   ├── TradeAnalysisScreen.kt
│   │   └── QrCodeScreenDesigned.kt
│   ├── components/         # Reusable components
│   │   ├── CandlestickChart.kt
│   │   ├── TradeReplayPanel.kt
│   │   ├── LivePositionMonitor.kt
│   │   ├── SignalDetailsCard.kt
│   │   ├── TradeProgressionTracker.kt
│   │   ├── AIAnalysisPanel.kt
│   │   ├── PricePositionIndicator.kt
│   │   └── GlassmorphicComponents.kt
│   ├── navigation/         # Navigation setup
│   │   └── AppNavigation.kt
│   └── theme/              # Design system
│       └── Theme.kt
└── utils/                  # Utilities
    ├── QrCodeGenerator.kt
    └── ErrorHandler.kt
```

## Design System

### Color Palette
- **Primary Purple**: `#7C3AED`
- **Primary Pink**: `#EC4899`
- **Accent Cyan**: `#06B6D4`
- **Accent Orange**: `#F97316`
- **Accent Green**: `#10B981`
- **Dark Surface**: `#0A0E27`
- **Light Surface**: `#F5F5F5`

### Typography
- **Display Large**: 32sp Bold
- **Headline Large**: 20sp Bold
- **Title Large**: 16sp Bold
- **Body Large**: 16sp Regular
- **Label Large**: 12sp Medium

### Spacing
- **Extra Large**: 32dp
- **Large**: 24dp
- **Medium**: 16dp
- **Small**: 12dp
- **Extra Small**: 8dp

### Border Radius
- **Extra Large**: 50dp
- **Large**: 20dp
- **Medium**: 16dp
- **Small**: 12dp

## Component Development

### Adding a New Component

1. Create the composable function in `ui/components/`
2. Follow the naming convention: `[FeatureName]Component.kt`
3. Use the design system colors and typography
4. Add preview function for Compose preview

Example:
```kotlin
@Composable
fun MyNewComponent(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun MyNewComponentPreview() {
    AtlasTraderTheme {
        MyNewComponent(title = "Preview")
    }
}
```

### Using Glassmorphic Surfaces

```kotlin
GlassmorphicSurface(
    modifier = Modifier.fillMaxWidth(),
    alpha = 0.1f,
    cornerRadius = 20
) {
    // Your content here
}
```

## Data Flow

### Trade Data Flow
1. API request from `TradeApiService`
2. Response stored in Room database via `TradeDao`
3. ViewModel observes database changes
4. UI renders updated data

### Chart Data
1. Candlestick data from API
2. Technical levels calculated from trade model
3. Chart component renders with overlays
4. Animation progresses through replay steps

## Testing

### Unit Tests
Place tests in `app/src/test/java/com/example/`

### UI Tests
Place tests in `app/src/androidTest/java/com/example/`

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests com.example.MyTest

# Run Android instrumented tests
./gradlew connectedAndroidTest
```

## Building & Releasing

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
export KEYSTORE_PATH="/path/to/keystore.jks"
export STORE_PASSWORD="password"
export KEY_PASSWORD="password"
./gradlew assembleRelease
```

## Common Tasks

### Add a New Screen
1. Create file in `ui/screens/` named `[ScreenName]Screen.kt`
2. Create composable with `@Composable` annotation
3. Add route to `AppNavigation.kt`
4. Navigate to it from existing screen

### Add a New Data Model
1. Create file in `data/models/`
2. Use `@Entity` for Room database models
3. Use `@Json` for API response mapping

### Add API Endpoint
1. Add method to `TradeApiService`
2. Define request/response models
3. Implement in ViewModel
4. Call from UI

## Performance Tips

- Use `remember` to prevent recomposition
- Use `LaunchedEffect` for side effects
- Lazy load screens and data
- Minimize composable functions
- Use `key()` in lists

## Debugging

### Enable Debug Logging
```kotlin
// In RetrofitClient
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

### Logcat Filtering
```bash
./gradlew assembleDebug
adb logcat | grep "Atlas"
```

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Firebase](https://firebase.google.com/)
