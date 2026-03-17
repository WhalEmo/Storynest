package com.example.storynest.Posts

import androidx.room.Entity
import androidx.room.PrimaryKey

class RemoteKeysEntity {
    @Entity(tableName = "remote_keys")
    data class RemoteKeys(
        @PrimaryKey val post_id: Long,
        val prevKey: Int?,
        val nextKey: Int?
    )
}