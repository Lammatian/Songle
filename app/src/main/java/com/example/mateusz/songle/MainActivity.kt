package com.example.mateusz.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

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
        val listView = findViewById<ListView>(R.id.lv)
        val values = arrayListOf("One", "Two", "Three")
        val adapter = WordFeedAdapter(this, R.layout.dummy_item, values)
        listView.adapter = adapter
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
