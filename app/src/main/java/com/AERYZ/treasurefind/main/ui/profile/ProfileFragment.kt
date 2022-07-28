package com.AERYZ.treasurefind.main.ui.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.AERYZ.treasurefind.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private val tabNames = arrayOf("Own","Found")
    private lateinit var tabLayoutMediator : TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val tabLayout: TabLayout = view.findViewById(R.id.profile_tablayout)
        val viewPager2: ViewPager2 = view.findViewById(R.id.profile_viewpager2)


        val list_fragments: ArrayList<Fragment> = ArrayList()
        list_fragments.add(OwnTreasureFragment())
        list_fragments.add(FoundTreasureFragment())

        viewPager2.adapter = ProfileFragmentStateAdapter(requireActivity(), list_fragments)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy {
                tab: TabLayout.Tab, position: Int ->
            tab.text = tabNames[position]
        }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }

}