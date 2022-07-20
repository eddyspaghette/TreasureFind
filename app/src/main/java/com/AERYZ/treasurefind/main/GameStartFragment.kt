package com.AERYZ.treasurefind.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.AERYZ.treasurefind.R

/**
 * A simple [Fragment] subclass.
 * Use the [GameStartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameStartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_start, container, false)
    }
}