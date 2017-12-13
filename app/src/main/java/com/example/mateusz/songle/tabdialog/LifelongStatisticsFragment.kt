package com.example.mateusz.songle.tabdialog

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mateusz.songle.FontChangeCrawler
import com.example.mateusz.songle.R

class LifelongStatisticsFragment : Fragment() {
    private lateinit var texts: HashMap<Int, String>

    fun createInstance(texts: HashMap<Int, String>) : LifelongStatisticsFragment {
        val fragment = LifelongStatisticsFragment()
        fragment.texts = texts
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.dialog_statistics, container, false)
        val tf = Typeface.createFromAsset(context.assets, "fonts/Baloo.ttf")
        FontChangeCrawler(tf).replaceFonts(v as ViewGroup)

        for ((id, text) in texts) {
            (v.findViewById<TextView>(id)).text = text
        }

        return v
    }
}