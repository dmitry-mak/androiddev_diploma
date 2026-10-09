package ru.netology.nework.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeWorkDb =
        Room.databaseBuilder(context, NeWorkDb::class.java, NeWorkDb.NAME).build()

    @Provides
    fun providePostDao(db: NeWorkDb): PostDao = db.postDao()
}