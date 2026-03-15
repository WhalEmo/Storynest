package com.example.storynest.DependencyInjection

import android.content.Context
import androidx.room.Room
import com.example.storynest.Posts.AppDatabase
import com.example.storynest.Posts.PostDao
import com.example.storynest.Posts.RemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "storynest_db"
        )

            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePostDao(db: AppDatabase): PostDao {
        return db.postDao()
    }

    @Provides
    fun provideRemoteKeysDao(db: AppDatabase): RemoteKeysDao {
        return db.remoteKeysDao()
    }
}