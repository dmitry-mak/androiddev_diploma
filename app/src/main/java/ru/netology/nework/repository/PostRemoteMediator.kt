package ru.netology.nework.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import ru.netology.nework.api.ApiService
import ru.netology.nework.db.PostDao
import ru.netology.nework.db.PostEntity
import ru.netology.nework.db.toEntity
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        Log.d("MEDIATOR", "load: ${loadType}")
        return try {
            val response = when (loadType) {
                LoadType.REFRESH -> apiService.getLatestPosts(state.config.pageSize)

                LoadType.PREPEND -> {
                    val firstId = state.firstItemOrNull()?.id
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getAfterId(firstId, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val lastId = state.lastItemOrNull()?.id
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    apiService.getBeforeId(lastId, state.config.pageSize)
                }
            }
            if (!response.isSuccessful) {
                return MediatorResult.Error(IOException("Ошибка сервера: ${response.code()}"))
            }
            val items = response.body().orEmpty().map { it.toEntity() }
            Log.d(
                "MEDIATOR",
                "$loadType: получено ${items.size}, firstId=${items.firstOrNull()?.id}"
            )

            when (loadType) {
                LoadType.REFRESH -> postDao.refreshAll(items)
                else -> postDao.upsertAll(items)
            }
            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}