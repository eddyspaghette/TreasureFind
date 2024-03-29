package com.AERYZ.treasurefind.main.ui.hider_place

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.dialogs.ProgressDialog
import com.AERYZ.treasurefind.main.ui.hider_map.HiderMapActivity
import com.AERYZ.treasurefind.main.ui.seeker_map.SeekerMapActivity
import com.AERYZ.treasurefind.main.util.Util
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

/*
 * Hider place is where users can upload a image to create a session
*/
class HiderPlaceFragment : Fragment(), MyFirebase.TreasureInsertionListener {
    private lateinit var viewModel: HiderPlaceViewModel
    private lateinit var titleEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var cameraResultListener: ActivityResultLauncher<Intent>

    private var myFirebase = MyFirebase()
    private var uid = FirebaseAuth.getInstance().uid!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_hider_place, container, false)

        val treasureImageView: ImageView = view.findViewById(R.id.treasurePhoto)
        viewModel = ViewModelProvider(requireActivity())[HiderPlaceViewModel::class.java]

        viewModel.treasurePhoto.value = BitmapFactory.decodeResource(
                                        requireActivity().getResources(),
                                        R.drawable.tf_logo)
        viewModel.treasurePhoto.observe(requireActivity()) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }

        titleEditText = view.findViewById(R.id.treasureTitle)
        descEditText = view.findViewById(R.id.treasureDescription)


        cameraResultListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null && it.data!!.extras != null) {
                    viewModel.treasurePhoto.value = it.data!!.extras!!.get("data") as Bitmap
                }
            }
        }

        setButtonListeners(view)

        return view
    }


    // set all the button listeners
    private fun setButtonListeners(view: View) {
        setTakePhotoBtnListener(view)
        addTreasureBtnListener(view)
    }

    private fun addTreasureBtnListener(view: View) {
        val btn: Button = view.findViewById(R.id.addButton)
        btn.setOnClickListener {
            val location = Util.getCurrentLocation(requireActivity())
            uid.let {
                val myFirebase = MyFirebase()
                val myTreasure = Treasure(
                    it,
                    "",
                    titleEditText.text.toString(),
                    descEditText.text.toString(),
                    location.latitude,
                    location.longitude,
                    viewModel.treasurePhoto.value!!
                )
                val dialog = ProgressDialog.progressDialog(requireActivity())
                val successDialog = ProgressDialog.successDialog(requireActivity())
                myFirebase.insert(myTreasure, dialog, successDialog, this)
            }
    }
    }

    private fun setTakePhotoBtnListener(view: View) {
        val btn: Button = view.findViewById(R.id.changeImgButton)
        btn.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultListener.launch(cameraIntent)
        }
    }

    override fun onSuccess(tid: String) {
        //update database
        myFirebase.updateUser(uid, "in_session", tid)

        //start Map activity
        val intent = Intent(requireActivity(), HiderMapActivity::class.java)
        intent.putExtra(SeekerMapActivity.tid_KEY, tid)
        startActivity(intent)
    }

    override fun onFailure(exception: Exception) {
        Log.d("Debug", "Insert failed")
    }
}