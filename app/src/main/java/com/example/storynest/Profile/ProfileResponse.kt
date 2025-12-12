package com.example.storynest.Profile

data class ProfileResponse(
    val userResponseDto: UserResponseDto,
    val followedCount: Int,
    val followerCount: Int,
    val myPost: MyPost,
    val postCount: Int,
    val following: Boolean,
    val follower: Boolean,
    val ownProfile: Boolean
)

data class UserResponseDto(
    val id: Int,
    val username: String,
    val email: String,
    val name: String,
    val surname: String,
    val profile: String?,  // null olabilir
    val biography: String,
    val date: String
)

data class MyPost(
    val content: List<Any>,
    val pageable: Pageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: Sort,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean,
    val empty: Boolean,
    val unsorted: Boolean
)
