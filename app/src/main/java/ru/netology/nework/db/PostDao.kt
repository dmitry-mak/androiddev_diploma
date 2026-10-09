package ru.netology.nework.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Query("DELETE FROM posts")
    suspend fun clear()

    @Transaction
    suspend fun refreshAll(posts: List<PostEntity>) {
        clear()
        upsertAll(posts)
    }

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Query("SELECT id FROM posts ORDER by id DESC LIMIT :count ")
    suspend fun topIds(count: Int): List<Long>

    @Query("SELECT COUNT(*) FROM posts WHERE id IN (:ids)")
    suspend fun countByIds(ids: List<Long>): Int
}