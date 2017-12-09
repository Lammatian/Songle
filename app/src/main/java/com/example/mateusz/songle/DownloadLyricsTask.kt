package com.example.mateusz.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

interface DownloadCompleteListener{
    fun onDownloadComplete(result: String)
}

class DownloadLyricsTask(private val caller: DownloadCompleteListener) :
        AsyncTask<String, Void, String?>() {

    override fun doInBackground(vararg urls: String): String? {
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

    private fun loadLyricsFromNetwork(urlString: String): String {
        val stream = downloadUrl(urlString)

        // get and return lyrics of the song
        return stream.bufferedReader().use{it.readText()}
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

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}
