package com.kipita.di

import com.kipita.domain.usecase.AiOrchestrationUseCase
import com.kipita.domain.usecase.TravelDataEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    fun provideAiOrchestrationUseCase(engine: TravelDataEngine): AiOrchestrationUseCase =
        AiOrchestrationUseCase(engine)
}
