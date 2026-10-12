package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.db.PostDao
import ru.netology.nework.db.toDto
import ru.netology.nework.db.toEntity
import ru.netology.nework.dto.CreatePostRequest
import ru.netology.nework.dto.PostDto
import ru.netology.nework.error.AppError
import ru.netology.nework.error.AppException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao
) {

    @OptIn(ExperimentalPagingApi::class)
    fun pagingPosts(): Flow<PagingData<PostDto>> =
        Pager(
            config = PagingConfig(
                pageSize = 12,
                prefetchDistance = 4,
                enablePlaceholders = false
            ),
            remoteMediator = PostRemoteMediator(apiService, postDao),
            pagingSourceFactory = { postDao.pagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDto() }
        }


    suspend fun getPostById(id: Long): Result<PostDto> = try {
        val response = apiService.getPostById(id)
        if (response.isSuccessful) {
            val body = response.body() ?: return Result.failure(AppException(AppError.NotFound))
            Result.success(body)
        } else {
            Result.failure(AppException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AppException(AppError.Network))
    } catch (e: Exception) {
        Result.failure(AppException(AppError.Unknown))
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
                    ?: return Result.failure(AppException(AppError.Unknown))
            Result.success(body)
        } else {
            Result.failure(AppException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AppException(AppError.Network))
    } catch (e: Exception) {
        Result.failure(AppException(AppError.Unknown))
    }

    suspend fun createPost(request: CreatePostRequest): Result<PostDto> = try {
        val response = apiService.savePost(request)
        if (response.isSuccessful) {
            val body =
                response.body()
                    ?: return Result.failure(AppException(AppError.Unknown))
            Result.success(body)
        } else {
            Result.failure(AppException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AppException(AppError.Network))
    } catch (e: Exception) {
        Result.failure(AppException(AppError.Unknown))
    }

    // для оптимистичных лайков/анлайков
    suspend fun updateDb(post: PostDto) = postDao.upsert(post.toEntity())

    suspend fun deleteFromDb(id: Long) = postDao.deleteById(id)

    suspend fun deletePost(id: Long): Result<Unit> = try {
        val response = apiService.deletePostById(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(AppException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(AppException(AppError.Network))
    } catch (e: Exception) {
        Result.failure(AppException(AppError.Unknown))
    }

    private fun errorMessage(code: Int) = when (code) {
//        401, 403 -> "Необходимо авторизоваться"
//        404 -> "Посты не найдены"
//        else -> "Ошибка сервера: $code"
        401, 403 -> AppError.Unauthorized
        404 -> AppError.NotFound
        else -> AppError.Server(code)
    }
}

//class PostException(message: String) : Exception(message)