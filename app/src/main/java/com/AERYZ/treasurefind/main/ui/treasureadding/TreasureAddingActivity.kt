package com.AERYZ.treasurefind.main.ui.treasureadding

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.CameraModule
import com.AERYZ.treasurefind.main.services.TrackingService
import com.AERYZ.treasurefind.main.ui.dialogs.ProgressDialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class TreasureAddingActivity : AppCompatActivity() {
    private lateinit var treasureAddingViewModel: TreasureAddingViewModel
    private lateinit var titleEditText: EditText
    private lateinit var descEditText: EditText

    private var isBind = false
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treasure_adding)

        val treasureImageView: ImageView = findViewById(R.id.treasurePhoto)
        treasureAddingViewModel = ViewModelProvider(this)[TreasureAddingViewModel::class.java]
        treasureAddingViewModel.treasurePhoto.observe(this) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }

        serviceIntent = Intent(this, TrackingService::class.java)

        titleEditText = findViewById(R.id.treasureTitle)
        descEditText = findViewById(R.id.treasureDescription)

        treasureAddingViewModel.location.observe(this) {
            Log.d("Debug", "${it.latitude} ${it.longitude}")
        }

        setButtonListeners()

        startService(serviceIntent)
        bindService()
    }

    fun bindService(){
        if(!isBind){
            applicationContext.bindService(serviceIntent, treasureAddingViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
            println("bind service!")
        }
    }

    private fun unBindService(){
        if (isBind) {
            applicationContext.unbindService(treasureAddingViewModel)
            isBind = false
            println("unbind service!!!!!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
        stopService(intent)
    }

    // set all the button listeners
    private fun setButtonListeners() {
        setTakePhotoBtnListener()
        addTreasureBtnListener()
    }

    private fun addTreasureBtnListener() {
        val btn: Button = findViewById(R.id.addButton)
        btn.setOnClickListener {
            if (treasureAddingViewModel.location.value == null) {
                Log.d("Debug", "Failed to locate")
            }
            val location = treasureAddingViewModel.location.value
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
                    treasureAddingViewModel.treasurePhoto.value!!
                )
                val dialog = ProgressDialog.progressDialog(this)
                val successDialog = ProgressDialog.successDialog(this)
                myFirebase.insert(myTreasure, dialog, successDialog)
            }
        }
    }

    private fun setTakePhotoBtnListener() {
        val btn: Button = findViewById(R.id.changeImgButton)
        // cameraModule is responsible for adding bitmap to viewmodel
        val cameraModule = CameraModule(this, treasureAddingViewModel.treasurePhoto)
        btn.setOnClickListener {
            cameraModule.takePhoto()
        }
    }
}