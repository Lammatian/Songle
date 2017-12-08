package com.example.mateusz.songle.songdb

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class SongModule(private val context: Context) {

    @Provides
    fun providesAppContext() = context

    @Provides fun providesAppDatabase(context: Context): SongDatabase =
            Room.databaseBuilder(context, SongDatabase::class.java, "Songs").allowMainThreadQueries().build()

    @Provides fun providesToDoDao(database: SongDatabase) = database.songDao()
}