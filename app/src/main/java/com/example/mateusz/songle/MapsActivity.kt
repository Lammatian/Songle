package com.example.mateusz.songle

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.ActionBar
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_statistics.*
import kotlinx.android.synthetic.main.dialog_statistics.view.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by mateusz on 03/11/17.
 * ic_treasure taken from https://openclipart.org/detail/257257/chromatic-musical-notes-typography-no-background
 * ic_idea taken from https://thenounproject.com/term/idea/62335/, work of Takao Umehara
 */

// Map boundaries
private const val minLat = 55.942617
private const val maxLat = 55.946233
private const val minLng = -3.192473
private const val maxLng = -3.184319

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var points: List<MapPoint>
    private lateinit var treasure: Marker
    private lateinit var treasureLoc: Location
    private lateinit var lyrics: List<List<String>>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var guessPenalty: List<Double>
    private lateinit var wordsFound: WordsFound
    private var markerToPoint: HashMap<Marker, MapPoint> = HashMap()
    private lateinit var tf: Typeface
    private var lifelongStatistics: HashMap<String, String> = hashMapOf(
            "Best score" to "0",
            "Games played" to "0",
            "Games won" to "0",
            "Total time in game" to "0:00:00",
            "Average score" to "0",
            "Last game score" to "0"
    )
    private val desToIcon: HashMap<String, Int> = hashMapOf(
            "unclassified" to R.mipmap.wht_blank,
            "boring" to R.mipmap.ylw_blank,
            "notboring" to R.mipmap.ylw_circle,
            "interesting" to R.mipmap.orange_diamond,
            "veryinteresting" to R.mipmap.red_stars)
    private var viewType: ViewType = ViewType.LIST

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

        // Try downloading xml
        //val parsedMap = DownloadXmlTask(Dcl(), false).execute("Map", "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map4.kml")
        //val parsedLyrics = DownloadXmlTask(Dcl(), false).execute("Lyrics", "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/lyrics.txt")

        // Works!
        //lyrics = parsedLyrics.get() as List<List<String>>
        //points = parsedMap.get() as List<MapPoint>
        val lyricsText = """Just one more time before I go
I'll let you know
That all this time I've been afraid
I wouldn't let it show
Nobody can save me now
No
Nobody can save me now

Stars are only visible in darkness
Fear is ever-changing and evolving
And I
I feel poison inside
And I
I feel so alive

Nobody can save me now
The king is crowned
It's do or die
Nobody can save me now
The only sound
Is the battle cry"""

        lyrics = lyricsText.split("\n").map{it.split(" ")}
        points = ArrayList<MapPoint>(0)
        wordsFound = WordsFound(ArrayList(0), hashMapOf())

        wordViewText.text = """Scaramouche x2
come x1
Figaro x1
Galileo x1
Gotta x1
late x1
Thunderbolt x1
truth x1"""

        // Register BroadcastReceiver to track connection changes.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(networkReceiver, filter)

        // Font changing
        tf =  Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        FontChangeCrawler(tf).replaceFonts(this.mainMapView)

        // Shared preferences test
        // TODO: Improve
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()
        editor.putString("Best score", "2000")
        editor.apply()

        // Statistics initialization
        for (stat in lifelongStatistics.keys) {
            if (sharedPreferences.getString(stat, "") != "")
                lifelongStatistics[stat] = sharedPreferences.getString(stat, "")
        }

        // Guess penalty reading and scoring test
        val inputStream = BufferedReader(InputStreamReader(assets.open("helpers/guesspen.txt")))
        var read = inputStream.readText()
        read = read.substring(1, read.length-1)
        guessPenalty = read.split(",").map{it.toDouble()}
        wordViewText.text = score(0, 1, 10, "veryhard", 5).toString()

        // Choose difficulty
        showDialog(R.layout.dialog_difficulty, R.id.mainDiffView)
    }
    //endregion

    //region Map ready
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

        // Add a marker in ~Edinburgh and move the camera
        val edi = LatLng(55.946, -3.1888)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edi))

        // Current position on the map
        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se : SecurityException) {
            println("Security exception thrown [onMapReady]")
        }

        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true
        //mMap.isMyLocationEnabled = true

        // TODO: First if statement doesn't work
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

        // Set listener for click on the button
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                // TODO: Why is checking location permissions here?
                if (ContextCompat.checkSelfPermission(this@MapsActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                        showTreasure()
                        // TODO: Proper implementation
                        wordsFound.addLine(ArrayList(0), 0)
                        return true
                    }

                    // Word found popup dialog with custom style
                    // TODO: Dialog of appropriate width
                    val point = markerToPoint[marker]
                    val text = hashMapOf(
                            R.id.place to "[" + point!!.name.joinToString(",") + "]",
                            R.id.wordFound to lyrics[point.name[0]-1][point.name[1]-1]
                    )

                    showDialog(R.layout.dialog_wordfound, R.id.mainWordView, texts = text)
                    // TODO: Implement properly
                    updateWithNewWord(arrayOf(1, 2).toIntArray())
                    marker.remove()

                    //region Old wordfound dialog code
//                    val mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
//                    val mView = layoutInflater.inflate(R.layout.dialog_wordfound, null)
//
//                    mBuilder.setView(mView)
//                    val dialog = mBuilder.create()
//
//                    // Set word position in the popup
//                    val point = markerToPoint[marker]
//                    mView.findViewById<TextView>(R.id.place).text = "[" + point!!.name.joinToString(",") + "]"
//                    mView.findViewById<TextView>(R.id.wordFound).text = lyrics[point.name[0]-1][point.name[1]-1]
//
//                    marker.remove()
//                    // Set dialog width and height
//                    // TODO: Doesn't work properly with width
//                    // TODO: Additional parameters to showDialog?
//                    var lp = WindowManager.LayoutParams()
//                    lp.copyFrom(dialog.window.attributes)
//                    lp.width = 700
//                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//
//                    // Change font for the view
//                    FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainWordView))
//
//                    dialog.show()
//                    // Attributes have to be changed after showing for some reason
//                    dialog.window.attributes = lp
                    //endregion

                    // TODO: This should spawn a imagebutton in WordFeed
//                    object: CountDownTimer(5000, 5000) {
//
//                        override fun onTick(p0: Long) {
//                            wordView.visibility = View.VISIBLE
//                        }
//
//                        override fun onFinish() {
//                            wordView.visibility = View.INVISIBLE
//                        }
//                    }.start()

                    // TODO: Remove this shit from here to somewhere else
                    if (myLoc.distanceTo(treasureLoc) < 100) {
                        treasure.isVisible = true
                    }
                }

                // Stop the default reaction to clicking a marker
                return true
            }
        })

        // Place the words on the map
        placeWords(points)
    }
    //endregion

    /**
     * Places word icons on the map
     */
    fun placeWords(points: List<MapPoint>) {
        // Place all the word markers on the map
        for (p in points) {
            var marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(p.point[1], p.point[0]))
                    .icon(BitmapDescriptorFactory.fromResource(desToIcon[p.description]!!)))

            markerToPoint[marker] = p
        }

        // Create random position for the treasure marker
        var treasureLat = minLat + Math.random()*(maxLat - minLat)
        var treasureLng = minLng + Math.random()*(maxLng - minLng)

        // Add treasure marker
        treasure = mMap.addMarker(MarkerOptions()
                .position(LatLng(treasureLat, treasureLng))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_treasure))
                .visible(false))

        // Save treasure location for easier management
        treasureLoc = Location("")
        treasureLoc.latitude = treasureLat
        treasureLoc.longitude = treasureLng
    }

    //#region Menu Open/Close
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
        var showHelp = ValueAnimator.ofFloat(0f, 1f)
        showHelp.addUpdateListener {
            val value = showHelp.animatedValue as Float
            fab_help.alpha = value
        }
        showHelp.duration = 300
        showHelp.interpolator = AccelerateInterpolator()

        // Stats button
        var showStats = ValueAnimator.ofFloat(0f, 1f)
        showStats.addUpdateListener {
            val value = showStats.animatedValue as Float
            fab_stats.alpha = value
        }
        showStats.duration = 300
        showStats.interpolator = AccelerateInterpolator()
        showStats.startDelay = 50

        // Restart button
        var showRestart = ValueAnimator.ofFloat(0f, 1f)
        showRestart.addUpdateListener {
            val value = showRestart.animatedValue as Float
            fab_restart.alpha = value
        }
        showRestart.duration = 300
        showRestart.interpolator = AccelerateInterpolator()
        showRestart.startDelay = 100

        // Play all animations together (with given delays)
        var animations = AnimatorSet()
        animations.playTogether(showHelp, showStats, showRestart)
        animations.start()
    }

    private fun closeMenu() {
        // Animations to hide all buttons
        // Help button
        var hideHelp = ValueAnimator.ofFloat(1f, 0f)
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
        var hideStats = ValueAnimator.ofFloat(1f, 0f)
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
        var hideRestart = ValueAnimator.ofFloat(1f, 0f)
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
        var animations = AnimatorSet()
        animations.playTogether(hideHelp, hideStats, hideRestart)
        animations.start()
    }
    //#endregion

    //region Dialogs
    fun showStats(view: View) {
        val text = hashMapOf(
                R.id.statBestScore to lifelongStatistics["Best score"]!!,
                R.id.statGamesPlayed to lifelongStatistics["Games played"]!!,
                R.id.statGamesWon to lifelongStatistics["Games won"]!!,
                R.id.statTimeInGame to lifelongStatistics["Total time in game"]!!,
                R.id.statAverageScore to lifelongStatistics["Average score"]!!,
                R.id.statLastGameScore to lifelongStatistics["Last game score"]!!
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
        showDialog(R.layout.dialog_giveup, R.id.mainGiveUpView)
    }

    fun showGuessWindow(view: View) {
        showDialog(R.layout.dialog_guess, R.id.mainGuessView, "What song is this?", 24f)
    }

    fun showTreasure() {
        showDialog(R.layout.dialog_treasure, R.id.mainTreasureView)
    }

    private fun showDialog(layout: Int, mainView: Int, title: String = "", titleSize: Float = 0f, texts: HashMap<Int, String>? = null) {
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

    fun showWords(view: View) {
        if (wordView.visibility == View.INVISIBLE)
            wordView.visibility = View.VISIBLE
        else
            wordView.visibility = View.INVISIBLE
    }

    fun toMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun makeGuess(view: View) {
        // TODO: Get guessText value somehow
        //val guess = view.findViewById<EditText>(R.id.guessText).selectAll().toString()
        wordViewText.text = "Testing"
    }

    fun changeWordView(view: View) {
        if (viewType == ViewType.LIST)
            viewType = ViewType.COUNT
        else
            viewType = ViewType.LIST

        wordViewText.text = wordsFound.getWords(viewType)
    }

    fun updateWithNewWord(place: IntArray) {
        // Update word found database
        wordsFound.addWord(place)
        // Update text shown to user
        wordViewText.text = wordsFound.getWords(viewType)
    }

    //region Scoring
    fun score(time: Int, wordsNeeded: Int, wordsTotal: Int, difficulty: String, guesses: Int): Int {
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
            "cakewalk" -> 400
            "easy"     -> 500
            "medium"   -> 700
            "hard"     -> 900
            "veryhard" -> 1000
            else       -> 0
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
    fun stringSimilarity(s1: String, s2: String): Double {
        val p1 = HashSet<String>()
        val p2 = HashSet<String>()

        for (i: Int in 0..s1.length-2) {
            p1.add(s1.substring(i, i+2))
        }

        for (i: Int in 0..s2.length-2) {
            p2.add(s2.substring(i, i+2))
        }

        return 2.0*(p1.intersect(p2).size) / (p1.size + p2.size)
    }
}
