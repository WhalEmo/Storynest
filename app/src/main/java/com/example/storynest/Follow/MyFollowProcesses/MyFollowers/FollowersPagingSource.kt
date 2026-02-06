package com.example.storynest.Follow.MyFollowProcesses.MyFollowers

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storynest.Follow.ResponseDTO.FollowUserResponseDTO

class FollowersPagingSource(
    private val apiController: FollowersApiController
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
            val response = apiController.getUserFollowed(page, params.loadSize)
            val loadSize = params.loadSize
            val pageSize = 20



            // !isSuccess
            val data = response.body().orEmpty()

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