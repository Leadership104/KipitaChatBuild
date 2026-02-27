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
        TripEntity::class,
        UserEntity::class,
        CommunityGroupEntity::class,
        GroupMemberEntity::class,
        InviteEntity::class,
        SavedLocationEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class KipitaDatabase : RoomDatabase() {
    abstract fun travelNoticeDao(): TravelNoticeDao
    abstract fun merchantDao(): MerchantDao
    abstract fun nomadPlaceDao(): NomadPlaceDao
    abstract fun tripMessageDao(): TripMessageDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun tripDao(): TripDao
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun inviteDao(): InviteDao
    abstract fun savedLocationDao(): SavedLocationDao
}

