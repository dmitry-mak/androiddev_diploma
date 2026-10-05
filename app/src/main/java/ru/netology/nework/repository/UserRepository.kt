package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.JobDto
import ru.netology.nework.dto.UserDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getUsers(): Result<List<UserDto>> = try {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty())
        } else {
            Result.failure(UserException("Ошибка сервера: ${response.code()}"))
        }
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось загрузить пользователей"))
    }


    suspend fun getUserById(id: Long): Result<UserDto> = try {
        val response = apiService.getUserById(id)
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(UserException("Пользователь не найден"))
            Result.success(body)
        } else {
            Result.failure(UserException("Ошибка сервера: ${response.code()}"))
        }
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось загрузить пользователя"))
    }


    suspend fun getJobs(userId: Long): Result<List<JobDto>> = try {
        val response = apiService.getUserJobs(userId)
        if (response.isSuccessful) Result.success(response.body().orEmpty())
        else Result.failure(UserException("Ошибка сервера: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось загрузить список работ"))
    }

    suspend fun getMyJobs(): Result<List<JobDto>> = try {
        val response = apiService.getMyJobs()
        if (response.isSuccessful) Result.success(response.body().orEmpty())
        else Result.failure(UserException("Ошибка сервера: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось загрузить список работ"))
    }

    suspend fun saveMyJob(job: JobDto): Result<JobDto> = try {
        val response = apiService.saveMyJob(job)
        if (response.isSuccessful) {
            val body = response.body()
                ?: return Result.failure(UserException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(UserException("Ошибка сервера: ${response.code()}"))
        }
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось сохранить работу"))
    }

    suspend fun deleteMyJob(id: Long): Result<Unit> = try {
        val response = apiService.deleteMyJobById(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(UserException("Ошибка сервера: ${response.code()}"))
    } catch (e: IOException) {
        Result.failure(UserException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(UserException(e.message ?: "Не удалось удалить запись"))
    }
}

class UserException(message: String) : Exception(message)