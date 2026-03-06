package com.example.storynest.Posts

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storynest.HomePage.postResponse

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator (
    private val apiCall: suspend (page: Int, size: Int) -> List<postResponse>,
    private val database: AppDatabase
): RemoteMediator<Int, PostEntity>(){
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
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
               }
            }
            val response=apiCall(page,state.config.pageSize)
            val isEndOfList = response.isEmpty()
            database.withTransaction {
                if(loadType== LoadType.REFRESH){
                    database.remoteKeysDao().clearRemoteKeys()
                    database.postDao().clearAll()
                }

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1

                val entities = response.mapIndexed { index, post ->
                    PostEntity(
                        post_id = post.post_id,
                        user = post.user,
                        postName = post.postName,
                        contents = post.contents,
                        categories = post.categories,
                        coverImage = post.coverImage,
                        numberof_likes = post.numberof_likes,
                        postDate = post.postDate,
                        liked = post.liked,
                        pinnedCount = post.pinnedCount,
                        orderIndex = (page * state.config.pageSize) + index
                    )
                }
                val keys = response.map {
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
        }catch (e: Exception) {
            MediatorResult.Error(e)
        }

    }
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostEntity>): RemoteKeysEntity.RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { post -> database.remoteKeysDao().remoteKeysPostId(post.post_id) }
    }

}