package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.db.EventDao
import ru.netology.nework.db.toDto
import ru.netology.nework.db.toEntity
import ru.netology.nework.dto.CreateEventRequest
import ru.netology.nework.dto.EventDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    @OptIn(ExperimentalPagingApi::class)
    fun pagingEvents(): Flow<PagingData<EventDto>> =
        Pager(
            config = PagingConfig(
                pageSize = 12,
                prefetchDistance = 4,
                enablePlaceholders = false
            ), remoteMediator = EventRemoteMediator(apiService, eventDao),
            pagingSourceFactory = { eventDao.pagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDto() }
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

    suspend fun updateDb(event: EventDto)=eventDao.upsert(event.toEntity())

    suspend fun deleteFromDb(id: Long)= eventDao.deleteById(id)

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