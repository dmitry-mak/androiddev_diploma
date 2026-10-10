package ru.netology.nework.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import okio.IOException
import ru.netology.nework.api.ApiService
import ru.netology.nework.db.EventDao
import ru.netology.nework.db.EventEntity
import ru.netology.nework.db.PostDao
import ru.netology.nework.db.toEntity


@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: ApiService,
    private val eventDao: EventDao
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        Log.d("MEDIATOR","events load: $loadType")
        return try {
            val response = when (loadType) {
                LoadType.REFRESH -> apiService.getLatestEvents(state.config.pageSize)

                LoadType.PREPEND -> {
                    val firstId = state.firstItemOrNull()?.id
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getEventsAfterId(firstId, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val lastId = state.lastItemOrNull()?.id
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getEventsBeforeId(lastId, state.config.pageSize)
                }
            }
            if (!response.isSuccessful) {
                return MediatorResult.Error(IOException("Ошибка сервера: ${response.code()}"))
            }
            val items = response.body().orEmpty().map { it.toEntity() }
            Log.d("MEDIATOR", "events $loadType: получено ${items.size}, firstId=${items.firstOrNull()?.id}")

            when (loadType) {
                LoadType.REFRESH -> eventDao.refreshAll(items)
                else -> eventDao.upsertAll(items)
            }
            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}