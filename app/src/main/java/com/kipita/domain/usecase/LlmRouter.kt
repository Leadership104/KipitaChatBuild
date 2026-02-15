package com.kipita.domain.usecase

import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.ClaudeMessage
import com.kipita.data.api.ClaudeRequest
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GeminiContent
import com.kipita.data.api.GeminiPart
import com.kipita.data.api.GeminiRequest
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.OpenAiRequest
import com.kipita.domain.model.LlmPrompt
import com.kipita.domain.model.LlmProvider
import com.kipita.domain.model.LlmResult

class LlmRouter(
    private val openAi: OpenAiApiService,
    private val claude: ClaudeApiService,
    private val gemini: GeminiApiService,
    private val tokenProvider: LlmTokenProvider
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
                val response = gemini.generateContent(
                    apiKey = tokenProvider.geminiKey(),
                    request = GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt.input)))))
                )
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
                LlmResult(LlmProvider.GEMINI, text, "gemini-1.5-pro", 0.78)
            }
        }
    }

    suspend fun askAll(input: String): List<LlmResult> = listOf(
        ask(LlmPrompt(LlmProvider.OPENAI, input)),
        ask(LlmPrompt(LlmProvider.CLAUDE, input)),
        ask(LlmPrompt(LlmProvider.GEMINI, input))
    )
}

interface LlmTokenProvider {
    fun openAiKey(): String
    fun claudeKey(): String
    fun geminiKey(): String
}
