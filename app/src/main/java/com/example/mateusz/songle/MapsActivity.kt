package com.example.mateusz.songle

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.arch.persistence.room.Room
import android.content.*
import android.support.v7.app.AppCompatActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.location.Location
import android.net.ConnectivityManager
import android.os.*
import android.preference.PreferenceManager
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.mateusz.songle.songdb.Lyrics
import com.example.mateusz.songle.songdb.Song
import com.example.mateusz.songle.songdb.SongDatabase
import com.example.mateusz.songle.songdb.SongMap
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * ic_treasure taken from https://openclipart.org/detail/257257/chromatic-musical-notes-typography-no-background
 * ic_idea taken from https://thenounproject.com/term/idea/62335/, work of Takao Umehara
 */

// Map boundaries
private const val MIN_LAT = 55.942617
private const val MAX_LAT = 55.946233
private const val MIN_LNG = -3.192473
private const val MAX_LNG = -3.184319

// Difficulties
enum class Difficulty {
    Cakewalk,
    Easy,
    Medium,
    Hard,
    VeryHard
}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var points: List<MapPoint>
    private lateinit var treasure: Marker
    private lateinit var treasureLoc: Location
    private lateinit var treasureLine: List<String>
    private var treasureLineNumber = -1
    private lateinit var lyrics: List<List<String>>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var guessPenalty: List<Double>
    private lateinit var wordsFound: WordsFound
    private lateinit var wordsInGame: HashMap<ArrayList<Int>, Word>
    private lateinit var currentSong: Song
    private lateinit var difficulty: Difficulty
    private lateinit var songDB: SongDatabase
    private var markerToPoint: HashMap<Marker, MapPoint> = HashMap()
    private lateinit var tf: Typeface
    private lateinit var timestamp: Date
    private var numberOfSongs: Int = 0
    private var lifelongStatistics: HashMap<String, Long> = hashMapOf(
            "Best score" to 0L,
            "Games played" to 0L,
            "Games won" to 0L,
            "Total time in game" to 0L,
            "Average score" to 0L,
            "Last game score" to 0L
    )
    private val desToIcon: HashMap<String, Int> = hashMapOf(
            "unclassified" to R.mipmap.wht_blank,
            "boring" to R.mipmap.ylw_blank,
            "notboring" to R.mipmap.ylw_circle,
            "interesting" to R.mipmap.orange_diamond,
            "veryinteresting" to R.mipmap.red_stars)
    private val numberToDifficulty: HashMap<Int, Difficulty> = hashMapOf(
            5 to Difficulty.Cakewalk,
            4 to Difficulty.Easy,
            3 to Difficulty.Medium,
            2 to Difficulty.Hard,
            1 to Difficulty.VeryHard
    )
    private val baseUrl: String = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data"
