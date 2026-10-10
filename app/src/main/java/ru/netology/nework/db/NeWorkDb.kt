package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PostEntity::class, EventEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NeWorkDb : RoomDatabase(){

    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao

    companion object{
        const val NAME ="nework.db"
    }
}