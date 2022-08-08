package com.AERYZ.treasurefind.main.ui.hider_map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity.Companion.tid_KEY
import com.google.firebase.firestore.ktx.toObject
import com.mikhaellopez.circularimageview.CircularImageView

class SrFragment : Fragment() {
    private var myFirebase = MyFirebase()
    private lateinit var authorTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var avatarSRView: CircularImageView
    companion object {
        val sid_KEY = "sid"
        val tid_KEY = "tid"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sr, container, false)

        authorTextView = view.findViewById(R.id.sr_author_textview)
        imageView = view.findViewById(R.id.sr_imageview)
        avatarSRView = view.findViewById(R.id.avatar_sr)

        val avatar_image = MutableLiveData<Bitmap>(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))

        avatar_image.observe(requireActivity()) {
            avatarSRView.setImageBitmap(it)
        }
        if (arguments != null) {
            val sid = arguments?.getString(sid_KEY)!!
            val tid = arguments?.getString(tid_KEY)!!
            myFirebase.getUserDocument(sid).get()
                .addOnCompleteListener {
                    val seeker = it.result.toObject<MyUser>()
                    if (seeker != null) {
                        val author_text = seeker.userName
                        authorTextView.text = author_text
                        myFirebase.getProfileImage(requireActivity(), seeker.uid, avatar_image)
                        val bitmap = MutableLiveData(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))
                        myFirebase.getSRImage(requireActivity(), tid, sid, bitmap)
                        bitmap.observe(requireActivity()) { bm ->
                            imageView.setImageBitmap(bm)
                        }
                    }
                }

        }
        return view
    }
}