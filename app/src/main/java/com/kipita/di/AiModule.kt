package com.kipita.di

import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.TripChatRepository
import com.kipita.domain.usecase.AiOrchestrator
import com.kipita.domain.usecase.LlmRouter
import com.kipita.domain.usecase.LlmTokenProvider
import com.kipita.domain.usecase.TravelDataEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    fun provideTokenProvider(): LlmTokenProvider = object : LlmTokenProvider {
        override fun openAiKey(): String = "OPENAI_API_KEY"
        override fun claudeKey(): String = "CLAUDE_API_KEY"
        override fun geminiKey(): String = "GEMINI_API_KEY"
    }

    @Provides
    fun provideLlmRouter(
        openAiApiService: OpenAiApiService,
        claudeApiService: ClaudeApiService,
        geminiApiService: GeminiApiService,
        tokenProvider: LlmTokenProvider
    ): LlmRouter = LlmRouter(openAiApiService, claudeApiService, geminiApiService, tokenProvider)

    @Provides
    fun provideAiOrchestrator(
        travelDataEngine: TravelDataEngine,
        llmRouter: LlmRouter,
        merchantRepository: MerchantRepository,
        nomadRepository: NomadRepository,
        tripChatRepository: TripChatRepository
    ): AiOrchestrator = AiOrchestrator(
        travelDataEngine,
        llmRouter,
        merchantRepository,
        nomadRepository,
        tripChatRepository
    )
}
