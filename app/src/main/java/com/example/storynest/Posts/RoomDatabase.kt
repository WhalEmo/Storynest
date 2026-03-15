package com.example.storynest.Posts

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PostEntity::class, RemoteKeysEntity.RemoteKeys::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}