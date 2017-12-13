package com.example.mateusz.songle.tabdialog

import android.support.v4.app.FragmentManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter

class StatisticsTabsAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {

    private var mFragmentCollection: MutableList<Fragment> = ArrayList()
    private var mTitleCollection: MutableList<String> = ArrayList()

    fun addFragment(title: String, fragment: Fragment) {
        mTitleCollection.add(title)
        mFragmentCollection.add(fragment)
    }

    //Needed for
    override fun getPageTitle(position: Int): CharSequence {
        return mTitleCollection[position]
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentCollection[position]
    }

    override fun getCount(): Int {
        return mFragmentCollection.size
    }
}