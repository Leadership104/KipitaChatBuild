package com.kipita.di

import android.content.Context
import com.kipita.BuildConfig
import com.kipita.ai.KipitaAIManager
import com.kipita.data.api.ClaudeApiService
import com.kipita.data.api.GeminiApiService
import com.kipita.data.api.OpenAiApiService
import com.kipita.data.local.TripDao
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.TripChatRepository
import com.kipita.domain.usecase.AiOrchestrator
import com.kipita.domain.usecase.LlmRouter
import com.kipita.domain.usecase.LlmTokenProvider
import com.kipita.domain.usecase.AiOrchestrationUseCase
import com.kipita.domain.usecase.TravelDataEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    fun provideTokenProvider(): LlmTokenProvider = object : LlmTokenProvider {
        override fun openAiKey(): String = BuildConfig.OPENAI_API_KEY
        override fun claudeKey(): String = BuildConfig.CLAUDE_API_KEY
        override fun geminiKey(): String = BuildConfig.GEMINI_API_KEY
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

    @Provides
    fun provideAiOrchestrationUseCase(engine: TravelDataEngine): AiOrchestrationUseCase =
        AiOrchestrationUseCase(engine)

    @Provides
    @Singleton
    fun provideKipitaAIManager(
        tripDao: TripDao,
        @ApplicationContext context: Context
    ): KipitaAIManager = KipitaAIManager(tripDao, context)
}
