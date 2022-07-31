package com.AERYZ.treasurefind.main.ui.sr

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.SR
import com.AERYZ.treasurefind.main.CameraModule
import com.AERYZ.treasurefind.main.ui.map.MapsActivity
import com.AERYZ.treasurefind.main.util.Util
import com.google.firebase.auth.FirebaseAuth

class SRActivity : AppCompatActivity() {
    private lateinit var srViewModel: SRViewModel
    private lateinit var activityResult: ActivityResultLauncher<Intent>
    private var uid = FirebaseAuth.getInstance().currentUser?.uid
    private var myFirebase = MyFirebase()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sractivity)
        srViewModel = ViewModelProvider(this)[SRViewModel::class.java]

        val argument = intent
        val tid = argument.getStringExtra(MapsActivity.tid_KEY).toString()

        val btn_submitSR: Button = findViewById(R.id.btn_submitSR)
        val imageView: ImageView = findViewById(R.id.SRImageView)


        val cameraModule = CameraModule(this, srViewModel.image)
        imageView.setOnClickListener() {
            cameraModule.takePhoto()
        }

        srViewModel.image.observe(this) {
            imageView.setImageBitmap(srViewModel.image.value)
        }

        btn_submitSR.setOnClickListener() {
            if (srViewModel.image.value != null && uid != null)
            {
                val sR = SR(uid!!, srViewModel.image.value!!)
                myFirebase.updateSR(tid, sR)
                //may need visualization here
            }
        }
    }
}