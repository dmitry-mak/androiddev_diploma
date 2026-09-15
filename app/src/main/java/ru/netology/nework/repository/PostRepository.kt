package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.CreatePostRequest
import ru.netology.nework.dto.PostDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPosts(): Result<List<PostDto>> = try {
        val response = apiService.getPosts()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty())
        } else {
            Result.failure(PostException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(PostException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(PostException(e.message ?: "Не удалось загрузить посты"))
    }

    suspend fun getPostById(id: Long): Result<PostDto> = try {
        val response = apiService.getPostById(id)
        if (response.isSuccessful) {
            val body = response.body() ?: return Result.failure(PostException("Пост не найден"))
            Result.success(body)
        } else {
            Result.failure(PostException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(PostException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(PostException(e.message ?: "Не удалось загрузить пост"))
    }

    suspend fun like(post: PostDto): Result<PostDto> = try {
        val response = if (post.likedByMe) {
            apiService.unlikePostById(post.id)
        } else {
            apiService.likePostById(post.id)
        }

        if (response.isSuccessful) {
            val body =
                response.body()
                    ?: return Result.failure(PostException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(PostException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(PostException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(PostException(e.message ?: "Не удалось изменить лайк"))
    }

    suspend fun createPost(request: CreatePostRequest): Result<PostDto> = try {
        val response = apiService.savePost(request)
        if (response.isSuccessful) {
            val body =
                response.body()
                    ?: return Result.failure(PostException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(PostException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(PostException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(PostException(e.message ?: "Не удалось сохранить пост"))
    }

    suspend fun deletePost(id: Long): Result<Unit> = try {
        val response = apiService.deletePostById(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(PostException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(PostException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(PostException(e.message ?: "Не удалось удалить пост"))
    }

    private fun errorMessage(code: Int): String = when (code) {
        401, 403 -> "Необходимо авторизоваться"
        404 -> "Посты не найдены"
        else -> "Ошибка сервера: $code"
    }
}

class PostException(message: String) : Exception(message)