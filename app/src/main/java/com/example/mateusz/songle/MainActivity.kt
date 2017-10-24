package com.example.mateusz.songle

import android.content.Intent
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
        val baloo_font = Typeface.createFromAsset(assets, "fonts/Baloo.ttf")
        var font_change_crawler = FontChangeCrawler(baloo_font)
        font_change_crawler.replaceFonts(findViewById(android.R.id.content))
        //testText.typeface = baloo_font

        this.title = "Test"
        testText.text = "Still testing"
    }

    fun btnClick(view: View){
        btnTest.text = "Changed text!"
        btnTest.elevation = 100F
        count += 1
        txvCount.text = "Clicks: " + String.format("%d", count)
    }

    fun btnToMap(view: View){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
