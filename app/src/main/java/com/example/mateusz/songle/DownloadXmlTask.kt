package com.example.mateusz.songle

import android.content.res.Resources
import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by mateusz on 03/11/17.
 */
interface DownloadCompleteListener{
    fun onDownloadComplete(result: String)
}

class DownloadXmlTask(private val caller: DownloadCompleteListener,
                      private val summaryPref: Boolean) :
        AsyncTask<String, Void, Any>() {

    override fun doInBackground(vararg urls: String): Any {
        return try {
            return when (urls[0]) {
                "Song" -> loadSongsFromNetwork(urls[1])
                "Map" -> loadMapFromNetwork(urls[1])
                "Lyrics" -> loadLyricsFromNetwork(urls[1])
                else -> "Incorrect type"
            }
        }
        catch (e: IOException) {
            "Unable to load content. Check your network connection"
        }
        catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadSongsFromNetwork(urlString: String): Any {
        //var result = StringBuilder()
        val stream = downloadUrl(urlString)

        // parse XML
        val parser = XMLParser()

        return parser.isUpToDate(stream) ?: return "No updates"
    }

    private fun loadMapFromNetwork(urlString: String): List<MapPoint> {
        val stream = downloadUrl(urlString)

        // parse XML
        val parser = MapParser()

        return parser.parse(stream)
    }

    private fun loadLyricsFromNetwork(urlString: String): List<List<String>> {
        // TODO: Punctuation parsing?
        val stream = downloadUrl(urlString)

        // get lyrics of the song
        val lyrics = stream.bufferedReader().use{it.readText()}

        // split by lines
        val byLine = lyrics.split("\n")

        // split by spaces
        val bySpace = byLine.map{it.split(" ")}

        return bySpace
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection // or HttpsURLConnection

        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true

        // start the query
        conn.connect()
        return conn.inputStream
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)

        caller.onDownloadComplete(result.toString())
    }
}