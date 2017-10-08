package com.example.mateusz.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testText.text = "Other test"
    }

    public fun btnClick(view : View){
        Log.d("Tag", "OK");
        toMap();
    }

    private fun toMap(){
        var intent = Intent(this, MapsActivity::class.java);
        startActivity(intent);
    }
}
