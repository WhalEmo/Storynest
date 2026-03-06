package com.example.storynest.Posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity.RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE post_id = :postId")
    suspend fun remoteKeysPostId(postId: Long): RemoteKeysEntity.RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}