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
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey

                    if (nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    nextKey
                }
            }

            val responseWrapper = apiCall(page, state.config.pageSize)

            when (responseWrapper) {
                is ResultWrapper.Success -> {
                    val postList = responseWrapper.data
                    val isEndOfList = postList.size < state.config.pageSize


                    database.withTransaction {
                        try {
                            if (loadType == LoadType.REFRESH) {
                                database.remoteKeysDao().clearRemoteKeys()
                                database.postDao().clearAll()
                            }

                            val startIndex = page * state.config.pageSize
                            val prevKey = if (page == 0) null else page - 1
                            val nextKey = if (isEndOfList) null else page + 1

                            val entities = ArrayList<PostEntity>(postList.size)
                            postList.forEachIndexed { index, post ->
                                entities.add(post.toEntity(index = startIndex + index))
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


                        } catch (dbEx: Exception) {
                            throw dbEx
                        }
                    }
                    MediatorResult.Success(endOfPaginationReached = isEndOfList)
                }

                is ResultWrapper.Error -> {
                    MediatorResult.Error(Exception(responseWrapper.message))
                }
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostEntity>): RemoteKeysEntity.RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { post -> database.remoteKeysDao().remoteKeysPostId(post.post_id) }
    }
}