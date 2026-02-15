package com.kipita.di

import android.content.Context
import androidx.room.Room
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.local.KipitaDatabase
import com.kipita.data.local.TravelNoticeDao
import com.kipita.data.repository.AdvisoryRepository
import com.kipita.data.repository.HealthRepository
import com.kipita.data.repository.SafetyRepository
import com.kipita.data.validation.DataValidationLayer
import com.kipita.data.validation.SourceVerificationLayer
import com.kipita.domain.usecase.TravelDataEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TravelDataModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): KipitaDatabase =
        Room.databaseBuilder(context, KipitaDatabase::class.java, "kipita.db").build()

    @Provides
    fun provideTravelDao(db: KipitaDatabase): TravelNoticeDao = db.travelNoticeDao()

    @Provides
    fun provideSourceVerificationLayer(): SourceVerificationLayer = SourceVerificationLayer(
        allowedDomains = setOf("cdc.gov", "who.int", "gov", "state.gov", "europa.eu")
    )

    @Provides
    fun provideValidationLayer(sourceVerificationLayer: SourceVerificationLayer): DataValidationLayer =
        DataValidationLayer(sourceVerificationLayer)

    @Provides
    fun provideSafetyRepository(
        service: GovernmentApiService,
        dao: TravelNoticeDao,
        validator: DataValidationLayer
    ): SafetyRepository = SafetyRepository(service, dao, validator)

    @Provides
    fun provideHealthRepository(
        service: GovernmentApiService,
        dao: TravelNoticeDao,
        validator: DataValidationLayer
    ): HealthRepository = HealthRepository(service, dao, validator)

    @Provides
    fun provideAdvisoryRepository(
        service: GovernmentApiService,
        dao: TravelNoticeDao,
        validator: DataValidationLayer
    ): AdvisoryRepository = AdvisoryRepository(service, dao, validator)

    @Provides
    fun provideTravelDataEngine(
        safetyRepository: SafetyRepository,
        healthRepository: HealthRepository,
        advisoryRepository: AdvisoryRepository
    ): TravelDataEngine = TravelDataEngine(safetyRepository, healthRepository, advisoryRepository)
}
