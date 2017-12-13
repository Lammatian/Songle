package com.example.mateusz.songle.tabdialog

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mateusz.songle.*

class TabbedDialog : DialogFragment() {

    private lateinit var lifelong: HashMap<Int, String>
    private lateinit var individual: ArrayList<PastGameInfo>

    fun set(ll: HashMap<Int, String>, ind: ArrayList<PastGameInfo>) {
        lifelong = ll
        individual = ind
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mView = inflater!!.inflate(R.layout.statistics_tabs, container, false)
        val tabLayout = mView.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = mView.findViewById<ViewPager>(R.id.masterViewPager)
        val tabAdapter = StatisticsTabsAdapter(childFragmentManager)
        tabAdapter.addFragment("Lifelong", LifelongStatisticsFragment().createInstance(lifelong))
        tabAdapter.addFragment("Individual", GameStatisticsFragment().createInstance(individual))
        viewPager.adapter = tabAdapter
        tabLayout.setupWithViewPager(viewPager)
        return mView
    }
}