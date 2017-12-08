package com.example.mateusz.songle

import android.os.AsyncTask
import com.example.mateusz.songle.songdb.Song
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DownloadNewSongsTask(private val caller: DownloadCompleteListener,
                        private val url: String,
                        private val timestamp: Date,
                        private val numberOfSongs: Int) :
        AsyncTask<Void, Void, List<Song>?>() {

    override fun doInBackground(vararg urls: Void): List<Song>? {
        return try {
            loadSongsFromNetwork(url, timestamp, numberOfSongs)
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadSongsFromNetwork(urlString: String, timestamp: Date, numberOfSongs: Int): List<Song>? {
        // Get the stream from url
        var stream = downloadUrl(urlString)
        val parser = SongsParser()

        // Check if songs are up to date and return empty list if so
        if (parser.isUpToDate(stream, timestamp))
            return emptyList()

        stream = downloadUrl(urlString)
        // If not up to date, download new songs
        return parser.parse(stream, numberOfSongs)
    }

    // TODO: Move somewhere more general for less code repetition
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

    // TODO: Is that at all necessary? Use dcl better
    override fun onPostExecute(result: List<Song>?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}