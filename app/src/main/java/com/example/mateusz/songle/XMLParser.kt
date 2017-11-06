package com.example.mateusz.songle

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Created by mateusz on 03/11/17.
 */
data class Song(val number: Int, val artist: String, val title: String, val link: String)

class XMLParser {
    // namespace
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun isUpToDate(input: InputStream): List<Song>? {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, ns, "Songs")
            if (parser.getAttributeValue(0) == "2017-10-09T10:00:33.775+01:00[Europe/London]")
                return null

            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<Song> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readFeed(parser: XmlPullParser): List<Song> {
        val songs = ArrayList<Song>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "Song")
                songs.add(readSong(parser))
            else
                skip(parser)
        }

        return songs
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): Song {
        parser.require(XmlPullParser.START_TAG, ns, "Song")
        var number = 0
        var artist = ""
        var title = ""
        var link = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            when (parser.name) {
                "Number" -> number = readValue(parser, "Number").toInt()
                "Artist" -> artist = readValue(parser, "Artist")
                "Title" -> title = readValue(parser, "Title")
                "Link" -> link = readValue(parser, "Link")
                else -> skip(parser)
            }
        }

        return Song(number, artist, title, link)
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