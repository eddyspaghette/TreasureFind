package com.AERYZ.treasurefind.main.ui.treasureadding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.CameraModule
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class TreasureAddingActivity : AppCompatActivity() {
    private lateinit var treasureAddingViewModel: TreasureAddingViewModel
    private lateinit var progressBar: CircularProgressIndicator
    private var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treasure_adding)

        val treasureImageView: ImageView = findViewById(R.id.treasurePhoto)
        treasureAddingViewModel = ViewModelProvider(this)[TreasureAddingViewModel::class.java]
        treasureAddingViewModel.treasurePhoto.observe(this) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.INVISIBLE

        setButtonListeners()

    }

    // set all the button listeners
    private fun setButtonListeners() {
        setTakePhotoBtnListener()
        addTreasureBtnListener()
    }

    private fun addTreasureBtnListener() {
        val btn: Button = findViewById(R.id.addButton)
        btn.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            uid?.let {
                val myFirebase = MyFirebase()
                val myTreasure = Treasure(
                    it,
                    LatLng(0.0, 0.0),
                    treasureAddingViewModel.treasurePhoto.value!!
                )
                myFirebase.insert(myTreasure, progressBar)
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