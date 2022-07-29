package com.AERYZ.treasurefind.main.entry_point.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.AERYZ.treasurefind.R

class OnboardingAdapter(private var context: Context, private var onBoardingDataList: List<OnboardingData>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return onBoardingDataList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = LayoutInflater.from(context).inflate(R.layout.screen_onboarding, null)
        val imageView: ImageView
        val title: TextView
        val desc: TextView

        imageView = view.findViewById(R.id.image)
        title = view.findViewById(R.id.title)
        desc = view.findViewById(R.id.description)

        imageView.setImageResource((onBoardingDataList[position].imageUrl))
        title.text = onBoardingDataList[position].title
        desc.text = onBoardingDataList[position].description
        container.addView(view)
        return view
    }
}