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
        parser.require(XmlPullParser.START_TAG, ns, "feed")

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
                "Number" -> number = readNumber(parser)
                "Artist" -> artist = readArtist(parser)
                "Title" -> title = readTitle(parser)
                "Link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }

        return Song(number, artist, title, link)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readNumber(parser: XmlPullParser): Int {
        parser.require(XmlPullParser.START_TAG, ns, "Number")
        val number = readText(parser).toInt()
        parser.require(XmlPullParser.END_TAG, ns, "Number")
        return number
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Title")
        return title
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readArtist(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Artist")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Artist")
        return artist
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Link")
        return link
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