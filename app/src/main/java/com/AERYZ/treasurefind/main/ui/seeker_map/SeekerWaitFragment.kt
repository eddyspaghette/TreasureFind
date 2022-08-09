package com.AERYZ.treasurefind.main.ui.seeker_map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.AERYZ.treasurefind.R

/* Loading screen once submit request is sent */
class SeekerWaitFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_seeker_wait, container, false)
        return view
    }

}