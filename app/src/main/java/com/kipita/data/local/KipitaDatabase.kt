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
        ErrorLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class KipitaDatabase : RoomDatabase() {
    abstract fun travelNoticeDao(): TravelNoticeDao
    abstract fun merchantDao(): MerchantDao
    abstract fun nomadPlaceDao(): NomadPlaceDao
    abstract fun tripMessageDao(): TripMessageDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun errorLogDao(): ErrorLogDao
}
