package com.AERYZ.treasurefind.main.ui.Treasures

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentTreasureBinding
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.ui.map.MapsActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TreasureFragment : Fragment() {

    private var _binding: FragmentTreasureBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val treasureViewModel =
            ViewModelProvider(this).get(TreasureViewModel::class.java)

        _binding = FragmentTreasureBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btn_mapsActivity: Button = root.findViewById(R.id.btn_map)
        btn_mapsActivity.setOnClickListener() {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}