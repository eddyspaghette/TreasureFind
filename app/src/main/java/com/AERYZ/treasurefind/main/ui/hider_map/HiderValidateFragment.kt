package com.AERYZ.treasurefind.main.ui.hider_map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.main.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.mikhaellopez.circularimageview.CircularImageView


/*
 * Hider handles the submit requests from Seekers here
 * Hider can determine a winner or swipe to see other submit requests
*/
class HiderValidateFragment : Fragment() {
    private lateinit var viewModel: HiderMapViewModel
    private lateinit var viewModelFactory: HiderMapViewModelFactory
    private var tid: String = ""
    private var myFirebase = MyFirebase()
    private var uid = FirebaseAuth.getInstance().uid!!
    private lateinit var authorTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var avatarSRView: CircularImageView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_hider_validate, container, false)

        authorTextView = view.findViewById(R.id.sr_author_textview)
        imageView = view.findViewById(R.id.sr_imageview)
        avatarSRView = view.findViewById(R.id.avatar_sr)


        tid = arguments?.getString(HiderMapActivity.tid_KEY).toString()

        viewModelFactory = HiderMapViewModelFactory(tid)
        viewModel = ViewModelProvider(this, viewModelFactory)[HiderMapViewModel::class.java]

        val avatar_image = MutableLiveData<Bitmap>(BitmapFactory.decodeResource(resources, R.drawable.tf_logo))
        avatar_image.observe(requireActivity()) {
            avatarSRView.setImageBitmap(it)
        }

        viewModel.treasure.observe(requireActivity()) {
            if (it != null)
            {
                if (it.sr.size > 0)
                {
                    val sid = it.sr[0]
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
            }
        }

        val accept_btn: Button = view.findViewById(R.id.hider_map_accept_btn)
        val relativeLayout: RelativeLayout = view.findViewById(R.id.hider_validate_relativelayout)

        accept_btn.setOnClickListener() {
            if (viewModel.treasure.value != null && viewModel.treasure.value!!.sr.size > 0) {
                val wid = viewModel.treasure.value!!.sr[0]
                myFirebase.updateTreasure(tid, "wid", wid)
                myFirebase.getUserDocument(wid).update("score", FieldValue.increment(1))
                myFirebase.getUserDocument(uid).update("score", FieldValue.increment(1))
                myFirebase.addToFoundList(wid, tid)
                myFirebase.addToOwnedList(uid, tid)
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