package com.example.storynest

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

class GenericPagingSource<T : Any>(
    private val apiCall: suspend (page: Int, size: Int) -> List<T>
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val position = params.key ?: 0
        return try {
            val response = apiCall(position, params.loadSize)
            LoadResult.Page(
                data = response,
                prevKey = if (position == 0) null else position - 1,
                nextKey = if (response.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            val customException = when (e) {
                is UnknownHostException -> AppError.NetworkError()
                is ConnectException -> AppError.NgrokError()
                is HttpException -> {
                    if (e.code() == 502 || e.code() == 404) AppError.NgrokError()
                    else AppError.ServerError(e.code(), "Sunucu Hatası")
                }
                else -> AppError.UnknownError(e.localizedMessage ?: "Hata")
            }
            LoadResult.Error(customException)
        }
    }

}