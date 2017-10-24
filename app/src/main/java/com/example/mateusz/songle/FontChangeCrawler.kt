package com.example.mateusz.songle

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by mateusz on 24/10/17.
 */
class FontChangeCrawler(var typeface: Typeface){
    var _typeface = typeface;

    fun replaceFonts(viewTree: ViewGroup) {
        var child: View

        for(i in 0 until viewTree.childCount){
            child = viewTree.getChildAt(i)

            if (child is ViewGroup){
                // recursive call
                replaceFonts(child)
            }
            else if (child is TextView){
                child.setTypeface(_typeface)
            }
        }
    }
}