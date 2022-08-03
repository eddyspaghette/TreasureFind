package com.AERYZ.treasurefind.main.ui.hider_place

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.AERYZ.treasurefind.R

class HiderPlaceFragment : Fragment() {

    companion object {
        fun newInstance() = HiderPlaceFragment()
    }

    private lateinit var viewModel: HiderPlaceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hider_place, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HiderPlaceViewModel::class.java)
        // TODO: Use the ViewModel
    }

}