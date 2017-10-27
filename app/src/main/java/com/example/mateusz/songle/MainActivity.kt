package com.example.mateusz.songle

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.ViewAnimator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var count = 0

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

    fun btnShowHelp(view: View){
        val path : Path = Path()
        path.addCircle(txvTest.x-100f, txvTest.y, 100f, Path.Direction.CW)
        //var animator = ObjectAnimator.ofFloat(txvTest, "x", txvTest.x, txvTest.x+100f).start()
        var animator2 = ObjectAnimator.ofFloat(txvTest, TextView.X, TextView.Y, path).start()
//        animator.addUpdateListener{
//            val value = it.animatedValue as Float
//            txvTest.translationY = value
//            it.interpolator = LinearInterpolator()
//            it.duration = 300
//        }
//        animator.start()
    }
}
