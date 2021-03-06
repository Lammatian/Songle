package com.example.mateusz.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class GetTimestampTask(private val caller: LyricsDownloadCompleteListener) :
        AsyncTask<String, Void, Date?>() {

    override fun doInBackground(vararg urls: String): Date? {
        return try {
            loadSongsFromNetwork(urls[0])
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadSongsFromNetwork(urlString: String): Date {
        val stream = downloadUrl(urlString)

        // parse XML
        val parser = SongsParser()

        return parser.getTimestamp(stream)
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

    override fun onPostExecute(result: Date?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}