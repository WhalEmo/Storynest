package com.example.storynest.Posts

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY orderIndex ASC")
    fun getAllPosts(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE user_id = :myId ORDER BY post_id DESC")
    fun getMyPosts(myId: Long): PagingSource<Int, PostEntity>

    @Query("DELETE FROM posts WHERE post_id = :postId AND user_id = :myId")
    suspend fun deletePost(postId: Long, myId: Long)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)
}