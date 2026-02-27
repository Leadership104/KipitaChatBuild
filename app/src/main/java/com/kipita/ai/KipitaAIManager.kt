package com.kipita.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.kipita.BuildConfig
import com.kipita.data.local.TripDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified AI engine for Kipita. Wraps Gemini 2.0 Flash Lite via the native SDK,
 * loads past-trip memory from Room for context, and exposes a shared [isAiTyping]
 * state that all tabs can observe.
 */
@Singleton
class KipitaAIManager @Inject constructor(
    private val tripDao: TripDao,
    @ApplicationContext private val context: Context
) {
    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    private var _sharedPreferenceContext = ""

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 2048
            },
            systemInstruction = content {
                text(
                    "You are the Kipita Personal Concierge. Your mission is to eliminate decision fatigue. " +
                    "Use past trip data as your primary filter. For UI tasks, return raw JSON. " +
                    "For Chat and Trip Planning, be professional, concise, and use history to personalize. " +
                    "NO markdown, NO filler."
                )
            }
        )
    }

    private var chatSession: Chat? = null

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun chat(userMessage: String): String = withTypingIndicator {
        if (chatSession == null) {
            val memory = buildMemoryContext()
            chatSession = model.startChat(
                history = listOf(
                    content("user") { text("My travel history:\n$memory") },
                    content("model") { text("Understood. I have your travel history loaded.") }
                )
            )
        }
        chatSession!!.sendMessage(userMessage).text ?: ""
    }

    suspend fun planTrip(destination: String, durationDays: Int = 7): String = withTypingIndicator {
        val memory = buildMemoryContext()
        val sharedPref = _sharedPreferenceContext
        val prompt = """
            Past trip memory:
            $memory
            User hint: $sharedPref
            Generate a $durationDays-day JSON itinerary for $destination.
            Return ONLY valid JSON array matching this schema:
            [{"day":1,"label":"Day Label","items":[{"time":"HH:mm","emoji":"...","title":"...","desc":"...","loc":"..."}]}]
        """.trimIndent()
        model.generateContent(prompt).text ?: "[]"
    }

    suspend fun getContextualSuggestions(hourOfDay: Int, dayOfWeek: Int): List<String> =
        withTypingIndicator {
            val prompt = "Hour=$hourOfDay, Day=$dayOfWeek (1=Mon,7=Sun). " +
                "Return ONLY a JSON array of 4 short category strings suitable for this time, e.g. [\"Coffee\",\"Breakfast\"]"
            val raw = model.generateContent(prompt).text ?: "[]"
            JSONArray(raw).let { arr -> (0 until arr.length()).map { arr.getString(it) } }
        }

    suspend fun parseNlpSearch(query: String): String = withTypingIndicator {
        val prompt = "Parse this search into JSON filters: \"$query\". " +
            "Return: {\"category\":\"...\",\"ambiance\":\"...\",\"features\":[],\"distance\":\"...\"}"
        model.generateContent(prompt).text ?: "{}"
    }

    suspend fun analyzeImage(bitmap: Bitmap): String = withTypingIndicator {
        val response = model.generateContent(
            content {
                image(bitmap)
                text(
                    "Identify what's in this image. Return JSON: " +
                    "{\"type\":\"landmark|dish|place\",\"name\":\"...\",\"searchQuery\":\"...\",\"category\":\"...\"}"
                )
            }
        )
        response.text ?: "{}"
    }

    fun updateSharedPreference(hint: String) {
        _sharedPreferenceContext = hint
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun buildMemoryContext(): String {
        val pastTrips = tripDao.getPastTrips()
        return pastTrips.joinToString("\n") { trip ->
            "Trip to ${trip.destination}: sentiment='${trip.userSentiment}', " +
            "notes='${trip.notesText.take(120)}', preferences='${trip.pastPreferences}'"
        }
    }

    private suspend fun <T> withTypingIndicator(block: suspend () -> T): T {
        _isAiTyping.value = true
        return try {
            block()
        } finally {
            _isAiTyping.value = false
        }
    }
}
