package com.example.mateusz.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by mateusz on 03/12/17.
 */
class DownloadSongsTask(private val caller: DownloadCompleteListener,
                        private val summaryPref: Boolean) :
        AsyncTask<String, Void, List<Song>?>() {

    override fun doInBackground(vararg urls: String): List<Song>? {
        return try {
            // TODO: Timestamp
            loadSongsFromNetwork(urls[0], "")
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadSongsFromNetwork(urlString: String, timestamp: String): List<Song>? {
        //var result = StringBuilder()
        val stream = downloadUrl(urlString)

        // parse XML
        val parser = SongsParser()

        // Check if songs are up to date and return empty list if so
        // TODO: Timestamp
        //if (parser.isUpToDate(stream, timestamp))
        //    return emptyList()

        return parser.parse(stream)
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

    override fun onPostExecute(result: List<Song>?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}