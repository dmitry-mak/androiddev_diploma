package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
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
}

class UserException(message: String) : Exception(message)