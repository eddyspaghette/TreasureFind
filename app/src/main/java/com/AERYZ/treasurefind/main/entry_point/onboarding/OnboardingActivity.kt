package com.AERYZ.treasurefind.main.entry_point.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

        val onboardingData:MutableList<OnboardingData> = ArrayList()

        onboardingData.add(OnboardingData("title1", "desc1", R.drawable.pic1))
        onboardingData.add(OnboardingData("title2", "desc2", R.drawable.pic2))
        onboardingData.add(OnboardingData("title3", "desc3", R.drawable.pic3))

        setOnboardingAdapter(onboardingData)
    }

    private fun setOnboardingAdapter(onboardingData: List<OnboardingData>) {
        onboardingViewPager = findViewById(R.id.pager)
        onboardingAdapter = OnboardingAdapter(this, onboardingData)
        onboardingViewPager!!.adapter = onboardingAdapter
        tabLayout?.setupWithViewPager(onboardingViewPager)

    }
}