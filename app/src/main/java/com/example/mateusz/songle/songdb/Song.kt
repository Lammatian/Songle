package com.example.mateusz.songle.songdb

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.example.mateusz.songle.Difficulty
import com.example.mateusz.songle.MapPoint

@Entity(tableName = "Songs")
data class Song(@PrimaryKey(autoGenerate = false) var number: Int,
                @ColumnInfo(name = "Artist") var artist: String,
                @ColumnInfo(name = "Title") var title: String,
                @ColumnInfo(name = "Link") var link: String)

@Entity(tableName = "Lyrics")
data class Lyrics(@PrimaryKey(autoGenerate = false) var number: Int,
                  @ColumnInfo(name = "Lyrics") var lyrics: String)

@Entity(tableName = "Maps", primaryKeys = arrayOf("Number", "Difficulty"))
data class SongMap(@ColumnInfo(name = "Number") var number: Int,
                   @ColumnInfo(name = "Difficulty") var difficulty: Difficulty,
                   @ColumnInfo(name = "Points") var points: List<MapPoint>)
