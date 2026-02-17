package com.example.storynest.Follow.Paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storynest.Follow.FollowRepository
import com.example.storynest.Follow.FollowType
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO

class FollowPagingSource(
    private val repository: FollowRepository,
    private val followType: FollowType,
    private val userId: Long?
): PagingSource<Int, FollowUserResponseDTO>() {


    override fun getRefreshKey(state: PagingState<Int, FollowUserResponseDTO>): Int? {
        return state.anchorPosition?.let {position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FollowUserResponseDTO> {
        return try {
            val page = params.key ?: 0

            val data = when(followType) {
                FollowType.MY_FOLLOWERS -> {
                    repository.getFollowers(page, params.loadSize)
                }
                FollowType.MY_FOLLOWING -> {
                    repository.getFollowing(page, params.loadSize)
                }
                FollowType.USER_FOLLOWING ->{
                    repository.getOtherUserFollowing(userId!!, page, params.loadSize)
                }
                FollowType.USER_FOLLOWERS -> {
                    repository.getOtherUserFollowers(userId!!, page, params.loadSize)
                }
            }

            val loadSize = params.loadSize
            val pageSize = 20


            val pageLoaded = loadSize / pageSize
            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - pageLoaded,
                nextKey = if (data.isEmpty()) null else page + pageLoaded
            )

        }
        catch (e: Exception){
            LoadResult.Error(e)
        }
    }

}