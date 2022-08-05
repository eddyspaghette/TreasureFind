package com.AERYZ.treasurefind.main.ui.hider_map

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.AERYZ.treasurefind.R

class SrFragment : Fragment() {
    companion object {
        val sid_KEY = "sid"
        val bitmap_KEY = "bitmap"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sr, container, false)

        if (arguments != null) {
            val sid = "TID: ${arguments?.getString(sid_KEY)}"
            Log.d("Debug: ", sid)
            val bitmap: Bitmap? = arguments?.getParcelable(bitmap_KEY)
            val sid_text: TextView = view.findViewById(R.id.sr_tid_textview)
            val imageView: ImageView = view.findViewById(R.id.sr_imageview)
            sid_text.text = sid
            imageView.setImageBitmap(bitmap)
        }
        return view
    }
}