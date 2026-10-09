package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NeWorkDb : RoomDatabase(){

    abstract fun postDao(): PostDao

    companion object{
        const val NAME ="nework.db"
    }
}