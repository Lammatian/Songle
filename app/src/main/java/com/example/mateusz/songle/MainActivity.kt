package com.example.mateusz.songle

import android.animation.*
import android.content.Intent
import android.graphics.Path
import android.graphics.Typeface
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
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
    }

    fun fabChangeView(view: View){
        btnStats.alpha = 0f
        btnStats.visibility = View.VISIBLE

        var animator = ValueAnimator.ofFloat(btnStats.alpha, 0f, 1f)
        animator.addUpdateListener{
            val value = animator.animatedValue as Float
            btnStats.alpha = value
        }
        animator.duration = 300
        animator.interpolator = LinearInterpolator()
        animator.start()
        stretched = !stretched
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
        btnStats.alpha = 1f
        btnHelp.alpha = 1f
        btnRestart.alpha = 1f

        // Hide help button
        var hideRestart = ValueAnimator.ofFloat(btnRestart.alpha, 1f, 0f)
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
        var hideHelp = ValueAnimator.ofFloat(btnHelp.alpha, 1f, 0f)
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
        var hideStats = ValueAnimator.ofFloat(btnStats.alpha, 1f, 0f)
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
}
