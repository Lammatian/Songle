package com.example.mateusz.songle

import android.animation.*
import android.app.ActionBar
import android.content.ClipData
import android.content.Intent
import android.graphics.Path
import android.graphics.Typeface
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var count = 0
    var stretched = false
    var menuWrapContent = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Change fonts in views
        val balooFont = Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        val plumpFont = Typeface.createFromAsset(assets, "fonts/Plump.ttf")
        var fontChangeCrawler = FontChangeCrawler(balooFont)
        fontChangeCrawler.replaceFonts(findViewById(android.R.id.content))

        // Get WRAP_CONTENT value for the menu
        menuWrapContent = menuButtons.layoutParams.width

        this.title = "Test"
        //txvTest.typeface = plumpFont

        // Floating Action Button drag drop fun
        var y = 0f
        var y1 = 0f
        var down = true

        fab.setOnTouchListener(object: View.OnTouchListener {
            // Set of conditions to enable movement on pull
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                // If we click on the button, set y to when clicked just once
                if ((event?.action) == MotionEvent.ACTION_DOWN && down) {
                    y = event?.y
                    down = !down
                }
                // If we move the cursor, set y1 to the current movement
                if ((event?.action) == MotionEvent.ACTION_MOVE) {
                    y1 = event?.y
                    btnStretch.text = y.toString()
                }
                // If we stop holding touch down, check if the movement was up or down
                if ((event?.action) == MotionEvent.ACTION_UP) {
                    btnStretch.text = y1.toString()

                    // movement up (because the y-axis increases downwards)
                    if (y1!! < y!!) {
                        fab.animate().y(0f).start()
                        testView1.animate().y(0f - testView1.height + fab.height/2).start()
                        testView2.animate().y(0f + fab.height/2).start()
                    }
                    // movement down
                    else if (y1!! > y!!) {
                        fab.animate().y(800f).start()
                        testView1.animate().y(800f - testView1.height + fab.height/2).start()
                        testView2.animate().y(800f + fab.height/2).start()
                    }
                    down = !down
                }
                // To enable repeating actions
                return true
            }
        })
    }

    // Show or hide top menu along with its buttons
    fun btnMenu(view: View) {
        if (!stretched) {
            showMenu()
        }
        else {
            hideMenu()
        }

        stretched = !stretched
    }

    // Show the top menu and all the buttons inside
    fun showMenu() {
        var animatorSet = AnimatorSet()

        // Stretch menu
        // TODO: Adjust starting and ending width better
        var animatorStretchMenu = ValueAnimator.ofInt(btnStretch.layoutParams.width + 60, 800)
        animatorStretchMenu.addUpdateListener {
            val value = animatorStretchMenu.animatedValue as Int
            var lp = menuButtons.layoutParams
            lp.width = value
            menuButtons.layoutParams = lp
            menuButtons.requestLayout()
        }
        animatorStretchMenu.duration = 300
        animatorStretchMenu.interpolator = AccelerateInterpolator()

        // Animations to show buttons
        // First, set visibility to visible and opacity to 0
        btnStats.alpha = 0f
        btnHelp.alpha = 0f
        btnRestart.alpha = 0f
        btnStats.visibility = View.VISIBLE
        btnHelp.visibility = View.VISIBLE
        btnRestart.visibility = View.VISIBLE

        // Stats button
        var showStats = ValueAnimator.ofFloat(btnStats.alpha, 0f, 1f)
        showStats.addUpdateListener{
            val value = showStats.animatedValue as Float
            btnStats.alpha = value
        }
        showStats.duration = 300
        showStats.interpolator = AccelerateInterpolator()
        showStats.startDelay = 50

        // Help button
        var showHelp = ValueAnimator.ofFloat(btnHelp.alpha, 0f, 1f)
        showHelp.addUpdateListener{
            val value = showHelp.animatedValue as Float
            btnHelp.alpha = value
        }
        showHelp.duration = 300
        showHelp.interpolator = AccelerateInterpolator()
        showHelp.startDelay = 100

        // Restart button
        var showRestart = ValueAnimator.ofFloat(btnRestart.alpha, 0f, 1f)
        showRestart.addUpdateListener{
            val value = showRestart.animatedValue as Float
            btnRestart.alpha = value
        }
        showRestart.duration = 300
        showRestart.interpolator = AccelerateInterpolator()
        showRestart.startDelay = 150

        // Play all animations together (TODO: Add offset)
        animatorSet.playTogether(animatorStretchMenu, showStats, showHelp, showRestart)
        animatorSet.start()
    }

    // Hide the menu and all the buttons inside
    // TODO: Bug when closing and opening the menu quickly
    fun hideMenu() {
        var animatorSet = AnimatorSet()

        // Animation to shrink menu
        // TODO: Adjust starting width better
        var shrinkMenu = ValueAnimator.ofInt(menuButtons.layoutParams.width, btnStretch.layoutParams.width + 60)
        shrinkMenu.addUpdateListener {
            val value = shrinkMenu.animatedValue as Int
            var lp = menuButtons.layoutParams
            lp.width = value
            menuButtons.layoutParams = lp
        }
        shrinkMenu.duration = 300
        shrinkMenu.startDelay = 100
        shrinkMenu.interpolator = AccelerateInterpolator()

        // Animations to hide buttons
        // Hide help button
        var hideRestart = ValueAnimator.ofFloat(btnRestart.alpha, btnRestart.alpha, 0f)
        hideRestart.addUpdateListener {
            val value = hideRestart.animatedValue as Float
            btnRestart.alpha = value
        }
        hideRestart.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                btnRestart.visibility = View.GONE
            }
        })
        hideRestart.duration = 300
        hideRestart.interpolator = DecelerateInterpolator()

        // Hide help button
        var hideHelp = ValueAnimator.ofFloat(btnHelp.alpha, btnHelp.alpha, 0f)
        hideHelp.addUpdateListener {
            val value = hideHelp.animatedValue as Float
            btnHelp.alpha = value
        }
        hideHelp.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                btnHelp.visibility = View.GONE
            }
        })
        hideHelp.duration = 300
        hideHelp.startDelay = 75
        hideHelp.interpolator = DecelerateInterpolator()

        // Hide stats button
        var hideStats = ValueAnimator.ofFloat(btnStats.alpha, btnStats.alpha, 0f)
        hideStats.addUpdateListener {
            val value = hideStats.animatedValue as Float
            btnStats.alpha = value
        }
        hideStats.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                btnStats.visibility = View.GONE
            }
        })
        hideStats.duration = 300
        hideStats.startDelay = 150
        hideStats.interpolator = DecelerateInterpolator()

        // Play animations together
        animatorSet.playTogether(shrinkMenu, hideStats, hideHelp, hideRestart)
        animatorSet.start()
    }

    // Go to map; just for testing
    fun btnToMap(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
