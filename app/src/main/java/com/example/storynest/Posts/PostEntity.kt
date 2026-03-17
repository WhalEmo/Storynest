package com.example.storynest.Posts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.storynest.HomePage.UserResponse

@Entity(tableName = "posts",indices = [Index(value = ["user_id"])])
data class PostEntity(
    @PrimaryKey val post_id: Long,
    @Embedded(prefix = "user_")
    val user: UserResponse,
    val postName: String,
    val contents: String,
    val categories: String,
    val coverImage: String?,
    var numberof_likes: Int,
    val postDate: String,
    var liked: Boolean,
    var pinnedCount: Long,
    val orderIndex: Int,
    val isDeleted: Boolean = false,
    val localTimestamp: Long = System.currentTimeMillis()
)
