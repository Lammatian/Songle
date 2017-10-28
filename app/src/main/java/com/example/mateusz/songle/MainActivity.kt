package com.example.mateusz.songle

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Path
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var count = 0
    var stretched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val balooFont = Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        val plumpFont = Typeface.createFromAsset(assets, "fonts/Plump.ttf")
        var fontChangeCrawler = FontChangeCrawler(balooFont)
        fontChangeCrawler.replaceFonts(findViewById(android.R.id.content))

        this.title = "Test"
        testText.typeface = plumpFont
    }

    fun btnStartGame(view: View){
        btnStart.text = "Changed text!"
        btnStart.elevation = 100F
        count += 1
        txvCount.text = "Clicks: " + String.format("%d", count)
    }

    fun btnShowStats(view: View){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun btnShowHelp(view: View) {
        val path = Path()
        //path.addCircle(txvTest.x-100f, txvTest.y, 100f, Path.Direction.CW)
        path.moveTo(20f, 20f)
        //var animator = ObjectAnimator.ofFloat(txvTest, "x", txvTest.x, txvTest.x+100f).start()
        //var animator2 = ObjectAnimator.ofFloat(txvTest, TextView.X, TextView.Y, path).start()
        //ObjectAnimator.ofFloat(fab, FloatingActionButton.X, FloatingActionButton.Y, path).start()
        var animator = ValueAnimator.ofInt(btnStats.width, btnStats.width + 150)
        animator.addUpdateListener {
            val value = animator.animatedValue as Int
            var lp = btnStats.layoutParams
            lp.width = value
            btnStats.layoutParams = lp
        }
        animator.duration = 1000
        animator.interpolator = AccelerateInterpolator()
        animator.start()
//        animator.addUpdateListener{
//            val value = it.animatedValue as Float
//            txvTest.translationY = value
//            it.interpolator = LinearInterpolator()
//            it.duration = 300
//        }
//        animator.start()
    }

    fun fabChangeView(view: View){
        var animator : ValueAnimator = if (!stretched) ValueAnimator.ofInt(txvStretch.width, txvStretch.width + 500) else ValueAnimator.ofInt(txvStretch.width, txvStretch.width - 500)
        animator.addUpdateListener {
            val value = animator.animatedValue as Int
            var lp = txvStretch.layoutParams
            lp.width = value
            txvStretch.layoutParams = lp
        }
        animator.duration = 300
        animator.interpolator = AccelerateInterpolator()
        animator.start()
        stretched = !stretched
    }
}
