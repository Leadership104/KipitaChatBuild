package com.kipita.di

import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.KipitaDatabase
import com.kipita.data.local.TripDao
import com.kipita.data.repository.TripRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TripModule {

    @Provides
    fun provideTripDao(db: KipitaDatabase): TripDao = db.tripDao()

    @Provides
    @Singleton
    fun provideTripRepository(
        dao: TripDao,
        logger: InHouseErrorLogger
    ): TripRepository = TripRepository(dao, logger)
}
