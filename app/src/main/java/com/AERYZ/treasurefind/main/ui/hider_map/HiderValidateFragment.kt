package com.AERYZ.treasurefind.main.ui.hider_map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.main.OnSwipeTouchListener


class HiderValidateFragment : Fragment() {
    private lateinit var viewModel: HiderMapViewModel
    private lateinit var viewModelFactory: HiderMapViewModelFactory
    private var tid: String = ""
    private var isFirstTimeSR = false
    private lateinit var fragment: SrFragment
    private var myFirebase = MyFirebase()

    @SuppressLint("ClickableViewAccessibility")
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
                    fragment = SrFragment()
                    val bundle = Bundle()
                    bundle.putString(SrFragment.sid_KEY, it.sr[0])
                    fragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.hider_validate_fragment, fragment).commit()
                }
            }
        }

        val accept_btn: Button = view.findViewById(R.id.hider_map_accept_btn)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.hider_validate_relativelayout)

        accept_btn.setOnClickListener() {
            Toast.makeText(requireActivity(), "Ok!", Toast.LENGTH_SHORT).show()
            if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                myFirebase.updateTreasure(tid, "wid", viewModel.treasure.value!!.sr[0])
            }
        }

        relativeLayout.setOnTouchListener(object: OnSwipeTouchListener(requireActivity()){
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                    val sid = viewModel.treasure.value!!.sr[0]
                    myFirebase.removeSR(tid, sid)
                }
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                    val sid = viewModel.treasure.value!!.sr[0]
                    myFirebase.removeSR(tid, sid)
                }
            }
        })
        return view
    }

}