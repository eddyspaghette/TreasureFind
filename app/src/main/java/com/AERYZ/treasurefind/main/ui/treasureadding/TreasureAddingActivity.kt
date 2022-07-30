package com.AERYZ.treasurefind.main.ui.treasureadding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.CameraModule
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treasure_adding)

        val treasureImageView: ImageView = findViewById(R.id.treasurePhoto)
        treasureAddingViewModel = ViewModelProvider(this)[TreasureAddingViewModel::class.java]
        treasureAddingViewModel.treasurePhoto.observe(this) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }

        titleEditText = findViewById(R.id.treasureTitle)
        descEditText = findViewById(R.id.treasureDescription)

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
                    titleEditText.text.toString(),
                    descEditText.text.toString(),
                    LatLng(0.0, 0.0),
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