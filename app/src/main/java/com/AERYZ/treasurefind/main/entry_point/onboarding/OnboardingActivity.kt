package com.AERYZ.treasurefind.main.entry_point.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.entry_point.MainActivity
import com.google.android.material.tabs.TabLayout

class OnboardingActivity: AppCompatActivity() {
    var onboardingAdapter: OnboardingAdapter? = null
    var tabLayout: TabLayout? = null
    var onboardingViewPager: ViewPager? = null
    var skip: TextView? = null
    var next: TextView? = null
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        tabLayout = findViewById(R.id.tabLayout)
        skip = findViewById(R.id.onboardSkip)
        next = findViewById(R.id.onboardNext)

        val onboardingData:MutableList<OnboardingData> = ArrayList()

        onboardingData.add(OnboardingData("title1", "desc1", R.drawable.pic1))
        onboardingData.add(OnboardingData("title2", "desc2", R.drawable.pic2))
        onboardingData.add(OnboardingData("title3", "desc3", R.drawable.pic3))

        setOnboardingAdapter(onboardingData)

        skip?.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        position = onboardingViewPager!!.currentItem
        next?.setOnClickListener {
            if (position < onboardingData.size) {
                position++
                onboardingViewPager!!.currentItem = position
            }
            if (position == onboardingData.size) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                position = tab!!.position
                if (tab.position == onboardingData.size - 1) {
                    next!!.text = "GET STARTED"
                }
                else
                    next!!.text = "NEXT"
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun setOnboardingAdapter(onboardingData: List<OnboardingData>) {
        onboardingViewPager = findViewById(R.id.pager)
        onboardingAdapter = OnboardingAdapter(this, onboardingData)
        onboardingViewPager!!.adapter = onboardingAdapter
        tabLayout?.setupWithViewPager(onboardingViewPager)
    }
}