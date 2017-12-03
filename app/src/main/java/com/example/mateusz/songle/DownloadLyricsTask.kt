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
interface DownloadCompleteListener{
    fun onDownloadComplete(result: String)
}

class DownloadLyricsTask(private val caller: DownloadCompleteListener,
                         private val summaryPref: Boolean) :
        AsyncTask<String, Void, List<List<String>>?>() {

    override fun doInBackground(vararg urls: String): List<List<String>>? {
        return try {
            loadLyricsFromNetwork(urls[0])
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadLyricsFromNetwork(urlString: String): List<List<String>> {
        // TODO: Punctuation parsing?
        val stream = downloadUrl(urlString)

        // get lyrics of the song
        val lyrics = stream.bufferedReader().use{it.readText()}

        // return lyrics split by lines and by spaces
        return lyrics.split("\n").map{it.split(" ")}
    }

    // TODO: Move to more general place for no code repetition
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

    override fun onPostExecute(result: List<List<String>>?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}
