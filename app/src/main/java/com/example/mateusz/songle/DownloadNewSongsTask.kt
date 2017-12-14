package com.example.mateusz.songle

import android.os.AsyncTask
import com.example.mateusz.songle.songdb.Song
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadNewSongsTask(private val caller: LyricsDownloadCompleteListener,
                        private val url: String,
                        private val numberOfSongs: Int) :
        AsyncTask<Void, Void, List<Song>?>() {

    override fun doInBackground(vararg urls: Void): List<Song>? {
        return try {
            loadSongsFromNetwork(url, numberOfSongs)
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadSongsFromNetwork(urlString: String, numberOfSongs: Int): List<Song>? {
        // Get the stream from url
        val stream = downloadUrl(urlString)
        val parser = SongsParser()

        // If not up to date, download new songs
        return parser.parse(stream, numberOfSongs)
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

    override fun onPostExecute(result: List<Song>?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}