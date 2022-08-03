package com.AERYZ.treasurefind.main.ui.test

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.databinding.FragmentTestBinding
import com.AERYZ.treasurefind.main.ui.map.MapsActivity
import com.AERYZ.treasurefind.main.ui.sr.SRActivity

class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTestBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Map Activity button
        val btn_mapsActivity: Button = root.findViewById(R.id.btn_map)
        btn_mapsActivity.setOnClickListener() {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }

        //SR button
        val btn_SRAdding: Button = root.findViewById(R.id.btn_sradding)
        btn_SRAdding.setOnClickListener() {
            val intent = Intent(requireContext(), SRActivity::class.java)
            intent.putExtra(MapsActivity.tid_KEY, "Qqt7DNLsk12CWOtK26uE")
            startActivity(intent)
        }
            

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}