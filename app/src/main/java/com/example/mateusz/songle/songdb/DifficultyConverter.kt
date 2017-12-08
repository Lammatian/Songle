package com.example.mateusz.songle.songdb

import android.arch.persistence.room.TypeConverter
import com.example.mateusz.songle.Difficulty

class DifficultyConverter {
    @TypeConverter
    fun difficultyToString(difficulty: Difficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun fromString(diff: String): Difficulty {
        val s2d = hashMapOf(
                "Cakewalk" to Difficulty.Cakewalk,
                "Easy" to Difficulty.Easy,
                "Medium" to Difficulty.Medium,
                "Hard" to Difficulty.Hard,
                "VeryHard" to Difficulty.VeryHard
        )

        return s2d[diff]!!
    }
}