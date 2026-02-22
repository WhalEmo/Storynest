package com.example.storynest.Comments

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storynest.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow

class CommentPagingSource(
    private val postId: Long,
    private val api: ApiClient,
    private val pinnedCountState: MutableStateFlow<Long>
) : PagingSource<Int, commentResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, commentResponse> {

        val page = params.key ?: 0

        return try {

            val response = api.commentApi.commentsGet(
                postId = postId,
                page = page,
                size = params.loadSize
            )

            if (!response.isSuccessful) {
                return LoadResult.Error(Exception("API error"))
            }

            val comments = response.body() ?: emptyList()

            if (page == 0) {
                val pinned =
                    response.headers()["X-Pinned-Count"]?.toLong() ?: 0L
                pinnedCountState.value = pinned
            }

            LoadResult.Page(
                data = comments,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (comments.size < params.loadSize) null else page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, commentResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
