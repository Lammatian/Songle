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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_statistics.view.*

/**
 * Created by mateusz on 03/11/17.
 * ic_treasure taken from https://openclipart.org/detail/257257/chromatic-musical-notes-typography-no-background
 * ic_idea taken from https://thenounproject.com/term/idea/62335/, work of Takao Umehara
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var points: List<MapPoint>
    private lateinit var treasure: Marker
    private lateinit var treasureLoc: Location
    private var markerToPoint: HashMap<Marker, MapPoint> = HashMap()
    private lateinit var tf: Typeface
    private val desToIcon: HashMap<String, Int> = hashMapOf(
            "unclassified" to R.mipmap.wht_blank,
            "boring" to R.mipmap.ylw_blank,
            "notboring" to R.mipmap.ylw_circle,
            "interesting" to R.mipmap.orange_diamond,
            "veryinteresting" to R.mipmap.red_stars)

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

    class dcl : DownloadCompleteListener {
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
        val parsedMap = DownloadXmlTask(dcl(), false).execute("Map", "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map4.kml")
        val parsedLyrics = DownloadXmlTask(dcl(), false).execute("Lyrics", "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/lyrics.txt")

        // Works!
        points = parsedMap.get() as List<MapPoint>
        testText.text = parsedLyrics.get().toString()

        // Register BroadcastReceiver to track connection changes.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(networkReceiver, filter)

        // Font changing
        tf =  Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        FontChangeCrawler(tf).replaceFonts(this.mainMapView)

        // Choose difficulty
        var mBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        var mView = layoutInflater.inflate(R.layout.dialog_difficulty, null)

        mBuilder.setView(mView)
        var dialog = mBuilder.create()

        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainDiffView))

        dialog.show()
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
        mMap.addMarker(MarkerOptions()
                .position(edi)
                .title("Marker in Edi")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.red_stars)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edi))

        try {
            // Visualise current position with a small blue circle
            mMap.isMyLocationEnabled = true
        } catch (se : SecurityException) {
            println("Security exception thrown [onMapReady]")
        }

        // Add ”My location” button to the user interface
        mMap.uiSettings.isMyLocationButtonEnabled = true

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
                if (ContextCompat.checkSelfPermission(this@MapsActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // Get location of player and marker
                    // TODO: Get proper location of the player
                    var myLoc = Location("")
                    myLoc.latitude = edi.latitude
                    myLoc.longitude = edi.longitude
                    var markerLoc = Location("")
                    markerLoc.latitude = marker.position.latitude
                    markerLoc.longitude = marker.position.longitude

                    // 10 meters from the marker to pick it up for now
                    // TODO: Distance ignored for now
                    //if (myLoc.distanceTo(markerLoc) < 10) {
                    // Word found popup dialog with custom style
                    var mBuilder = AlertDialog.Builder(this@MapsActivity, R.style.CustomAlertDialog)
                    var mView = layoutInflater.inflate(R.layout.dialog_wordfound, null)

                    mBuilder.setView(mView)
                    var dialog = mBuilder.create()

                    // Popup title
                    var title = TextView(dialog.context)
                    title.typeface = tf
                    title.text = "You found a new word!"
                    title.textSize = 20f
                    title.gravity = Gravity.CENTER_HORIZONTAL
                    dialog.setCustomTitle(title)

                    // Set word place in the popup
                    mView.findViewById<TextView>(R.id.lineNumber).text = "35"
                    mView.findViewById<TextView>(R.id.placeNumber).text = "1"
                    mView.findViewById<TextView>(R.id.wordFound).text = "Scaramouche"

                    // Set dialog width and height
                    // TODO: Doesn't work properly with width
                    var lp = WindowManager.LayoutParams()
                    lp.copyFrom(dialog.window.attributes)
                    lp.width = 600
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT

                    // Change font for the view
                    FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainWordView))

                    dialog.show()
                    // Attributes have to be changed after showing for some reason
                    dialog.window.attributes = lp
                    //}

                    // TODO: This should spawn a imagebutton it WordFeed
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

                    // TODO: Remove this shit from here
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
        for (p in points) {
            var marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(p.point[1], p.point[0]))
                    .icon(BitmapDescriptorFactory.fromResource(desToIcon[p.description]!!)))

            markerToPoint[marker] = p
        }

        treasure = mMap.addMarker(MarkerOptions()
                .position(LatLng(points[0].point[1] + 0.001, points[0].point[0] + 0.001))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_treasure))
                .visible(false))

        treasureLoc = Location("")
        treasureLoc.latitude = treasure.position.latitude
        treasureLoc.longitude = treasure.position.longitude
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

    fun showStats(view: View) {
        var mBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        var mView = layoutInflater.inflate(R.layout.dialog_statistics, null)

        mBuilder.setView(mView)
        var dialog = mBuilder.create()

        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainStatView))

        dialog.show()
    }

    fun showHelp(view: View) {
        var mBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        var mView = layoutInflater.inflate(R.layout.dialog_help, null)

        var helpString = """
&#8226; Walk up to <font color='#FFC107'>markers</font> <br/>
&#8226; Collect <font color='#FFC107'>words</font> <br/>
&#8226; Guess the <font color='#FFC107'>song</font> <br/>
&#8226; Be <font color='#FFC107'>quick</font> and <font color='#FFC107'>picky</font> <br/>
&#8226; Get the best <font color='#FFC107'>score</font> <br/>
&#8226; If stuck, <font color='#FFC107'>restart</font> <br/>
&#8226; Try to find the <font color='#ff0000'>treasure</font> :)"""

        mView.findViewById<TextView>(R.id.helpText).text = Html.fromHtml(helpString)

        mBuilder.setView(mView)
        var dialog = mBuilder.create()

        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainHelpView))

        dialog.show()
    }

    fun showGiveUp(view: View) {
        var mBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        var mView = layoutInflater.inflate(R.layout.dialog_giveup, null)

        mBuilder.setView(mView)
        var dialog = mBuilder.create()

        FontChangeCrawler(tf).replaceFonts(mView.findViewById(R.id.mainGiveUpView))

        dialog.show()
    }
}
