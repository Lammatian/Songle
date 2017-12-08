package com.example.mateusz.songle.songdb

import android.arch.persistence.room.TypeConverter
import com.example.mateusz.songle.MapPoint

class MapPointConverter {

    private fun mapPointToString(mapPoint: MapPoint): String {
        return mapPoint.point.joinToString(",") + "," +
                mapPoint.description + "," +
                mapPoint.name.joinToString(",")
    }

    @TypeConverter
    fun pointsToString(points: List<MapPoint>): String {
        return points.map{mapPointToString(it)}.joinToString("\n")
    }

    private fun pointFromString(mp: String): MapPoint {
        val values = mp.split(",")
        return MapPoint(doubleArrayOf(values[0].toDouble(), values[1].toDouble()),
                values[3],
                intArrayOf(values[4].toInt(), values[5].toInt()))
    }

    @TypeConverter
    fun pointsFromString(mp: String): List<MapPoint> {
        val list = mp.split("\n")
        return list.map{pointFromString(it)}
    }
}