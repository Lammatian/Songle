package com.example.mateusz.songle

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Created by mateusz on 07/11/17.
 */

data class MapPoint(val point: DoubleArray, val description: String, val name: IntArray)

class MapParser {

    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<MapPoint> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readFeed(parser: XmlPullParser): List<MapPoint> {
        val points = ArrayList<MapPoint>()
        parser.require(XmlPullParser.START_TAG, ns, "kml")
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, ns, "Document")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "Placemark")
                points.add(readPoint(parser))
            else
                skip(parser)
        }

        return points
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPoint(parser: XmlPullParser): MapPoint {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark")
        var point = DoubleArray(3)
        var name = IntArray(2)
        var description = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            when (parser.name) {
                "name" -> name = readValue(parser, "name").split(':').map{it.toInt()}.toIntArray()
                "description" -> description = readValue(parser, "description")
                "Point" -> point = readCoordinates(parser, "Point")
                else -> skip(parser)
            }
        }

        return MapPoint(point, description, name)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readValue(parser: XmlPullParser, value: String): String {
        parser.require(XmlPullParser.START_TAG, ns, value)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, value)
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }

        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCoordinates(parser: XmlPullParser, value: String): DoubleArray {
        parser.require(XmlPullParser.START_TAG, ns, value)
        parser.nextTag()
        var result = readValue(parser, "coordinates").split(',').map{it.toDouble()}.toDoubleArray()
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, ns, value)
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG)
            throw IllegalStateException()

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}