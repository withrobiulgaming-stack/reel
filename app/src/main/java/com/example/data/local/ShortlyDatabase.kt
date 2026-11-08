package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoEntity::class,
        UserEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        BookmarkEntity::class,
        FollowEntity::class,
        NotificationEntity::class,
        MessageEntity::class,
        PayoutRequestEntity::class,
        VideoInteractionEntity::class,
        WatchHistoryEntity::class,
        AdEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ShortlyDatabase : RoomDatabase() {
    abstract fun shortlyDao(): ShortlyDao

    companion object {
        @Volatile
        private var instance: ShortlyDatabase? = null

        fun getInstance(context: Context): ShortlyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShortlyDatabase::class.java,
                    "shortly_platform.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
