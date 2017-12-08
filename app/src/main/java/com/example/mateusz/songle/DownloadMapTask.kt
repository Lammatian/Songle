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
class DownloadMapTask(private val caller: DownloadCompleteListener) :
        AsyncTask<String, Void, List<MapPoint>?>() {

    override fun doInBackground(vararg urls: String): List<MapPoint>? {
        return try {
            loadMapFromNetwork(urls[0])
        }
        catch (e: IOException) {
            null
        }
        catch (e: XmlPullParserException) {
            null
        }
    }

    private fun loadMapFromNetwork(urlString: String): List<MapPoint> {
        val stream = downloadUrl(urlString)

        // parse XML
        val parser = MapParser()

        return parser.parse(stream)
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

    override fun onPostExecute(result: List<MapPoint>?) {
        super.onPostExecute(result)
        caller.onDownloadComplete(result.toString())
    }
}