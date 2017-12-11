package com.example.mateusz.songle.songdb

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.example.mateusz.songle.Difficulty

@Dao interface SongDao {

    @Query("select * from Songs")
    fun getAllSongs() : List<Song>

    @Query("select * from Songs where number = :number")
    fun getSongByNumber(number: Int) : Song

    @Query("select * from Lyrics where number = :number")
    fun getLyricsByNumber(number: Int) : Lyrics

    @Query("select * from Maps where number = :number and difficulty = :difficulty")
    fun getMap(number: Int, difficulty: Difficulty) : SongMap

    @Query("select count(*) from Songs")
    fun getNumberOfSongs(): Int

    @Insert(onConflict = REPLACE)
    fun insertSong(song: Song)

    @Insert(onConflict = REPLACE)
    fun insertLyrics(lyrics: Lyrics)

    @Insert(onConflict = REPLACE)
    fun insertMap(map: SongMap)
}