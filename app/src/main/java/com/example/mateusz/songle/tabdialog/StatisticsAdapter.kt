package com.example.mateusz.songle.tabdialog

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mateusz.songle.PastGameInfo
import com.example.mateusz.songle.R

class StatisticsAdapter(context: Context,
                        private val values: ArrayList<PastGameInfo>,
                        private val tf: Typeface) : ArrayAdapter<PastGameInfo>(context, -1, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.game_info, parent, false)

        val textDate = rowView.findViewById<TextView>(R.id.gameDate)
        val textTitle = rowView.findViewById<TextView>(R.id.gameSongInfo)
        val textTime = rowView.findViewById<TextView>(R.id.gameTime)
        val textPoints = rowView.findViewById<TextView>(R.id.gamePoints)
        val textGuesses = rowView.findViewById<TextView>(R.id.gameGuesses)

        val pastGame = values[position]
        textDate.text = pastGame.date
        textDate.typeface = tf
        textTitle.text = pastGame.song
        textTitle.typeface = tf
        textTime.text = pastGame.gametime
        textTime.typeface = tf
        textPoints.text = pastGame.points.toString()
        textPoints.typeface = tf
        textGuesses.text = pastGame.guesses.toString()
        textGuesses.typeface = tf

        // Assume 0 points means a lose
        if (pastGame.points == 0) {
            rowView.setBackgroundResource(R.color.colorLoseHighlight)
            val color = parent!!.resources.getColor(R.color.colorMainFAB)
            textTime.setTextColor(color)
            textPoints.setTextColor(color)
            textGuesses.setTextColor(color)
        }
        else
            rowView.setBackgroundResource(R.color.colorWinHighlight)

        return rowView
    }
}