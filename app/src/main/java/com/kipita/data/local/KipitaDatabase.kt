package com.kipita.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TravelNoticeEntity::class,
        MerchantEntity::class,
        NomadPlaceEntity::class,
        TripMessageEntity::class,
        DirectMessageEntity::class,
        ErrorLogEntity::class,
        TripEntity::class          // v6: full trip storage with itinerary/invite schema
    ],
    version = 6,
    exportSchema = false
    // Note: TravelDataModule uses fallbackToDestructiveMigration() — safe for dev builds.
    // Before Play Store release, replace with explicit Room migrations.
)
abstract class KipitaDatabase : RoomDatabase() {
    abstract fun travelNoticeDao(): TravelNoticeDao
    abstract fun merchantDao(): MerchantDao
    abstract fun nomadPlaceDao(): NomadPlaceDao
    abstract fun tripMessageDao(): TripMessageDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun tripDao(): TripDao
}

