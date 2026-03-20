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
            rawLikeCount = numberof_likes,
            numberof_likes = PostFormatter.formatLike(numberof_likes),
            postDate = PostFormatter.formatPostDate(postDate),
            updateDate = PostFormatter.formatPostDate(postUpdate),
            likeIconRes = if (liked)
                R.drawable.baseline_favorite_24
            else
                R.drawable.baseline_favorite_border_24,
            liked = liked,
            edited = edited,
            pinnedCount = pinnedCount
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
            postUpdate = this.updateDate,
            liked = this.isLiked,
            edited = this.isEdited,
            pinnedCount = this.pinnedCount,
            orderIndex = index
        )
    }

        fun postUiItem.toEntity(orderIndex: Int = 0): PostEntity {
            return PostEntity(
                post_id = this.postId,
                user = com.example.storynest.HomePage.UserResponse(
                    id = this.userId,
                    username = this.userName,
                    profile = this.profileUrl,
                    email = "", name = "", surname = "", date = null,
                    biography = null, emailVerified = false, isFollowing = false
                ),
                postName = this.postName,
                contents = this.contents,
                categories = this.categories,
                coverImage = this.coverImage,
                numberof_likes = this.rawLikeCount,
                postDate = this.postDate,
                postUpdate = this.updateDate,
                liked = this.liked,
                edited = this.edited,
                pinnedCount = this.pinnedCount,
                orderIndex = orderIndex
            )
        }
}