package com.example.storynest.Posts

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storynest.HomePage.postResponse
import com.example.storynest.HomePage.viewModelHpHelper.PostMapper.toEntity
import com.example.storynest.ResultWrapper

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiCall: suspend (page: Int, size: Int) -> ResultWrapper<List<postResponse>>,
    private val database: AppDatabase
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        val responseWrapper = apiCall(page, state.config.pageSize)

        return when (responseWrapper) {
            is ResultWrapper.Success -> {
                val postList = responseWrapper.data
                val isEndOfList = postList.isEmpty()

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.remoteKeysDao().clearRemoteKeys()
                        database.postDao().clearAll()
                    }

                    val startIndex = page * state.config.pageSize
                    val prevKey = if (page == 0) null else page - 1
                    val nextKey = if (isEndOfList) null else page + 1

                    val entities = postList.mapIndexed { index, post ->
                        post.toEntity(index = startIndex + index)
                    }

                    val keys = postList.map {
                        RemoteKeysEntity.RemoteKeys(
                            post_id = it.post_id,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }

                    database.remoteKeysDao().insertAll(keys)
                    database.postDao().insertAll(entities)
                }
                MediatorResult.Success(endOfPaginationReached = isEndOfList)
            }

            is ResultWrapper.Error -> {
                MediatorResult.Error(Exception(responseWrapper.message))
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostEntity>): RemoteKeysEntity.RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { post -> database.remoteKeysDao().remoteKeysPostId(post.post_id) }
    }
}