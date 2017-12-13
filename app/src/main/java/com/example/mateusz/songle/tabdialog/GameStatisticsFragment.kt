package com.example.mateusz.songle.tabdialog

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.mateusz.songle.FontChangeCrawler
import com.example.mateusz.songle.PastGameInfo
import com.example.mateusz.songle.R

class GameStatisticsFragment : Fragment() {

    private lateinit var pastGames: ArrayList<PastGameInfo>

    fun createInstance(pg: ArrayList<PastGameInfo>) : GameStatisticsFragment {
        val fragment = GameStatisticsFragment()
        fragment.pastGames = pg
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.statistics_games, container, false)
        val tf = Typeface.createFromAsset(context.assets, "fonts/Baloo.ttf")
        val lview = v.findViewById<ListView>(R.id.gamesList)
        val adapter = StatisticsAdapter(context, pastGames, tf)
        lview.adapter = adapter
        FontChangeCrawler(tf).replaceFonts(v as ViewGroup)
        return v
    }
}