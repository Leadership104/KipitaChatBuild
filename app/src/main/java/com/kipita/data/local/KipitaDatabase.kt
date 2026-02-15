package com.kipita.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TravelNoticeEntity::class], version = 1, exportSchema = false)
abstract class KipitaDatabase : RoomDatabase() {
    abstract fun travelNoticeDao(): TravelNoticeDao
}
