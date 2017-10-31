package com.example.mateusz.songle

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
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

        this.title = "Test"

        // Floating Action Button open menu


        //txvTest.typeface = plumpFont

        // Floating Action Button drag drop fun
//        var y = 0f
//        var y1 = 0f
//        var down = true
//
//        fab.setOnTouchListener(object: View.OnTouchListener {
//            // Set of conditions to enable movement on pull
//            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
//                // If we click on the button, set y to when clicked just once
//                if ((event?.action) == MotionEvent.ACTION_DOWN && down) {
//                    y = event?.y
//                    down = !down
//                }
//                // If we move the cursor, set y1 to the current movement
//                if ((event?.action) == MotionEvent.ACTION_MOVE) {
//                    y1 = event?.y
//                    btnStretch.text = y.toString()
//                }
//                // If we stop holding touch down, check if the movement was up or down
//                if ((event?.action) == MotionEvent.ACTION_UP) {
//                    btnStretch.text = y1.toString()
//
//                    // movement up (because the y-axis increases downwards)
//                    if (y1!! < y!!) {
//                        fab.animate().y(0f).start()
//                        testView1.animate().y(0f - testView1.height + fab.height/2).start()
//                        testView2.animate().y(0f + fab.height/2).start()
//                    }
//                    // movement down
//                    else if (y1!! > y!!) {
//                        fab.animate().y(800f).start()
//                        testView1.animate().y(800f - testView1.height + fab.height/2).start()
//                        testView2.animate().y(800f + fab.height/2).start()
//                    }
//                    down = !down
//                }
//                // To enable repeating actions
//                return true
//            }
//        })
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

    fun changeText1(view: View) {
        testText.text = "help"
    }

    fun changeText2(view: View) {
        testText.text = "stats"
    }

    fun changeText3(view: View) {
        testText.text = "restart"
    }
}
