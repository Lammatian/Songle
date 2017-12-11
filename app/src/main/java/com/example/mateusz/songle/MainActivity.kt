package com.example.mateusz.songle

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewPager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.CorrectionInfo
import android.widget.*
import java.lang.Integer.max


class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private lateinit var buttons: ArrayList<Button>
    private var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        viewPager = findViewById(R.id.viewpager)
        setupViewPager(viewPager)

        tabLayout = findViewById(R.id.tabs)
        tabLayout!!.setupWithViewPager(viewPager)*/
        buttons = ArrayList()

        val btnPress = findViewById<Button>(R.id.press)
        btnPress.setOnClickListener {
            _ ->
            val scale = resources.displayMetrics.density
            // Move all the buttons down
            for (button in buttons) {
                val l = button.layoutParams as ViewGroup.MarginLayoutParams
                var topMargin = l.topMargin
                topMargin += (scale*50 + 0.5).toInt()
                l.setMargins(l.leftMargin, topMargin, l.rightMargin, l.bottomMargin)
                button.layoutParams = l
            }
            val padding = (10*scale + 0.5).toInt()
            val layout = findViewById<CoordinatorLayout>(R.id.mainView)
            val button = Button(this)
            val lp = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.END
            lp.setMargins(0, (scale*60 + 0.5).toInt(), (scale*10 + 0.5).toInt(), 0)
            button.layoutParams = lp
            button.background = getDrawable(R.drawable.rectangle)
            button.setPadding(padding, padding, padding, padding)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
            button.setOnClickListener {
                _ ->
                if (button.text == "") {
                    button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    button.text = "A word here"
                } else {
                    button.text = ""
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
                }
            }
            buttons.add(button)
            layout.addView(button)
            counter += 1
        }

        val btnAnimate = findViewById<Button>(R.id.animate)
        btnAnimate.setOnClickListener {
            _ ->
            val scale = resources.displayMetrics.density
            // Move all the buttons down
            for (button in buttons) {
                val l = button.layoutParams as ViewGroup.MarginLayoutParams
                var topMargin = l.topMargin
                topMargin += (scale*50 + 0.5).toInt()
                l.setMargins(l.leftMargin, topMargin, l.rightMargin, l.bottomMargin)
                button.layoutParams = l
            }
            val padding = (10*scale + 0.5).toInt()
            val layout = findViewById<CoordinatorLayout>(R.id.mainView)
            val button = Button(this)
            val lp = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.END
            lp.setMargins(0, (scale*60 + 0.5).toInt(), (scale*10 + 0.5).toInt(), 0)
            button.layoutParams = lp
            button.background = getDrawable(R.drawable.rectangle)
            button.setPadding(padding, padding, padding, padding)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
            button.setOnClickListener {
                _ ->
                if (button.text == "") {
                    button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    button.text = "A word here"
                } else {
                    button.text = ""
                    button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audiotrack_black_24px, 0, 0, 0)
                }
            }
            buttons.add(button)
            layout.addView(button)
            counter += 1
            object: CountDownTimer(10000, 10000) {
                override fun onTick(p0: Long) {
                }

                override fun onFinish() {
                    val removeButton = ValueAnimator.ofFloat(1f, 0f)
                    removeButton.addListener(object: Animator.AnimatorListener {
                        override fun onAnimationStart(p0: Animator?) {}
                        override fun onAnimationCancel(p0: Animator?) {}
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {
                            layout.removeView(button)
                            buttons.remove(button)
                            counter -= 1
                        }
                    })
                    removeButton.addUpdateListener {
                        val value = removeButton.animatedValue as Float
                        button.alpha = value
                    }
                    removeButton.duration = 500
                    removeButton.start()
                }
            }.start()
        }
    }

    /*fun setupViewPager(viewPager: ViewPager?) {
        var adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(FragmentTest(), "ONE")
        adapter.addFragment(FragmentTest(), "TWO")
        adapter.addFragment(FragmentTest(), "THREE")
        viewPager!!.adapter = adapter
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }*/
}
