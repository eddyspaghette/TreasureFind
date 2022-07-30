package com.AERYZ.treasurefind.main.ui.treasureadding

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.CameraModule
import com.AERYZ.treasurefind.main.ui.dialogs.ProgressDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

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
//        progressBar = findViewById(R.id.progress_bar)
//        progressBar.visibility = View.INVISIBLE

        setButtonListeners()

    }

    // set all the button listeners
    private fun setButtonListeners() {
        setTakePhotoBtnListener()
        addTreasureBtnListener()
    }

    private fun addTreasureBtnListener() {
        val btn: Button = findViewById(R.id.addButton)
        val titleView: TextView = findViewById(R.id.treasureTitle)
        val descriptionView: TextView = findViewById(R.id.treasureDescription)
        btn.setOnClickListener {
//            val uid = FirebaseAuth.getInstance().currentUser?.uid
//            if (uid != null) {
//                val baos = ByteArrayOutputStream()
//                treasureAddingViewModel.treasurePhoto.value!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                val data = baos.toByteArray()
//
//                val treasureRef = storage.reference
//                    .child("images/treasures/uid/${uid}/${System.currentTimeMillis()}")
//
//                val uploadTask = treasureRef.putBytes(data)
//                uploadTask.addOnFailureListener {
//                    Log.d("DEBUG: failed upload", "$it")
//                }.addOnSuccessListener {
//                    progressBar.visibility = View.INVISIBLE
//                    Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT)
//                    Log.d("DEBUG: uploaded successfully", "$it")
//                }.addOnProgressListener {
//                    progressBar.visibility = View.VISIBLE
//                }
//            }
            val dialog = ProgressDialog.progressDialog(this)
            dialog.show()
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