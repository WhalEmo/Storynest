package com.example.storynest.Comments

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource
import androidx.paging.PagingState

class CommentPagingSource(
    private val apiCall: suspend (page: Int, size: Int) -> List<commentResponse>
) : PagingSource<Int, commentResponse>() {

    override fun getRefreshKey(state: PagingState<Int, commentResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, commentResponse> {
        val position = params.key ?: 0
        return try {
            val response = apiCall(position, params.loadSize)
            LoadResult.Page(
                data = response,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
