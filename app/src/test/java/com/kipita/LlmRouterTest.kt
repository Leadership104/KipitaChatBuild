package com.kipita

import com.google.common.truth.Truth.assertThat
import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.ClaudeBlock
import com.kipita.data.api.ClaudeResponse
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.GeminiCandidate
import com.kipita.data.api.GeminiContent
import com.kipita.data.api.GeminiPart
import com.kipita.data.api.GeminiResponse
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.api.OpenAiResponse
import com.kipita.domain.model.LlmPrompt
import com.kipita.domain.model.LlmProvider
import com.kipita.domain.usecase.LlmRouter
import com.kipita.domain.usecase.LlmTokenProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LlmRouterTest {
    @Test
    fun `ask routes to selected provider`() = runTest {
        val openAi = mockk<OpenAiApiService>()
        val claude = mockk<ClaudeApiService>()
        val gemini = mockk<GeminiApiService>()
        val tokens = object : LlmTokenProvider {
            override fun openAiKey() = "o"
            override fun claudeKey() = "c"
            override fun geminiKey() = "g"
        }
        coEvery { openAi.createResponse(any(), any()) } returns OpenAiResponse("openai")
        coEvery { claude.createMessage(any(), any(), any()) } returns ClaudeResponse(listOf(ClaudeBlock("claude")))
        coEvery { gemini.generateContent(any(), any()) } returns GeminiResponse(
            listOf(GeminiCandidate(GeminiContent(listOf(GeminiPart("gemini")))))
        )

        val router = LlmRouter(openAi, claude, gemini, tokens)

        val res = router.ask(LlmPrompt(LlmProvider.CLAUDE, "hello"))

        assertThat(res.provider).isEqualTo(LlmProvider.CLAUDE)
        assertThat(res.content).isEqualTo("claude")
    }
}
