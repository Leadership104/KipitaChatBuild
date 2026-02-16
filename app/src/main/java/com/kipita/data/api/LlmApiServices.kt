package com.kipita.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/responses")
    suspend fun createResponse(
        @Header("Authorization") bearer: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class OpenAiRequest(val model: String, val input: String)
data class OpenAiResponse(val outputText: String)

data class ClaudeRequest(val model: String, val max_tokens: Int, val messages: List<ClaudeMessage>)
data class ClaudeMessage(val role: String, val content: String)
data class ClaudeResponse(val content: List<ClaudeBlock>)
data class ClaudeBlock(val text: String)

data class GeminiRequest(
    val systemInstruction: GeminiContent? = null,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val tools: List<GeminiTool>? = null,
    val cachedContent: String? = null
)

data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: Map<String, Any>? = null,
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null
)

data class GeminiTool(val googleSearchRetrieval: Map<String, String>? = null)

data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)

data class GeminiResponse(val candidates: List<GeminiCandidate>)
data class GeminiCandidate(val content: GeminiContent)
