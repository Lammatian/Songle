package com.example.mateusz.songle.songdb

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [(Song::class), (Lyrics::class), (SongMap::class)], version = 2, exportSchema = false)
@TypeConverters(DifficultyConverter::class, MapPointConverter::class)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao() : SongDao
}