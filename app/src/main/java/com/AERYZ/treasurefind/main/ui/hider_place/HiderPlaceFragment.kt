package com.AERYZ.treasurefind.main.ui.hider_place

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.AERYZ.treasurefind.main.CameraModule
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.dialogs.ProgressDialog
import com.AERYZ.treasurefind.main.ui.map.MapsActivity
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class HiderPlaceFragment : Fragment(), MyFirebase.TreasureInsertionListener {
    private lateinit var viewModel: HiderPlaceViewModel
    private lateinit var titleEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var cameraResultListener: ActivityResultLauncher<Intent>

    private var isBind = false
    private lateinit var serviceIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_hider_place, container, false)

        val treasureImageView: ImageView = view.findViewById(R.id.treasurePhoto)
        viewModel = ViewModelProvider(requireActivity())[HiderPlaceViewModel::class.java]
        viewModel.treasurePhoto.observe(requireActivity()) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }

        serviceIntent = Intent(requireContext(), TrackingService::class.java)

        titleEditText = view.findViewById(R.id.treasureTitle)
        descEditText = view.findViewById(R.id.treasureDescription)

        viewModel.location.observe(requireActivity()) {
            Log.d("Debug", "${it.latitude} ${it.longitude}")
        }

        cameraResultListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null && it.data!!.extras != null) {
                    viewModel.treasurePhoto.value = it.data!!.extras!!.get("data") as Bitmap
                }
            }
        }

        setButtonListeners(view)

        requireActivity().startService(serviceIntent)
        bindService()

        return view
    }

    fun bindService(){
        if(!isBind){
            requireActivity().applicationContext.bindService(serviceIntent, viewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println("bind service!")
        }
    }

    private fun unBindService(){
        if (isBind) {
            requireActivity().applicationContext.unbindService(viewModel)
            isBind = false
            println("unbind service!!!!!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
        requireActivity().stopService(serviceIntent)
    }

    // set all the button listeners
    private fun setButtonListeners(view: View) {
        setTakePhotoBtnListener(view)
        addTreasureBtnListener(view)
    }

    private fun addTreasureBtnListener(view: View) {
        val btn: Button = view.findViewById(R.id.addButton)
        btn.setOnClickListener {
            if (viewModel.location.value == null) {
                Log.d("Debug", "Failed to locate")
            }
            val location = viewModel.location.value
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            uid?.let {
                val myFirebase = MyFirebase()
                val myTreasure = Treasure(
                    it,
                    "",
                    titleEditText.text.toString(),
                    descEditText.text.toString(),
                    location!!.latitude,
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
        // cameraModule is responsible for adding bitmap to viewmodel
        //val cameraModule = CameraModule(requireActivity(), viewModel.treasurePhoto)
        btn.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultListener.launch(cameraIntent)
        }
    }

    override fun onSuccess(tid: String) {
        //start Map activity

        val intent = Intent(requireActivity(), MapsActivity::class.java)
        intent.putExtra(MapsActivity.tid_KEY, tid)
        intent.putExtra(MapsActivity.who_KEY, 0) //0 is hider, 1 is seeker
        startActivity(intent)
    }

    override fun onFailure(exception: Exception) {
        Log.d("Debug", "Insert failed")
    }

}