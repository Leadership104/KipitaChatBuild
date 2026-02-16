package com.kipita.domain.usecase

import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.ClaudeMessage
import com.kipita.data.api.ClaudeRequest
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GeminiContent
import com.kipita.data.api.GeminiGenerationConfig
import com.kipita.data.api.GeminiPart
import com.kipita.data.api.GeminiRequest
import com.kipita.data.api.GeminiTool
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.OpenAiRequest
import com.kipita.domain.model.LlmPrompt
import com.kipita.domain.model.LlmProvider
import com.kipita.domain.model.LlmResult
import java.time.Instant

private const val GEMINI_FLASH_LITE_MODEL = "gemini-2.5-flash-lite"
private const val KIPITA_SYSTEM_INSTRUCTION =
    "We are the Kipita Discovery Concierge, a helpful and savvy local expert for an app that connects users to Restaurants, Entertainment, and Shopping. " +
        "Our Goal: Help users find exactly what they need based on their current context (time, location, and mood). " +
        "Our Tone: Professional yet friendly and enthusiastic. Use short, concise sentences to save on tokens and ensure fast reading on mobile screens. " +
        "Rules: 1) Extract intent from vague prompts like date night. 2) Prioritize actionable, bulleted recommendations. " +
        "3) Respect budget/constraints like cheap or dog-friendly. 4) No hallucinations beyond Restaurants, Entertainment, Shopping. 5) Be concise."

class LlmRouter(
    private val openAi: OpenAiApiService,
    private val claude: ClaudeApiService,
    private val gemini: GeminiApiService,
    private val tokenProvider: LlmTokenProvider,
    private val geminiUsageLimiter: GeminiUsageLimiter = GeminiUsageLimiter(),
    private val geminiContextCache: GeminiContextCache = GeminiContextCache()
) {
    suspend fun ask(prompt: LlmPrompt): LlmResult {
        return when (prompt.provider) {
            LlmProvider.OPENAI -> {
                val response = openAi.createResponse(
                    bearer = "Bearer ${tokenProvider.openAiKey()}",
                    request = OpenAiRequest(model = "gpt-4o-mini", input = prompt.input)
                )
                LlmResult(LlmProvider.OPENAI, response.outputText, "gpt-4o-mini", 0.82)
            }

            LlmProvider.CLAUDE -> {
                val response = claude.createMessage(
                    apiKey = tokenProvider.claudeKey(),
                    version = "2023-06-01",
                    request = ClaudeRequest(
                        model = "claude-3-5-sonnet-latest",
                        max_tokens = 512,
                        messages = listOf(ClaudeMessage("user", prompt.input))
                    )
                )
                LlmResult(
                    LlmProvider.CLAUDE,
                    response.content.firstOrNull()?.text.orEmpty(),
                    "claude-3-5-sonnet-latest",
                    0.8
                )
            }

            LlmProvider.GEMINI -> {
                geminiUsageLimiter.onRequest(Instant.now())
                geminiContextCache.get(prompt.input)?.let { cached ->
                    return LlmResult(LlmProvider.GEMINI, cached, GEMINI_FLASH_LITE_MODEL, 0.8)
                }

                val generationConfig = if (prompt.structured) {
                    GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        responseSchema = mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "recommendations" to mapOf("type" to "array"),
                                "reasoning" to mapOf("type" to "string")
                            )
                        ),
                        temperature = 0.2,
                        maxOutputTokens = 700
                    )
                } else {
                    GeminiGenerationConfig(temperature = 0.3, maxOutputTokens = 500)
                }

                val response = gemini.generateContent(
                    apiKey = tokenProvider.geminiKey(),
                    request = GeminiRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(KIPITA_SYSTEM_INSTRUCTION))),
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt.input)))),
                        generationConfig = generationConfig,
                        tools = listOf(GeminiTool(googleSearchRetrieval = mapOf("mode" to "grounded"))),
                        cachedContent = geminiContextCache.key(prompt.input)
                    )
                )
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
                geminiContextCache.put(prompt.input, text)
                LlmResult(LlmProvider.GEMINI, text, GEMINI_FLASH_LITE_MODEL, 0.78)
            }
        }
    }

    suspend fun askAll(input: String): List<LlmResult> = listOf(
        ask(LlmPrompt(LlmProvider.OPENAI, input)),
        ask(LlmPrompt(LlmProvider.CLAUDE, input)),
        ask(LlmPrompt(LlmProvider.GEMINI, input))
    )
}

class GeminiUsageLimiter(
    private val rpmLimit: Int = 30,
    private val rpdLimit: Int = 1500
) {
    private var dayKey: String = ""
    private var dayCount: Int = 0
    private val minuteBucket = ArrayDeque<Long>()

    fun onRequest(now: Instant) {
        val day = now.toString().take(10)
        if (day != dayKey) {
            dayKey = day
            dayCount = 0
            minuteBucket.clear()
        }

        val minuteAgo = now.minusSeconds(60).toEpochMilli()
        while (minuteBucket.isNotEmpty() && minuteBucket.first() < minuteAgo) minuteBucket.removeFirst()
        check(minuteBucket.size < rpmLimit) { "Gemini free-tier RPM limit reached" }
        check(dayCount < rpdLimit) { "Gemini free-tier RPD limit reached" }

        minuteBucket.addLast(now.toEpochMilli())
        dayCount += 1
    }
}

class GeminiContextCache {
    private val map = mutableMapOf<String, String>()
    fun key(prompt: String): String = "kipita/${prompt.hashCode()}"
    fun put(prompt: String, value: String) {
        map[key(prompt)] = value
    }

    fun get(prompt: String): String? = map[key(prompt)]
}

interface LlmTokenProvider {
    fun openAiKey(): String
    fun claudeKey(): String
    fun geminiKey(): String
}
