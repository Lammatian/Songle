package com.example.mateusz.songle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton

class WordFeedAdapter(private val cont: Context,
                      private val textViewResourceId: Int,
                      private val objects: List<String>) : ArrayAdapter<String>(cont, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val wordfeeditem = inflater.inflate(R.layout.wordfeed_item, parent, false)
        val button = wordfeeditem.findViewById<Button>(R.id.item)
        button.setOnClickListener {
            _ ->
            if (button.text == "") {
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                button.text = objects[position]
            }
            else {
                button.text = ""
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
            }
        }

        return wordfeeditem
    }
}