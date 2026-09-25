package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.dto.CreateEventRequest
import ru.netology.nework.dto.EventDto
import java.io.IOException
import java.security.interfaces.RSAKey
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class EventRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getEvents(): Result<List<EventDto>> = try {
        val response = apiService.getEvents()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty())
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось загрузить событие"))
    }

    suspend fun getEventById(id: Long): Result<EventDto> = try {
        val response = apiService.getEventById(id)
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(EventException("Событие не найдено"))
            Result.success(body)
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось загрузить событие"))
    }

    suspend fun createEvent(request: CreateEventRequest): Result<EventDto> = try {
        val response = apiService.saveEvent(request)
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(EventException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось сохранить событие"))
    }

    suspend fun deleteEvent(id: Long): Result<Unit> = try {
        val response = apiService.deleteEventById(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось удалить событие"))
    }

    suspend fun like(event: EventDto): Result<EventDto> = try {
        val response = if (event.likedByMe) {
            apiService.unlikeEventById(event.id)
        } else {
            apiService.likeEventById(event.id)
        }
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(EventException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось изменить лайк"))
    }

    suspend fun participate(event: EventDto): Result<EventDto> = try {
        val response = if (event.participatedByMe) {
            apiService.leaveEventById(event.id)
        } else {
            apiService.joinEventById(event.id)
        }
        if (response.isSuccessful) {
            val body =
                response.body() ?: return Result.failure(EventException("Пустой ответ от сервера"))
            Result.success(body)
        } else {
            Result.failure(EventException(errorMessage(response.code())))
        }
    } catch (e: IOException) {
        Result.failure(EventException("Ошибка сети"))
    } catch (e: Exception) {
        Result.failure(EventException(e.message ?: "Не удалось изменить статус участия"))
    }

    private fun errorMessage(code: Int): String = when (code) {
        401, 403 -> "Необходимо авторизоваться"
        404 -> "Событие не найдено"
        else -> "Ошибка сервера: $code"
    }
}

class EventException(message: String) : Exception(message)