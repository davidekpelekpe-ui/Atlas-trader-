package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.api.BybitClient
import com.example.api.BybitClosedPnlResponse
import com.example.api.BybitClosedPnlResult
import com.example.api.BybitClosedPnl
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Try default automatic initialization using google-services.json generated strings first
                try {
                    FirebaseApp.initializeApp(context)
                    Log.d(TAG, "Firebase automatically initialized using google-services.json.")
                } catch (ex: Exception) {
                    Log.w(TAG, "Default Firebase initialization failed, falling back to manual config: ${ex.localizedMessage}")
                    
                    val prefs = context.getSharedPreferences("atlas_firebase_prefs", Context.MODE_PRIVATE)
                    val storedApiKey = prefs.getString("api_key", "") ?: ""
                    val storedAppId = prefs.getString("app_id", "") ?: ""
                    val storedProjectId = prefs.getString("project_id", "") ?: ""

                    val apiKey = if (storedApiKey.isNotEmpty()) storedApiKey else try { BuildConfig.FIREBASE_API_KEY } catch (e: Exception) { "" }
                    val appId = if (storedAppId.isNotEmpty()) storedAppId else try { BuildConfig.FIREBASE_APP_ID } catch (e: Exception) { "" }
                    val projectId = if (storedProjectId.isNotEmpty()) storedProjectId else try { BuildConfig.FIREBASE_PROJECT_ID } catch (e: Exception) { "" }

                    val options = FirebaseOptions.Builder()
                        .setApiKey(if (apiKey.isNullOrEmpty() || apiKey.contains("PLACEHOLDER")) "mock-api-key-for-atlas-trader" else apiKey)
                        .setApplicationId(if (appId.isNullOrEmpty() || appId.contains("PLACEHOLDER")) "1:123456789012:android:mockappiddummy" else appId)
                        .setProjectId(if (projectId.isNullOrEmpty() || projectId.contains("PLACEHOLDER")) "mock-project-id" else projectId)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.d(TAG, "Firebase manually initialized successfully as a fallback.")
                }
            } else {
                Log.d(TAG, "Firebase already initialized.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase: ${e.localizedMessage}", e)
        }
    }

    fun reinitializeWithCredentials(context: Context, apiKey: String, appId: String, projectId: String): Boolean {
        try {
            val apps = FirebaseApp.getApps(context)
            for (app in apps) {
                app.delete()
            }
            
            val prefs = context.getSharedPreferences("atlas_firebase_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("api_key", apiKey)
                .putString("app_id", appId)
                .putString("project_id", projectId)
                .apply()

            val options = FirebaseOptions.Builder()
                .setApiKey(if (apiKey.isEmpty() || apiKey.contains("PLACEHOLDER")) "mock-api-key-for-atlas-trader" else apiKey)
                .setApplicationId(if (appId.isEmpty() || appId.contains("PLACEHOLDER")) "1:123456789012:android:mockappiddummy" else appId)
                .setProjectId(if (projectId.isEmpty() || projectId.contains("PLACEHOLDER")) "mock-project-id" else projectId)
                .build()
            
            FirebaseApp.initializeApp(context, options)
            Log.d(TAG, "Firebase re-initialized with custom credentials.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to re-initialize Firebase: ${e.localizedMessage}", e)
            return false
        }
    }

    fun getCustomCredentials(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences("atlas_firebase_prefs", Context.MODE_PRIVATE)
        val apiKey = prefs.getString("api_key", "") ?: ""
        val appId = prefs.getString("app_id", "") ?: ""
        val projectId = prefs.getString("project_id", "") ?: ""
        return Triple(apiKey, appId, projectId)
    }

    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth not available: ${e.localizedMessage}")
            null
        }
    }

    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Firestore not available: ${e.localizedMessage}")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = auth != null && firestore != null

    val isFirebaseReal: Boolean
        get() {
            if (auth == null) return false
            val app = try { FirebaseApp.getInstance() } catch (e: Exception) { null } ?: return false
            val apiKey = app.options.apiKey ?: ""
            val projectId = app.options.projectId ?: ""
            return apiKey.isNotEmpty() && !apiKey.contains("PLACEHOLDER") && !apiKey.contains("mock") &&
                    projectId.isNotEmpty() && !projectId.contains("PLACEHOLDER") && !projectId.contains("mock")
        }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * Signs in with email and password.
     */
    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val authInstance = auth
        if (authInstance == null || !isFirebaseReal) {
            onResult(false, "Firebase Auth is currently unavailable on this device (demo mode fallback).")
            return
        }
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Signed in successfully!")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Sign in failed")
                }
            }
    }

    /**
     * Signs up with email and password.
     */
    fun signUpWithEmail(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val authInstance = auth
        if (authInstance == null || !isFirebaseReal) {
            onResult(false, "Firebase Auth is currently unavailable on this device.")
            return
        }
        authInstance.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Account created successfully!")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Sign up failed")
                }
            }
    }

    /**
     * Triggers anonymous sign in as a fallback to allow easy testing.
     */
    fun signInAnonymously(onResult: (Boolean, String) -> Unit) {
        val authInstance = auth
        if (authInstance == null) {
            onResult(false, "Firebase Auth is currently unavailable.")
            return
        }
        authInstance.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Signed in as Guest!")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Guest sign in failed")
                }
            }
    }

    /**
     * Signs out from the account.
     */
    fun signOut() {
        auth?.signOut()
    }

    /**
     * Saves Bybit API keys to Firestore collection `/users/{uid}/api_credentials/bybit`.
     */
    fun saveBybitCredentials(
        uid: String,
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onResult(false, "Firestore is unavailable. Saved locally only.")
            return
        }

        val data = hashMapOf(
            "apiKey" to apiKey,
            "apiSecret" to apiSecret,
            "useTestnet" to useTestnet,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("api_credentials")
            .document("bybit")
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, "Bybit credentials backed up securely to Firestore!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Failed to back up keys: ${e.localizedMessage}")
            }
    }

    /**
     * Saves user trading preferences to Firestore collection `/users/{uid}/settings/app`.
     */
    fun saveAppSettingsToFirestore(uid: String, settings: AppSettings, onResult: ((Boolean, String) -> Unit)? = null) {
        val db = firestore
        if (db == null) {
            onResult?.invoke(false, "Firestore is unavailable.")
            return
        }

        val data = hashMapOf(
            "minPdhl" to settings.minPdhl,
            "minRr" to settings.minRr,
            "excludedCoins" to settings.excludedCoins,
            "scannerSensitivity" to settings.scannerSensitivity,
            "isDarkMode" to settings.isDarkMode,
            "isNotificationEnabled" to settings.isNotificationEnabled,
            "isVibrationEnabled" to settings.isVibrationEnabled,
            "autoJournal" to settings.autoJournal,
            "bybitApiKey" to settings.bybitApiKey,
            "bybitApiSecret" to settings.bybitApiSecret,
            "bybitUseTestnet" to settings.bybitUseTestnet,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("settings")
            .document("app")
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onResult?.invoke(true, "Trading preferences backed up to Firestore successfully!")
            }
            .addOnFailureListener { e ->
                onResult?.invoke(false, "Failed to back up trading preferences: ${e.localizedMessage}")
            }
    }

    /**
     * Fetches user trading preferences from Firestore.
     */
    fun getAppSettingsFromFirestore(uid: String, onResult: (AppSettings?) -> Unit) {
        val db = firestore
        if (db == null) {
            onResult(null)
            return
        }

        db.collection("users")
            .document(uid)
            .collection("settings")
            .document("app")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    try {
                        val settings = AppSettings(
                            id = 1,
                            minPdhl = doc.getDouble("minPdhl") ?: 1.0,
                            minRr = doc.getDouble("minRr") ?: 1.5,
                            excludedCoins = doc.getString("excludedCoins") ?: "USDC,BUSD",
                            scannerSensitivity = doc.getString("scannerSensitivity") ?: "Medium",
                            isDarkMode = doc.getBoolean("isDarkMode") ?: true,
                            isNotificationEnabled = doc.getBoolean("isNotificationEnabled") ?: true,
                            isVibrationEnabled = doc.getBoolean("isVibrationEnabled") ?: true,
                            autoJournal = doc.getBoolean("autoJournal") ?: true,
                            bybitApiKey = doc.getString("bybitApiKey") ?: "",
                            bybitApiSecret = doc.getString("bybitApiSecret") ?: "",
                            bybitUseTestnet = doc.getBoolean("bybitUseTestnet") ?: true
                        )
                        onResult(settings)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse AppSettings from Firestore: ${e.localizedMessage}")
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch AppSettings from Firestore: ${e.localizedMessage}")
                onResult(null)
            }
    }

    /**
     * Fetches Bybit credentials from Firestore.
     */
    fun getBybitCredentials(
        uid: String,
        onResult: (apiKey: String, apiSecret: String, useTestnet: Boolean) -> Unit
    ) {
        val db = firestore ?: return
        db.collection("users")
            .document(uid)
            .collection("api_credentials")
            .document("bybit")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val key = doc.getString("apiKey") ?: ""
                    val secret = doc.getString("apiSecret") ?: ""
                    val testnet = doc.getBoolean("useTestnet") ?: true
                    onResult(key, secret, testnet)
                }
            }
    }

    /**
     * Persists a trade log into Firestore collection `/users/{uid}/trade_logs/{logId}`.
     */
    fun saveTradeLogToFirestore(uid: String, trade: TradeJournal) {
        val db = firestore ?: return
        val tradeData = hashMapOf(
            "id" to trade.id,
            "coin" to trade.coin,
            "direction" to trade.direction,
            "setupType" to trade.setupType,
            "entryPrice" to trade.entryPrice,
            "exitPrice" to trade.exitPrice,
            "profitLoss" to trade.profitLoss,
            "notes" to trade.notes,
            "timestamp" to trade.timestamp,
            "screenshotBase64" to trade.screenshotBase64,
            "voiceNoteText" to trade.voiceNoteText
        )

        db.collection("users")
            .document(uid)
            .collection("trade_logs")
            .document(trade.id.toString())
            .set(tradeData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Trade ${trade.id} saved to Firestore successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save trade to Firestore", e)
            }
    }

    /**
     * Syncs historical closed trades from Bybit account into Firestore `/users/{uid}/trade_logs`.
     */
    fun syncBybitHistoricalTrades(
        uid: String,
        apiKey: String,
        apiSecret: String,
        useTestnet: Boolean,
        onComplete: (List<TradeJournal>) -> Unit
    ) {
        val db = firestore ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val trades = BybitClient.fetchClosedPnL(apiKey, apiSecret, useTestnet)
            if (trades.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    trades.forEach { trade ->
                        // Save to Firestore with a unique document key
                        val docId = "bybit_" + trade.coin + "_" + trade.timestamp
                        val tradeData = hashMapOf(
                            "coin" to trade.coin,
                            "direction" to trade.direction,
                            "setupType" to "Bybit Closed P&L",
                            "entryPrice" to trade.entryPrice,
                            "exitPrice" to trade.exitPrice,
                            "profitLoss" to trade.profitLoss,
                            "notes" to trade.notes,
                            "timestamp" to trade.timestamp,
                            "voiceNoteText" to ""
                        )
                        db.collection("users")
                            .document(uid)
                            .collection("trade_logs")
                            .document(docId)
                            .set(tradeData, SetOptions.merge())
                    }
                    onComplete(trades)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onComplete(emptyList())
                }
            }
        }
    }

    /**
     * Logs an application exception to Firestore under collection `/app_errors`.
     */
    fun logErrorToFirestore(email: String?, exceptionName: String, message: String, stackTrace: String) {
        val db = firestore ?: return
        val errorData = hashMapOf(
            "email" to (email ?: "anonymous@atlastrader.com"),
            "timestamp" to System.currentTimeMillis(),
            "exceptionName" to exceptionName,
            "message" to message,
            "stackTrace" to stackTrace,
            "device" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "osVersion" to android.os.Build.VERSION.RELEASE
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("app_errors")
                    .add(errorData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Exception successfully logged to Firestore.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to log exception to Firestore", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log exception to Firestore: ${e.localizedMessage}")
            }
        }
    }
}
