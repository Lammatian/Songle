package com.example.mateusz.songle

import android.content.res.Resources
import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
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
        AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg urls: String): String {
        return try {
            loadXmlFromNetwork(urls[0])
        }
        catch (e: IOException) {
            "Unable to load content. Check your network connection"
        }
        catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadXmlFromNetwork(urlString: String): String {
        //var result = StringBuilder()
        var stream = downloadUrl(urlString)

        // parse XML
        var parser = XMLParser()

        val result = parser.isUpToDate(stream) ?: return "No updates"

        return result.joinToString(", ")
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

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        caller.onDownloadComplete(result)
    }
}