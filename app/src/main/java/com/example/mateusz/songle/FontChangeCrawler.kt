package com.example.mateusz.songle

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class FontChangeCrawler(tf: Typeface){
    var typeface = tf

    // Replace font of all TextViews (text and buttons) to a given font recursively
    // Doesn't include ListViews
    fun replaceFonts(viewTree: ViewGroup) {
        var child: View

        for(i in 0 until viewTree.childCount){
            child = viewTree.getChildAt(i)

            if (child is ViewGroup){
                // recursive call
                replaceFonts(child)
            }
            else if (child is TextView){
                child.typeface = typeface
            }
        }
    }
}