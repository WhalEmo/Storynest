package com.example.storynest.HomePage.viewModelHpHelper

import com.example.storynest.HomePage.postResponse
import com.example.storynest.HomePage.postUiItem
import com.example.storynest.Posts.PostEntity
import com.example.storynest.R

object PostMapper {
    fun PostEntity.toUiItem(): postUiItem{
        return postUiItem(
            postId = post_id,
            userId = user.id,
            userName = user.username,
            profileUrl = user.profile,
            postName = postName,
            contents = contents,
            categories = categories,
            coverImage = coverImage,
            numberof_likes = PostFormatter.formatLike(numberof_likes),
            postDate = PostFormatter.formatPostDate(postDate),
            likeIconRes = if (liked)
                R.drawable.baseline_favorite_24
            else
                R.drawable.baseline_favorite_border_24,
        )
    }
    fun postResponse.toEntity(index: Int): PostEntity {
        return PostEntity(
            post_id = this.post_id,
            user = this.user,
            postName = this.postName,
            contents = this.contents,
            categories = this.categories,
            coverImage = this.coverImage,
            numberof_likes = this.numberof_likes,
            postDate = this.postDate,
            liked = this.liked,
            pinnedCount = this.pinnedCount,
            orderIndex = index
        )
    }
}