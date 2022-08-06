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
import com.AERYZ.treasurefind.main.ui.livecamera.LiveCameraActivity
import com.AERYZ.treasurefind.main.ui.bottomsheet.BottomSheetActivity
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity

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
            val intent = Intent(requireContext(), SeekerMapActivity::class.java)
            startActivity(intent)
        }

        //LiveCamera button
        val btn_LiveCamera: Button = root.findViewById(R.id.btn_LiveCamera)
        btn_LiveCamera.setOnClickListener() {
            val intent = Intent(requireContext(), LiveCameraActivity::class.java)
            startActivity(intent)
        }

        //Bottomsheet button
        val btn_bottomsheet: Button = root.findViewById(R.id.btn_bottomsheet)
        btn_bottomsheet.setOnClickListener() {
            val intent = Intent(requireContext(), BottomSheetActivity::class.java)

            startActivity(intent)
        }
            

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}