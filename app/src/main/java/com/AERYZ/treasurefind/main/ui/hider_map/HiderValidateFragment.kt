package com.AERYZ.treasurefind.main.ui.hider_map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R


class HiderValidateFragment : Fragment() {
    private lateinit var viewModel: HiderMapViewModel
    private lateinit var viewModelFactory: HiderMapViewModelFactory
    private var tid: String = ""
    private var isFirstTimeSR = false
    private lateinit var fragment: SrFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_hider_validate, container, false)

        tid = arguments?.getString(HiderMapActivity.tid_KEY).toString()

        viewModelFactory = HiderMapViewModelFactory(tid!!)
        viewModel = ViewModelProvider(this, viewModelFactory)[HiderMapViewModel::class.java]


        viewModel.treasure.observe(requireActivity()) {
            if (it != null)
            {
                if (it.sr.size > 0)
                {
                    Log.d("Debug", "in creating fragment ${it.sr.size}")
                    fragment = SrFragment()
                    val bundle = Bundle()
                    bundle.putString(SrFragment.sid_KEY, it.sr[0])
                    fragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.hider_validate_fragment, fragment).commit()
                }
            }
        }

        val accept_btn: Button = view.findViewById(R.id.hider_map_accept_btn)
        val skip_btn: Button = view.findViewById(R.id.hider_map_skip_btn)

        accept_btn.setOnClickListener() {
            Toast.makeText(requireActivity(), "Ok!", Toast.LENGTH_SHORT).show()
            if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                viewModel.treasure.value!!.wid = viewModel.treasure.value!!.sr[0]
                //need update database here
            }
        }

        skip_btn.setOnClickListener() {
            Log.d("Debug: size of sr", viewModel.treasure.value!!.sr.size.toString())
            if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                viewModel.treasure.value!!.sr.removeFirst()
                //need update database here
            }
        }
        return view
    }

}