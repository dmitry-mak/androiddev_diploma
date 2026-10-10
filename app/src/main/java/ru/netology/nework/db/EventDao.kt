package ru.netology.nework.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert


@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER by id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    @Upsert
    suspend fun upsert(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun clear()

    @Transaction
    suspend fun refreshAll(events: List<EventEntity>){
        clear()
        upsertAll(events)
    }

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT id FROM events ORDER by id DESC LIMIT :count")
    suspend fun topIds(count: Int): List<Long>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM events WHERE id IN (:ids)")
    suspend fun countByIds(ids: List<Long>): Int

}