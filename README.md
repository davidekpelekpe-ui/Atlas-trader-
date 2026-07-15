<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Atlas Trader - Trade Analysis App

Atlas Trader is a professional trade analysis application built with Kotlin and Jetpack Compose. It provides AI-powered trading insights, local data storage, and easy app sharing via QR codes.

View your app in AI Studio: https://ai.studio/apps/1118ff5e-b214-42f2-8117-f99d74da6e01

## Features

- 📊 **Trade Analysis**: AI-powered trade analysis using Gemini API
- 💾 **Local Storage**: Room database for offline access to trades
- 🔗 **API Integration**: Retrofit + OkHttp for seamless API communication
- 📱 **QR Code Sharing**: Generate QR codes to share the app
- 🎨 **Modern UI**: Built with Jetpack Compose and Material Design 3
- 🔥 **Firebase Integration**: Optional Firebase backend for authentication and data sync
- 🛡️ **Error Handling**: Comprehensive error handling and user feedback

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (Latest version)
- Android SDK 24 (API 24) or higher
- Gemini API Key from [Google AI Studio](https://ai.google.dev/)
- Optional: Firebase project setup

## Setup & Installation

### 1. Clone the Repository
```bash
git clone https://github.com/davidekpelekpe-ui/Atlas-trader-.git
cd Atlas-trader-
```

### 2. Open in Android Studio
- Launch Android Studio
- Select **File** → **Open**
- Choose the Atlas Trader project directory
- Allow Android Studio to download and configure dependencies

### 3. Configure Environment Variables

Create a `.env` file in the project root directory with the following variables:

```env
# Required: Gemini API Key
GEMINI_API_KEY=your_gemini_api_key_here

# Optional: Firebase Configuration
FIREBASE_API_KEY="your_firebase_api_key"
FIREBASE_APP_ID="your_firebase_app_id"
FIREBASE_PROJECT_ID="your_firebase_project_id"

# API Configuration
API_BASE_URL="https://api.example.com/"
API_TIMEOUT_SECONDS=30
```

**Getting your Gemini API Key:**
1. Visit [Google AI Studio](https://ai.google.dev/)
2. Click "Get API Key"
3. Create a new API key
4. Copy it to your `.env` file

### 4. Sync Gradle

- Click **File** → **Sync Now**
- Wait for the build to complete

### 5. Run on Emulator or Device

- Click the **Run** button (▶️) or press `Shift + F10`
- Select your emulator or connected device
- The app will build and launch

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/
│   │   ├── MainActivity.kt           # App entry point
│   │   ├── api/
│   │   │   ├── TradeApiService.kt    # Retrofit API endpoints
│   │   │   └── RetrofitClient.kt     # HTTP client configuration
│   │   ├── data/
│   │   │   ├── database/
│   │   │   │   ├── TradeDatabase.kt  # Room database
│   │   │   │   └── TradeDao.kt       # Database access object
│   │   │   └── models/
│   │   │       └── Trade.kt          # Data models
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt     # Main home screen
│   │   │   │   └── QrCodeScreen.kt   # QR code sharing screen
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt  # Navigation setup
│   │   │   └── theme/
│   │   │       └── Theme.kt          # Material Design 3 theme
│   │   └── utils/
│   │       ├── QrCodeGenerator.kt    # QR code generation
│   │       └── ErrorHandler.kt       # Error handling utilities
│   └── res/
│       └── AndroidManifest.xml       # App manifest
├── build.gradle.kts                  # App-level build config
└── google-services.json              # Firebase config

build.gradle.kts                       # Root build config
settings.gradle.kts                    # Gradle settings
gradle.properties                      # Gradle properties
.env.example                           # Environment variables template
```

## Building & Deployment

### Debug Build
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
Requires setting up release signing:

```bash
# Set environment variables
export KEYSTORE_PATH="/path/to/my-upload-key.jks"
export STORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"

# Build
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

## Key Technologies

- **Kotlin**: Modern Android development language
- **Jetpack Compose**: Declarative UI framework
- **Room Database**: Local data persistence
- **Retrofit + OkHttp**: HTTP networking
- **Moshi**: JSON serialization
- **Firebase AI**: AI/ML services
- **Google ML Kit**: QR code generation
- **Coroutines**: Asynchronous programming
- **Material Design 3**: Modern design system

## Troubleshooting

### Missing `.env` File
**Error**: `Property 'GEMINI_API_KEY' not found`

**Solution**: Create `.env` file with your API keys (see Setup section above)

### Build Fails with Gradle Errors

**Solution**:
1. Click **File** → **Invalidate Caches** → **Invalidate and Restart**
2. Click **File** → **Sync Now**
3. Rebuild the project

### App Crashes on Launch

**Solution**:
1. Check that `GEMINI_API_KEY` is correctly set in `.env`
2. Ensure Android API level 24+ is being used
3. Check the logcat for detailed error messages

### Emulator Issues

**Solution**:
- Use Android Studio's emulator with API 31 or higher
- Alternatively, test on a physical device with Android 7.0+

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -am 'Add feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Submit a Pull Request

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions:
- Check existing [GitHub Issues](https://github.com/davidekpelekpe-ui/Atlas-trader-/issues)
- Review the [troubleshooting section](#troubleshooting)
- Check the [Android documentation](https://developer.android.com/docs)

---

**Built with ❤️ using Kotlin and Jetpack Compose**
