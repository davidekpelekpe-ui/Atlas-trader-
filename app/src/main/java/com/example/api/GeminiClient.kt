package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import android.util.Log

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String // Base64 encoded data
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String // "HIGH", "LOW"
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Double? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    /**
     * Executes advanced analysis with gemini-3.1-pro-preview and HIGH thinking level.
     */
    suspend fun getAdvancedAnalysis(prompt: String, systemInstruction: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Demo mode active: Configure your GEMINI_API_KEY in the Secrets panel for real-time market analyses."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                // For gemini-3.1-pro-preview, we enable HIGH thinking
                thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
            ),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
        )

        return try {
            val response = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Analyst is currently revieweing market structure. Please try again."
        } catch (e: Exception) {
            Log.e("GeminiClient", "Analysis failed", e)
            "Error performing analysis: ${e.localizedMessage}"
        }
    }

    /**
     * Transcribes audio data using gemini-3.5-flash.
     */
    suspend fun transcribeAudio(base64Audio: String, mimeType: String = "audio/mp3"): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Demo Note: Set your GEMINI_API_KEY to enable automated speech transcription."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(
                Part(text = "Transcribe the spoken audio in this file. Provide only the text transcription, do not add conversational notes."),
                Part(inlineData = InlineData(mimeType = mimeType, data = base64Audio))
            )))
        )

        return try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No voice content detected."
        } catch (e: Exception) {
            Log.e("GeminiClient", "Transcription failed", e)
            "Error transcribing audio: ${e.localizedMessage}"
        }
    }
}
