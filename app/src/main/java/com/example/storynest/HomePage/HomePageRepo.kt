package com.example.storynest.HomePage

import com.example.storynest.ErrorType
import com.example.storynest.ResultWrapper
import com.example.storynest.parseErrorBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class HomePageRepo(
    private val api: HPController
) {
    suspend fun addPost(request: postRequest): ResultWrapper<postResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.addPost(request).execute()
            }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            } else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)
                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.WRONG_REGISTER
                )
            }
        } catch (e: Exception) {
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }

    suspend fun getUserPosts(
        userId: Long,
        page: Int,
        size: Int
    ): ResultWrapper<List<postResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getUserPosts(userId, page, size).execute()
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            } else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)
                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.WRONG_REGISTER
                )
            }
        } catch (e: Exception) {
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }

    suspend fun toggleLike(
        postId: Long
    ): ResultWrapper<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.toggleLike(postId).execute()
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ResultWrapper.Success(body)
                } else {
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            } else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)

                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.SERVER_ERROR
                )
            }
        } catch (e: Exception) {
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }
    suspend fun getUsersWhoLike(
        postId: Long,
        page: Int = 0,
        size: Int = 10
    ):ResultWrapper<List<UserResponse>>{
        return try{
            val response = withContext(Dispatchers.IO) {
                api.getUsersWhoLike(postId, page, size).execute()
            }
            if(response.isSuccessful){
                val body=response.body()
                if(body!=null){
                    ResultWrapper.Success(body)
                }else{
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            }else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)

                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.SERVER_ERROR
                )
            }
        }catch (e: Exception){
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }
    suspend fun HomePagePosts(
        page: Int = 0,
        size: Int = 10
    ): ResultWrapper<List<postResponse>>{
        return try {
            val response = withContext(Dispatchers.IO) {
                api.HomePagePosts( page ,size).execute()
            }
            if(response.isSuccessful){
                val body=response.body()
                if(body!=null){
                    ResultWrapper.Success(body)
                }else{
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            }else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)

                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.SERVER_ERROR
                )
            }

        }catch (e: Exception){
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }
    suspend fun deletePost(
        postId: Long
    ): ResultWrapper<String>{
        return try {
            val response = withContext(Dispatchers.IO) {
                api.DeletePost(postId).execute()
            }
            if(response.isSuccessful){
                val body=response.body()
                if(body!=null){
                    ResultWrapper.Success(body)
                }else{
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            }else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)

                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.SERVER_ERROR
                )
            }

        }catch (e: Exception){
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }
    suspend fun updatePost(
        postId: Long,
        request: PostUpdateRequest
    ): ResultWrapper<String>{
        return try {
            val response = withContext(Dispatchers.IO) {
                api.updatePost(postId,request).execute()
            }
            if(response.isSuccessful){
                val body=response.body()
                if(body!=null){
                    ResultWrapper.Success(body)
                }else{
                    ResultWrapper.Error(
                        message = "Sunucu boş yanıt döndürdü",
                        type = ErrorType.EMPTY_RESPONSE
                    )
                }
            }else {
                val errorString = response.errorBody()?.string() ?: ""
                val errorMessage = parseErrorBody(errorString)

                ResultWrapper.Error(
                    message = errorMessage,
                    type = ErrorType.SERVER_ERROR
                )
            }
        }catch (e: Exception){
            ResultWrapper.Error(
                message = "Sunucuya bağlanılamadı: ${e.message}",
                type = ErrorType.SERVER_ERROR
            )
        }
    }
}