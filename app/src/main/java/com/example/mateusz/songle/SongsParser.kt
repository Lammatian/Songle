package com.example.mateusz.songle

import android.util.Xml
import com.example.mateusz.songle.songdb.Song
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class SongsParser {
    // namespace
    private val ns: String? = null

    /**
     * Check if the song file is up to date
     */
    @Throws(XmlPullParserException::class, IOException::class)
    fun getTimestamp(input: InputStream): Date {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, ns, "Songs")

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            return format.parse(parser.getAttributeValue(0))
        }
    }

    /**
     * Parse the song file returning all the new songs
     */
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream, numberOfSongs: Int): List<Song> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(input, null)
            parser.nextTag()
            return readFeed(parser, numberOfSongs)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser, numberOfSongs: Int): List<Song> {
        val songs = ArrayList<Song>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            // TODO: This is not really scalable
            if (parser.name == "Song") {
                val song = readSong(parser)
                if (song.number > numberOfSongs)
                    songs.add(song)
            }
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

    @Throws(XmlPullParserException::class, IOException::class)
    private fun nextSong(parser: XmlPullParser) {
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