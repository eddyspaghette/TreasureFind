package com.AERYZ.treasurefind.main.ui.treasureadding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.CameraModule

class TreasureAddingActivity : AppCompatActivity() {
    private lateinit var treasureAddingViewModel: TreasureAddingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treasure_adding)

        val treasureImageView: ImageView = findViewById(R.id.treasurePhoto)
        treasureAddingViewModel = ViewModelProvider(this)[TreasureAddingViewModel::class.java]
        treasureAddingViewModel.treasurePhoto.observe(this) {
            // everytime the bitmap changes, set the imageview
            treasureImageView.setImageBitmap(it)
        }

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