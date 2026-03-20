package com.example.storynest.Posts

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE isDeleted = 0 ORDER BY orderIndex ASC")
    fun getAllPosts(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE user_id = :myId ORDER BY post_id DESC")
    fun getMyPosts(myId: Long): PagingSource<Int, PostEntity>

    @Query("DELETE FROM posts WHERE post_id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("UPDATE posts SET isDeleted = 1 WHERE post_id = :postId")
    suspend fun softDeletePost(postId: Long)

    @Query("UPDATE posts SET isDeleted = 0 WHERE post_id = :postId")
    suspend fun undoSoftDelete(postId: Long)

    @Query("UPDATE posts SET liked = :isLiked, numberof_likes = :count WHERE post_id = :id")
    suspend fun updateLikeStatus(id: Long, isLiked: Boolean, count: Int)

    @Query("""
        UPDATE posts 
        SET postName = :name, contents = :content, categories = :cat, coverImage = :image, postUpdate = :update
        WHERE post_id = :postId
    """)
    suspend fun updatePostFields(
        postId: Long,
        name: String,
        content: String,
        cat: String,
        image: String?,
        update: String?
    )


    @Query("DELETE FROM posts")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)
}