//    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    private var viewType: ViewType = ViewType.List
    private var numberOfWrongGuesses: Int = 0
    private lateinit var newSongs: List<Song>
    private lateinit var songs: List<Song>
    private var wordFeed: ArrayList<Button> = ArrayList()

    // TODO: Implement
    //region Network receiver
    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.activeNetworkInfo

            if (networkInfo?.type == ConnectivityManager.TYPE_WIFI) {
                // use WIFI
            }
            else if (networkInfo != null) {
                // use network
            }
            else {
                // No WIFI and permission or no connection
            }
        }
    }
    //endregion

    private var networkReceiver = NetworkReceiver()

    class Dcl : DownloadCompleteListener {
        override fun onDownloadComplete(result: String) {
            print(result)
        }
    }

    //region On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Register BroadcastReceiver to track connection changes.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(networkReceiver, filter)

        // Font changing
        tf =  Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        FontChangeCrawler(tf).replaceFonts(this.mainMapView)

        // Toggle switch onClick
        wordViewType.setOnClickListener {
            _ -> changeWordView()
        }

        // Shared preferences retrieval
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Statistics initialization
        for (stat in lifelongStatistics.keys) {
            if (sharedPreferences.getLong(stat, -1L) != -1L)
                lifelongStatistics[stat] = sharedPreferences.getLong(stat, 0L)
        }

        // Get the timestamp of the last known song file and the number of songs in it
        timestamp = dateFormat.parse(sharedPreferences.getString("timestamp", "2017-12-08T12:50:20.327Z[Europe/London]"))

        // Guess penalty reading and scoring test
        val inputStream = BufferedReader(InputStreamReader(assets.open("helpers/guesspen.txt")))
        var readGuessPen = inputStream.readText()
        readGuessPen = readGuessPen.substring(1, readGuessPen.length-1)
        guessPenalty = readGuessPen.split(",").map{it.toDouble()}

        // Connect to database and get information about songs
        // TODO: When loading, show some information
        songDB = Room.databaseBuilder(applicationContext, SongDatabase::class.java, "Songs")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        numberOfSongs = songDB.songDao().getNumberOfSongs()

        // Check for new songs
        val songUrl = baseUrl + "/songs/songs.xml"
        val serverTimestampTask = GetTimestampTask(Dcl()).execute(songUrl)
        val serverTimestamp = serverTimestampTask.get() as Date

        if (serverTimestamp != timestamp) {
            val parsedSongs = DownloadNewSongsTask(Dcl(), songUrl, numberOfSongs).execute()
            newSongs = parsedSongs.get() as List<Song>

            // Add new songs, lyrics and map points to the database
            for (i in 0 until newSongs.size) {
                songDB.songDao().insertSong(newSongs[i])

                val newSongNum = numberOfSongs + 1 + i

                // Download lyrics to new songs
                val lyricsUrl = baseUrl + "/songs/" + String.format("%02d", newSongNum) + "/lyrics.txt"
                val parsedLyrics = DownloadLyricsTask(Dcl()).execute(lyricsUrl)
                val lyrics = parsedLyrics.get()!!
                songDB.songDao().insertLyrics(Lyrics(newSongNum, lyrics))

                // Download all maps to new songs
                for (j in 1..5) {
                    val mapUrl = baseUrl + "/songs/" + String.format("%02d", newSongNum) + "/map" + j.toString() + ".kml"
                    val parsedMap = DownloadMapTask(Dcl()).execute(mapUrl)
                    val map = parsedMap.get()!!
                    songDB.songDao().insertMap(SongMap(newSongNum, numberToDifficulty[j]!!, map))
                }
            }
        }

        // Finally, get all songs from the database
        songs = songDB.songDao().getAllSongs()

        // Update timestamp
        val editor = sharedPreferences.edit()
        editor.putString("timestamp", serverTimestamp.toString())
        editor.apply()

        // Update number of songs
        numberOfSongs = songs.size
    }
    //endregion

    //region Map handlers
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Set map style
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.custom_style2))
        }
        catch (e: Resources.NotFoundException) {
            println("Style not found exception thrown [onMapReady]")
        }

        // Move camera to player location
        // TODO: Get location and zoom to the location (OnLocationChanged method)
        val zoom = 16f
        val edi = LatLng(55.946, -3.1888)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edi, zoom))

        // Current position on the map
        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        }
        catch (se : SecurityException) {
            println("Security exception thrown [onMapReady]")
        }

        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true
        //mMap.isMyLocationEnabled = true

        // TODO: First if statement doesn't work
        //region Location services
        // Ask for location services if needed
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("In order to use the app, you need to provide location")
                        .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                ActivityCompat.requestPermissions(this@MapsActivity,
                                        Array(1){Manifest.permission.ACCESS_FINE_LOCATION},
                                        99)
                            }
                        })
                        .create()
                        .show()
            }
        }
        else {
            ActivityCompat.requestPermissions(this,
                    Array(1){Manifest.permission.ACCESS_FINE_LOCATION},
                    99)
        }
        //endregion

        //region Marker click handling
        // Set listener for click on the button
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                // Get location of player and marker
                // TODO: Get proper location of the player
                val myLoc = Location("")
                myLoc.latitude = edi.latitude
                myLoc.longitude = edi.longitude
                val markerLoc = Location("")
                markerLoc.latitude = marker.position.latitude
                markerLoc.longitude = marker.position.longitude

                // Check if player can pick up the word from current distance
                if (myLoc.distanceTo(markerLoc) > 1000)
                    return true

                // Sort out treasure separately
                if (marker == treasure) {
                    treasure.remove()
                    showTreasure()
                    updateWithNewLine()
                    addWordFeedItem(treasureLine.joinToString(" ") + "[" + treasureLineNumber + "]", true)
                    return true
                }

                // Word found popup dialog with custom style
                val point = markerToPoint[marker]
                val text = hashMapOf(
                        R.id.place to "[" + point!!.name.joinToString(",") + "]",
                        R.id.wordFound to lyrics[point.name[0]-1][point.name[1]-1]
                )

                showDialog(R.layout.dialog_wordfound, R.id.mainWordView, texts = text)
                updateWithNewWord(arrayListOf(point.name[0], point.name[1]))
                marker.remove()

                // Add word to word feed
                addWordFeedItem(text[R.id.wordFound] + " " + text[R.id.place])

                // TODO: Remove this shit from here to onLocationChanged
                if (myLoc.distanceTo(treasureLoc) < 100) {
                    treasure.isVisible = true
                }

                // Stop the default reaction to clicking a marker
                return true
            }
        })
        //endregion

        //region Choose difficulty
        showDifficulty()
        //endregion
    }

    /**
     * Places word icons on the map
     */
    private fun placeWords(points: List<MapPoint>) {
        // Place all the word markers on the map
        for (p in points) {
            val marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(p.point[1], p.point[0]))
                    .icon(BitmapDescriptorFactory.fromResource(desToIcon[p.description]!!)))

            markerToPoint[marker] = p
        }

        // Create random position for the treasure marker
        val treasureLat = MIN_LAT + Math.random()*(MAX_LAT - MIN_LAT)
        val treasureLng = MIN_LNG + Math.random()*(MAX_LNG - MIN_LNG)

        // Add treasure marker
        treasure = mMap.addMarker(MarkerOptions()
                .position(LatLng(treasureLat, treasureLng))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_treasure))
                .visible(true))

        // Save treasure location for easier management
        treasureLoc = Location("")
        treasureLoc.latitude = treasureLat
        treasureLoc.longitude = treasureLng
    }
    //endregion

    //region Game logic
    /**
     * Choose difficulty and start the game
     */
    private fun chooseDifficulty(view: View) {
        difficulty = when (view.id) {
            R.id.btnCakewalk -> Difficulty.Cakewalk
            R.id.btnEasy     -> Difficulty.Easy
            R.id.btnMedium   -> Difficulty.Medium
            R.id.btnHard     -> Difficulty.Hard
            R.id.btnVHard    -> Difficulty.VeryHard
            else             -> Difficulty.Medium
        }

        startGame()
    }

    /**
     * Initialization of the game
     */
    private fun startGame() {
        // Set number of wrong guesses to 0
        numberOfWrongGuesses = 0

        // Get last two songs
        // TODO: Get proper last two songs
        val lastTwo = arrayOf(1, 2)

        // Pick a different song from the above for the game
        var choice = Math.floor(Math.random()*numberOfSongs + 1).toInt()

        while (choice in lastTwo)
            choice = Math.floor(Math.random()*numberOfSongs + 1).toInt()

        // Get song to guess
        currentSong = songs[choice-1]

        // Get map points for the song
        points = songDB.songDao().getMap(choice, difficulty).points

        // Get lyrics for the song
        lyrics = songDB.songDao().getLyricsByNumber(choice).lyrics.split("\n").map{it.split(" ")}

        // Pick treasure and make sure it's not an empty line
        treasureLineNumber = (Math.random()*lyrics.size).toInt()

        while (lyrics[treasureLineNumber].size < 2)
            treasureLineNumber = (Math.random()*lyrics.size).toInt()

        treasureLine = lyrics[treasureLineNumber]

        // Initialize wordsInGame
        wordsInGame = hashMapOf()
        for (point in points) {
            wordsInGame.put(point.name.toCollection(ArrayList()), Word(lyrics[point.name[0]-1][point.name[1]-1],
                    WordValue.valueOf(point.description.capitalize())))
        }

        // Initialize wordFound
        wordsFound = WordsFound(lyrics, wordsInGame)
        wordViewText.text = wordsFound.getWords(viewType)

        // Set treasure and map
        placeWords(points)

        // Start the clock
        startTime = SystemClock.uptimeMillis()
        handler.post(runnable)
    }

    /**
     * Logic after ending a game
     */
    private fun endGame(win: Boolean) {
        // Calculate the score
        val gameScore =
                if (win) score((currentTime/1000).toInt(),
                        wordsFound.numberOfWordsFound,
                        wordsFound.numberOfWordsInGame,
                        difficulty,
                        numberOfWrongGuesses)
                else
                    0

        // Update best score if necessary
        lifelongStatistics["Best score"] = Math.max(gameScore.toLong(), lifelongStatistics["Best score"]!!)

        // Increment number of played and won games if necessary
        lifelongStatistics["Games won"] = lifelongStatistics["Games won"]!! + win.compareTo(false)
        lifelongStatistics["Games played"] = lifelongStatistics["Games played"]!! + 1

        // Add time spent in this game to the statistics
        lifelongStatistics["Total time in game"] = lifelongStatistics["Total time in game"]!! + currentTime

        // Update average score
        val totalScoreBefore = (lifelongStatistics["Games played"]!! - 1) * lifelongStatistics["Average score"]!!
        val newAverage = (totalScoreBefore + gameScore.toLong()) / lifelongStatistics["Games played"]!!
        lifelongStatistics["Average score"] = newAverage

        // Update last game score
        lifelongStatistics["Last game score"] = gameScore.toLong()

        // Update shared preferences
        val editor = sharedPreferences.edit()
        for ((statistic, value) in lifelongStatistics) {
            editor.putLong(statistic, value)
        }
        editor.apply()

        // Show win dialog
        showEndGame(win, gameScore)
    }

    /**
     * Handle correct and incorrect user guess
     */
    private fun makeGuess(view: View, guess: String) : Boolean {
        // If user guessed correctly, show the win dialog with statistics
        if (stringSimilarity(guess, currentSong.title) > 0.9) {
            clearMap()
            // Hide word view
            showWords(view)
            // Handle win
            endGame(true)
            return true
        }
        // If user guessed incorrectly, change button to red for 2 seconds
        // TODO: Shake the button a bit as well
        else {
            val button = view.findViewById<ImageButton>(R.id.makeGuess)
            button.background = resources.getDrawable(R.drawable.no_circle)
            object: CountDownTimer(2000, 2000) {
                override fun onTick(p0: Long) {
                }

                override fun onFinish() {
                    button.background = resources.getDrawable(R.drawable.choice_circle)
                }
            }.start()
            // User guessed incorrectly
            numberOfWrongGuesses += 1
            return false
        }
    }

    /**
     * Clear all the markers from the map
     */
    private fun clearMap() {
        for ((marker, _) in markerToPoint) {
            marker.remove()
        }
        treasure.remove()
    }

    /**
     * Put new word into the collection of found words
     */
    fun updateWithNewWord(place: ArrayList<Int>) {
        // Update word found database
        wordsFound.addWord(place)
        // Update text shown to user
        wordViewText.text = wordsFound.getWords(viewType)
    }

    /**
     * Put the treasure line into the wordsfound database and update word view
     */
    fun updateWithNewLine() {
        // Update word found database with a line
        wordsFound.addLine(treasureLine, treasureLineNumber)
        // Update text shown to user
        wordViewText.text = wordsFound.getWords(viewType)
    }
    //endregion

    //#region GUI Handlers
    // Variable checking if menu is opened or not
    private var opened = false

    fun menuOpenClose(view: View) {
        if (!opened)
            openMenu()
        else
            closeMenu()

        opened = !opened
    }

    private fun openMenu() {
        // TODO: When closing and opening quickly, buttons don't show up
        // TODO: On animation end in opening set visibility to true OR cancel hide animations
        // Set visibility of all buttons to visible and opacity to 0
        fab_help.alpha = 0f
        fab_stats.alpha = 0f
        fab_restart.alpha = 0f
        fab_help.visibility = View.VISIBLE
        fab_stats.visibility = View.VISIBLE
        fab_restart.visibility = View.VISIBLE

        // Animations to show all buttons
        // Help button
        val showHelp = ValueAnimator.ofFloat(0f, 1f)
        showHelp.addUpdateListener {
            val value = showHelp.animatedValue as Float
            fab_help.alpha = value
        }
        showHelp.duration = 300
        showHelp.interpolator = AccelerateInterpolator()

        // Stats button
        val showStats = ValueAnimator.ofFloat(0f, 1f)
        showStats.addUpdateListener {
            val value = showStats.animatedValue as Float
            fab_stats.alpha = value
        }
        showStats.duration = 300
        showStats.interpolator = AccelerateInterpolator()
        showStats.startDelay = 50

        // Restart button
        val showRestart = ValueAnimator.ofFloat(0f, 1f)
        showRestart.addUpdateListener {
            val value = showRestart.animatedValue as Float
            fab_restart.alpha = value
        }
        showRestart.duration = 300
        showRestart.interpolator = AccelerateInterpolator()
        showRestart.startDelay = 100

        // Play all animations together (with given delays)
        val animations = AnimatorSet()
        animations.playTogether(showHelp, showStats, showRestart)
        animations.start()
    }

    private fun closeMenu() {
        // Animations to hide all buttons
        // Help button
        val hideHelp = ValueAnimator.ofFloat(1f, 0f)
        hideHelp.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                fab_help.visibility = View.INVISIBLE
            }
        })
        hideHelp.addUpdateListener {
            val value = hideHelp.animatedValue as Float
            fab_help.alpha = value
        }
        hideHelp.duration = 300
        hideHelp.interpolator = DecelerateInterpolator()
        hideHelp.startDelay = 100

        // Stats button
        val hideStats = ValueAnimator.ofFloat(1f, 0f)
        hideStats.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                fab_stats.visibility = View.INVISIBLE
            }
        })
        hideStats.addUpdateListener {
            val value = hideStats.animatedValue as Float
            fab_stats.alpha = value
        }
        hideStats.duration = 300
        hideStats.interpolator = DecelerateInterpolator()
        hideStats.startDelay = 50

        // Restart button
        val hideRestart = ValueAnimator.ofFloat(1f, 0f)
        hideRestart.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                fab_restart.visibility = View.INVISIBLE
            }
        })
        hideRestart.addUpdateListener {
            val value = hideRestart.animatedValue as Float
            fab_restart.alpha = value
        }
        hideRestart.duration = 300
        hideRestart.interpolator = DecelerateInterpolator()

        // Play all animations together (with given delays)
        val animations = AnimatorSet()
        animations.playTogether(hideHelp, hideStats, hideRestart)
        animations.start()
    }

    fun showWords(view: View) {
        if (wordView.visibility == View.INVISIBLE)
            wordView.visibility = View.VISIBLE
        else
            wordView.visibility = View.INVISIBLE
    }

    /**
     * Change the word view layout between list and count
     */
    private fun changeWordView() {
        viewType = if (viewType == ViewType.List)
            ViewType.Count
        else
            ViewType.List

        wordViewText.text = wordsFound.getWords(viewType)
    }

    /**
     * Add a button with last found word to word feed
     */
    private fun addWordFeedItem(text: String, isTreasure: Boolean = false) {
        // Scale from int to dp
        val scale = resources.displayMetrics.density
        // Move all the buttons down by changing their top margin by 50dp
        for (word in wordFeed) {
            val l = word.layoutParams as ViewGroup.MarginLayoutParams
            var topMargin = l.topMargin
            topMargin += (scale*50 + 0.5).toInt()
            l.setMargins(l.leftMargin, topMargin, l.rightMargin, l.bottomMargin)
            word.layoutParams = l
        }
        // Set new button's layout parameters
        val padding = (10*scale + 0.5).toInt()
        val layout = findViewById<CoordinatorLayout>(R.id.mainMapView)
        val button = Button(this)
        val lp = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.END
        lp.setMargins(0, (scale*60 + 0.5).toInt(), (scale*10 + 0.5).toInt(), 0)
        button.layoutParams = lp
        // Set button's appearance
        // Words have normal background
        if (!isTreasure) {
            button.background = getDrawable(R.drawable.rectangle)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
        }
        // Treasure has special background
        else {
            button.background = getDrawable(R.drawable.treasure_rectangle)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
        }
        button.setPadding(padding, padding, padding, padding)
        // Add an option for the player to see the word he found
        button.setOnClickListener {
            _ ->
            if (button.text == "") {
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                button.text = text
            } else {
                button.text = ""
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
            }
        }
        // Keep track of all the buttons
        wordFeed.add(button)
        layout.addView(button)
        // Make the button disappear after 10 seconds by fading away
        object: CountDownTimer(10000, 10000) {
            override fun onTick(p0: Long) {
            }

            override fun onFinish() {
                val removeButton = ValueAnimator.ofFloat(1f, 0f)
                // Removing button on animation end
                removeButton.addListener(object: Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {}
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        layout.removeView(button)
                        wordFeed.remove(button)
                    }
                })
                // Button fading
                removeButton.addUpdateListener {
                    val value = removeButton.animatedValue as Float
                    button.alpha = value
                }
                removeButton.duration = 500
                removeButton.start()
            }
        }.start()
    }
    //#endregion

    //region Dialogs
    fun showStats(view: View) {
        val text = hashMapOf(
                R.id.statBestScore to lifelongStatistics["Best score"]!!.toString(),
                R.id.statGamesPlayed to lifelongStatistics["Games played"]!!.toString(),
                R.id.statGamesWon to lifelongStatistics["Games won"]!!.toString(),
                R.id.statTimeInGame to getFormattedTime(lifelongStatistics["Total time in game"]!!),
                R.id.statAverageScore to lifelongStatistics["Average score"]!!.toString(),
                R.id.statLastGameScore to lifelongStatistics["Last game score"]!!.toString()
        )

        showDialog(R.layout.dialog_statistics, R.id.mainStatView, texts = text)
    }

    fun showHelp(view: View) {
        val helpString = """
&#8226; Walk up to <font color='#FFC107'>markers</font> <br/>
&#8226; Collect <font color='#FFC107'>words</font> <br/>
&#8226; Guess the <font color='#FFC107'>song</font> <br/>
&#8226; Be <font color='#FFC107'>quick</font> and <font color='#FFC107'>picky</font> <br/>
&#8226; Get the best <font color='#FFC107'>score</font> <br/>
&#8226; If stuck, <font color='#FFC107'>restart</font> <br/>
&#8226; Try to find the <font color='#ff0000'>treasure</font> :)"""

        val text = hashMapOf(
                R.id.helpText to helpString
        )

        showDialog(R.layout.dialog_help, R.id.mainHelpView, texts = text)
    }

    fun showGiveUp(view: View) {
        // Set up the dialog
        val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
        val mView = layoutInflater.inflate(R.layout.dialog_giveup, null)

        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        // If 15 seconds have passed, no longer can change difficulty
        val playAgain = mView.findViewById<Button>(R.id.btnGiveUp)
        if (currentTime/1000 > 15) {
            // Change text, its colour and background
            playAgain.background = getDrawable(R.drawable.no_rectangle)
            playAgain.setTextColor(resources.getColor(R.color.colorNo))
            playAgain.text = getString(R.string.givingUp)
            // Set appropriate padding in dp
            val scale = resources.displayMetrics.density
            val pad = (10*scale + 0.5).toInt()
            playAgain.setPadding(pad, pad, pad, pad)
            // Set onClick listener to show losing dialog
            playAgain.setOnClickListener {
                dialog.dismiss()
                endGame(false)
            }
        }
        else {
            playAgain.setOnClickListener {
                dialog.dismiss()
                clearMap()
                showDifficulty()
            }
        }

        // Change font for the dialog
        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainGiveUpView))

        // Show the dialog
        dialog.show()
        dialog.window.setLayout(600, 380)
    }

    fun showGuessWindow(view: View) {
        // Set up the dialog
        val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
        val mView = layoutInflater.inflate(R.layout.dialog_guess, null)

        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        // If title is not empty, set it
        if (title != "") {
            val viewTitle = TextView(dialog.context)
            viewTitle.typeface = tf
            viewTitle.text = title
            viewTitle.textSize = 24f
            viewTitle.gravity = Gravity.CENTER_HORIZONTAL
            dialog.setCustomTitle(viewTitle)
        }

        val makeGuessButton = mView.findViewById<ImageButton>(R.id.makeGuess)
        val guessText = mView.findViewById<EditText>(R.id.guessText)
        // TODO: This is hacky, I don't really like it
        makeGuessButton.setOnClickListener({
            v ->
            if (makeGuess(v, guessText.text.toString())) {
                dialog.dismiss()
            }
        })

        // Change font for the dialog
        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainGuessView))

        // Show the dialog
        dialog.show()
    }

    fun showTreasure() {
        val texts = hashMapOf(
                R.id.line to treasureLine.joinToString(" "),
                R.id.lineNumber to "[Line " + treasureLineNumber.toString() + "]"
        )

        showDialog(R.layout.dialog_treasure, R.id.mainTreasureView, texts = texts)
    }

    private fun showDifficulty() {
        val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
        val mView = layoutInflater.inflate(R.layout.dialog_difficulty, null)

        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        // Get all difficulty buttons
        val cakewalk = mView.findViewById<Button>(R.id.btnCakewalk)
        val easy = mView.findViewById<Button>(R.id.btnEasy)
        val medium = mView.findViewById<Button>(R.id.btnMedium)
        val hard = mView.findViewById<Button>(R.id.btnHard)
        val vhard = mView.findViewById<Button>(R.id.btnVHard)
        val diffs = arrayOf(cakewalk, easy, medium, hard, vhard)

        // On click listener for all buttons
        val ocl = View.OnClickListener {
            dialog.dismiss()
            chooseDifficulty(it)
        }

        // Set the onClick for all buttons
        for (view in diffs)
            view.setOnClickListener(ocl)

        // Change font for the dialog
        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainDiffView))

        // Show the dialog
        dialog.show()
    }

    private fun showEndGame(win: Boolean, gameScore: Int) {
        // Set up the dialog
        val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
        val mView = layoutInflater.inflate(R.layout.dialog_win, null)

        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        // Set text of all text views
        if (win)
            mView.findViewById<TextView>(R.id.totalGuesses).text = (numberOfWrongGuesses + 1).toString()
        else
            mView.findViewById<TextView>(R.id.totalGuesses).text = numberOfWrongGuesses.toString()
        mView.findViewById<TextView>(R.id.totalPoints).text = gameScore.toString()
        mView.findViewById<TextView>(R.id.totalTime).text = getFormattedTime(currentTime)
        mView.findViewById<TextView>(R.id.songInformation).text = getString(R.string.songInfo,
                currentSong.artist,
                currentSong.title)

        // Change font for the dialog
        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainWinView))

        // If lost game, change the title text to lose
        if (!win) {
            val winText = mView.findViewById<TextView>(R.id.textWinInfo)
            winText.text = getString(R.string.lose)
            winText.setTextColor(resources.getColor(R.color.colorNo))
        }

        // Set the onClick for playAgain and stopPlaying
        val again = mView.findViewById<Button>(R.id.playAgainButton)
        again.setOnClickListener {
            showDifficulty()
            dialog.dismiss()
        }
        // If stop playing, set timer to 0 and close the window
        // TODO: Enable playing again after closing window?
        val stop = mView.findViewById<Button>(R.id.stopPlayingButton)
        stop.setOnClickListener {
            startTime = SystemClock.uptimeMillis()
            currentTime = 0L
            timer.text = getFormattedTime(currentTime)
            handler.removeCallbacks(runnable)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun showDialog(layout: Int,
                           mainView: Int,
                           title: String = "",
                           titleSize: Float = 0f,
                           texts: HashMap<Int, String>? = null) {
        // Set up the dialog
        val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
        val mView = layoutInflater.inflate(layout, null)

        mBuilder.setView(mView)
        val dialog = mBuilder.create()

        // If title is not empty, set it
        if (title != "") {
            val viewTitle = TextView(dialog.context)
            viewTitle.typeface = tf
            viewTitle.text = title
            viewTitle.textSize = titleSize
            viewTitle.gravity = Gravity.CENTER_HORIZONTAL
            dialog.setCustomTitle(viewTitle)
        }

        // Set texts of all textviews
        if (texts != null) {
            for ((id, text) in texts) {
                mView.findViewById<TextView>(id).text = Html.fromHtml(text)
            }
        }

        // Change font for the dialog
        FontChangeCrawler(tf).replaceFonts(mView.findViewById(mainView))

        // Show the dialog
        dialog.show()
    }
    //endregion

    //region Time handling
    var handler = Handler()
    var startTime: Long = 0L
    var currentTime: Long = 0L
    private var runnable = object : Runnable {
        override fun run() {
            currentTime = SystemClock.uptimeMillis() - startTime

            timer.text = getFormattedTime(currentTime)

            // Call this again after a second
            handler.postDelayed(this, 1000)
        }
    }

    /**
     * Returns properly formatted current game time
     */
    private fun getFormattedTime(time: Long) : String {
        val seconds = (time/1000).toInt()
        val minutes = seconds/60
        val hours = seconds/3600

        // Get time either with or without hours
        return if (hours > 0)
            getString(R.string.hourTime,
                    hours.toString(),
                    String.format("%02d", minutes%60),
                    String.format("%02d", seconds%60))
        else
            getString(R.string.minuteTime,
                    String.format("%02d", minutes%60),
                    String.format("%02d", seconds%60))
    }
    //endregion

    //region Scoring
    private fun score(time: Int, wordsNeeded: Int, wordsTotal: Int, difficulty: Difficulty, guesses: Int): Int {
        // Apply scoring functions to arguments
        // T(time)
        val T = when {
            time in 0..180    -> 1.0
            time in 180..1800 -> 100.0/time + 4.0/9
            time > 1800       -> 0.5
            else              -> 0.0
        }
        // W(words)
        val words = (wordsNeeded.toDouble()) / wordsTotal
        val W = when {
            words in 0.0..0.05 -> 1.0
            words in 0.05..0.5 -> 1.0/(36.0*words) + 4.0/9
            words > 0.5        -> 0.5
            else               -> 0.0
        }
        // D(difficulty)
        val D = when (difficulty){
            Difficulty.Cakewalk -> 400
            Difficulty.Easy     -> 500
            Difficulty.Medium   -> 700
            Difficulty.Hard     -> 900
            Difficulty.VeryHard -> 1000
        }
        // G(guesses, difficulty)
        // Any guess after the 1000th is counted the same as 1000th
        val g = Math.min(1000, guesses)
        val G = D / 42.3376 * guessPenalty[g]

        // Score(time, words, difficulty, guesses)
        return (T * W * D - G).toInt()
    }
    //endregion

    // Implementation of Dice string similarity algorithm
    // TODO: Move to separate class
    private fun stringSimilarity(s1: String, s2: String): Double {
        val p1 = HashSet<String>()
        val p2 = HashSet<String>()

        for (i: Int in 0..s1.length-2) {
            p1.add(s1.substring(i, i+2).toLowerCase())
        }

        for (i: Int in 0..s2.length-2) {
            p2.add(s2.substring(i, i+2).toLowerCase())
        }

        return 2.0*(p1.intersect(p2).size) / (p1.size + p2.size)
    }

    fun toMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}