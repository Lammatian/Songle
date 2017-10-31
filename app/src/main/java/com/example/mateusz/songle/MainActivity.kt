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

        this.title = "Test"
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
}
