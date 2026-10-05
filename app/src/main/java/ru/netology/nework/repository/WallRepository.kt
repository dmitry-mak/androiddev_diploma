package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.PostDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WallRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getWall(authorId: Long): Result<List<PostDto>> = try {
        val response = apiService.getUserWall(authorId)
        if (response.isSuccessful) Result.success(response.body().orEmpty())
        else Result.failure(WallException(errorMessage(response.code())))
    } catch (e: IOException) {
        Result.failure(WallException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(WallException(e.message ?: "Не удалось загрузить стену"))
    }

    suspend fun getMyWall(): Result<List<PostDto>> = try {
        val response = apiService.getMyWall()
        if (response.isSuccessful) Result.success(response.body().orEmpty())
        else Result.failure(WallException("Ошибка сети"))
    } catch (e: IOException) {
        Result.failure(WallException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(WallException(e.message ?: "Не удалось загрузить стену"))
    }

    suspend fun like(authorId: Long, post: PostDto): Result<PostDto> = try {
        val response = if (post.likedByMe) {
            apiService.unlikeWallPostById(authorId, post.id)
        } else {
            apiService.likeWallPostById(authorId, post.id)
        }
        if (response.isSuccessful) {
            val body = response.body()
                ?: return Result.failure(WallException("Пустой ответ сервера"))
            Result.success(body)
        } else {
            Result.failure(WallException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(WallException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(WallException(e.message ?: "Не удалось изменить лайк"))
    }


    suspend fun likeMyPost(post: PostDto): Result<PostDto> = try {
        val response = if (post.likedByMe) {
            apiService.unlikeMyWallPostById(post.id)
        } else {
            apiService.likeMyWallPostById(post.id)
        }
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(WallException("Пустой ответ сервера"))
            Result.success(body)
        } else {
            Result.failure(WallException(errorMessage(response.code())))
        }
    } catch (e: Exception) {
        Result.failure(WallException(e.message ?: "Не удалось изменить лайк"))
    }


    private fun errorMessage(code: Int): String = when (code) {
        401, 403 -> "Необходимо авторизоваться"
        404 -> "Пост не найден"
        else -> "Ошибка сервера: $code"
    }
}

class WallException(message: String) : Exception(message)