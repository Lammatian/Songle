package com.example.mateusz.songle

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import android.widget.TextView
import android.widget.Toast
import com.example.mateusz.songle.songdb.Lyrics
import com.example.mateusz.songle.songdb.Song
import com.example.mateusz.songle.songdb.SongDatabase
import com.example.mateusz.songle.songdb.SongMap
import kotlinx.android.synthetic.main.activity_loading.*
import java.text.SimpleDateFormat
import java.util.*

class LoadingActivity : AppCompatActivity() {

    private val baseUrl: String = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data"
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var timestamp: Date
    private var numberOfSongs: Int = 0
    private lateinit var songDB: SongDatabase
    private val numberToDifficulty: HashMap<Int, Difficulty> = hashMapOf(
            5 to Difficulty.Cakewalk,
            4 to Difficulty.Easy,
            3 to Difficulty.Medium,
            2 to Difficulty.Hard,
            1 to Difficulty.VeryHard
    )
    private lateinit var newSongs: List<Song>
    private lateinit var connMgr: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Change fonts
        val tf = Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        FontChangeCrawler(tf).replaceFonts(this.mainLoadingView)

        // Check connection
        connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connRunnable.run()
    }

    /**
     * Get new song data from the server if necessary
     */
    private fun getData() {
        // Shared preferences retrieval
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Get the timestamp of the last known song file and the number of songs in it
        // If user has not timestamp saved, it would get an old one to prompt downloading songs
        timestamp = dateFormat.parse(sharedPreferences.getString("timestamp", "Fri Dec 01 12:00:00 UTC 2017"))

        //region Database
        // Get text view for updates
        val downloadInfo = findViewById<TextView>(R.id.downloadedContent)

        // Connect to database and get information about songs
        songDB = Room.databaseBuilder(applicationContext, SongDatabase::class.java, "Songs")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        numberOfSongs = songDB.songDao().getNumberOfSongs()

        // Check for new songs
        val songUrl = baseUrl + "/songs/songs.xml"
        val serverTimestampTask = GetTimestampTask(MapsActivity.Dcl()).execute(songUrl)
        val serverTimestamp = serverTimestampTask.get() as Date

        // Update timestamp
        val editor = sharedPreferences.edit()
        editor.putString("timestamp", serverTimestamp.toString())
        editor.apply()

        // Check if there are any updates
        if (serverTimestamp.toString() != timestamp.toString()) {
            val parsedSongs = DownloadNewSongsTask(MapsActivity.Dcl(), songUrl, numberOfSongs).execute()
            newSongs = parsedSongs.get() as List<Song>

            // Add new songs, lyrics and map points to the database
            for (i in 0 until newSongs.size) {
                songDB.songDao().insertSong(newSongs[i])

                val newSongNum = numberOfSongs + 1 + i

                // Download lyrics to new songs
                val lyricsUrl = baseUrl + "/songs/" + String.format("%02d", newSongNum) + "/lyrics.txt"
                DownloadLyricsTask(object : LyricsDownloadCompleteListener {
                    override fun onDownloadComplete(result: String) {
                        println("Lyrics download complete: " + newSongNum)
                        downloadInfo.text = getString(R.string.lyricsEnd, newSongNum)
                        songDB.songDao().insertLyrics(Lyrics(newSongNum, result))
                    }
                }, object: LyricsDownloadStartedListener {
                    override fun onDownloadStarted() {
                        downloadInfo.text = getString(R.string.lyricsStart, newSongNum)
                    }
                }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, lyricsUrl)

                // Download all maps to new songs
                for (j in 1..5) {
                    val mapUrl = baseUrl + "/songs/" + String.format("%02d", newSongNum) + "/map" + j.toString() + ".kml"
                    DownloadMapTask(object : MapDownloadCompleteListener {
                        override fun onDownloadComplete(result: List<MapPoint>?) {
                            downloadInfo.text = getString(R.string.mapEnd, newSongNum, j)
                            println("Map download complete: $newSongNum, $j")
                            songDB.songDao().insertMap(SongMap(newSongNum, numberToDifficulty[j]!!, result!!))

                            if (i == newSongs.size - 1 && j == 5) {
                                downloadInfo.text = getString(R.string.downloadCompleted)
                                goToMap()
                            }

                        }
                    }, object : MapDownloadStartedListener {
                        override fun onDownloadStarted() {
                            downloadInfo.text = getString(R.string.mapStart, newSongNum, j)
                        }
                    }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mapUrl)
                }
            }
        }
        // If no updates, start the game immediately
        else {
            downloadInfo.text = getString(R.string.downloadCompleted)
            // At this point you may as well make it look professional
            object : CountDownTimer(300, 300) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    goToMap()
                }
            }.start()
        }
        //endregion
    }

    /**
     * Start the MapsActivity to enable playing the game
     */
    fun goToMap() {
        songDB.close()
        val intent = Intent(applicationContext, MapsActivity::class.java)
        startActivity(intent)
    }

    //region Network management
    private fun isOnline() : Boolean {
        return connMgr.activeNetworkInfo != null && connMgr.activeNetworkInfo.isConnected
    }

    val connHandler = Handler()
    private var connRunnable = object : Runnable {
        override fun run() {
            if (isOnline())
                getData()
            else {
                Toast.makeText(this@LoadingActivity,
                        "No Internet connection",
                        Toast.LENGTH_SHORT).show()
                connHandler.postDelayed(this, 2000)
            }
        }
    }
    //endregion
}
