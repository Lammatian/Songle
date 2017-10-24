package com.example.mateusz.songle

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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

    }
}
