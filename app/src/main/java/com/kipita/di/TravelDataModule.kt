package com.kipita.di

import android.content.Context
import androidx.room.Room
import com.kipita.data.api.BtcMerchantApiService
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.kipita.data.api.CurrencyApiService
import com.kipita.data.api.ErrorReportApiService
import com.kipita.data.api.GovernmentApiService
import com.kipita.data.api.NomadApiService
import com.kipita.data.api.WeatherApiService
import com.kipita.data.error.InHouseErrorLogger
import com.kipita.data.local.KipitaDatabase
import com.kipita.data.local.ErrorLogDao
import com.kipita.data.local.MerchantDao
import com.kipita.data.local.NomadPlaceDao
import com.kipita.data.local.TravelNoticeDao
import com.kipita.data.local.DirectMessageDao
import com.kipita.data.local.TripMessageDao
import com.kipita.data.repository.AdvisoryRepository
import com.kipita.data.repository.CurrencyRepository
import com.kipita.data.repository.HealthRepository
import com.kipita.data.repository.MerchantRepository
import com.kipita.data.repository.NomadRepository
import com.kipita.data.repository.OfflineMapRepository
import com.kipita.data.repository.OfflineMessagingRepository
import com.kipita.data.repository.WeatherRepository
import com.kipita.data.repository.SafetyRepository
import com.kipita.data.repository.TripChatRepository
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
    fun provideDb(@ApplicationContext context: Context): KipitaDatabase {
        // SQLCipher AES-256 encrypted database (SOW requirement)
        val passphrase = SQLiteDatabase.getBytes("kipita_secure_v1".toCharArray())
        val factory = SupportFactory(passphrase)

        // Guard: if the existing database file is a plain (unencrypted) SQLite file
        // from a prior install, SQLCipher cannot open it and the app crashes with
        // "file is not a database". Detect this by checking the standard SQLite magic
        // header ("SQLite format 3\0") which is absent from SQLCipher-encrypted files.
        val dbFile = context.getDatabasePath("kipita.db")
        if (dbFile.exists()) {
            try {
                val magic = ByteArray(16)
                dbFile.inputStream().use { it.read(magic) }
                val sqliteMagic = "SQLite format 3\u0000".toByteArray(Charsets.UTF_8)
                if (magic.contentEquals(sqliteMagic)) {
                    dbFile.delete()   // unencrypted file — let Room create a fresh encrypted DB
                }
            } catch (_: Exception) {
                dbFile.delete()       // unreadable / corrupt — start fresh
            }
        }

        return Room.databaseBuilder(context, KipitaDatabase::class.java, "kipita.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTravelDao(db: KipitaDatabase): TravelNoticeDao = db.travelNoticeDao()

    @Provides
    fun provideMerchantDao(db: KipitaDatabase): MerchantDao = db.merchantDao()

    @Provides
    fun provideNomadPlaceDao(db: KipitaDatabase): NomadPlaceDao = db.nomadPlaceDao()

    @Provides
    fun provideTripMessageDao(db: KipitaDatabase): TripMessageDao = db.tripMessageDao()

    @Provides
    fun provideDirectMessageDao(db: KipitaDatabase): DirectMessageDao = db.directMessageDao()

    @Provides
    fun provideErrorLogDao(db: KipitaDatabase): ErrorLogDao = db.errorLogDao()

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
    fun provideMerchantRepository(service: BtcMerchantApiService, dao: MerchantDao): MerchantRepository =
        MerchantRepository(service, dao)

    @Provides
    fun provideNomadRepository(service: NomadApiService, dao: NomadPlaceDao): NomadRepository =
        NomadRepository(service, dao)

    @Provides
    fun provideCurrencyRepository(service: CurrencyApiService): CurrencyRepository = CurrencyRepository(service)

    @Provides
    fun provideWeatherRepository(service: WeatherApiService): WeatherRepository = WeatherRepository(service)

    @Provides
    fun provideTripChatRepository(dao: TripMessageDao): TripChatRepository = TripChatRepository(dao)

    @Provides
    fun provideOfflineMessagingRepository(dao: DirectMessageDao): OfflineMessagingRepository =
        OfflineMessagingRepository(dao)

    @Provides
    fun provideOfflineMapRepository(): OfflineMapRepository = OfflineMapRepository()

    @Provides
    fun provideInHouseErrorLogger(
        dao: ErrorLogDao,
        errorReportApiService: ErrorReportApiService
    ): InHouseErrorLogger = InHouseErrorLogger(dao, errorReportApiService)

    @Provides
    fun provideTravelDataEngine(
        safetyRepository: SafetyRepository,
        healthRepository: HealthRepository,
        advisoryRepository: AdvisoryRepository
    ): TravelDataEngine = TravelDataEngine(safetyRepository, healthRepository, advisoryRepository)
}
