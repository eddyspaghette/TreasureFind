package com.AERYZ.treasurefind.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData

/* Camera functionality so we can call it from within other activities easily */
class CameraModule(activity: FragmentActivity, var CameraOutputBitmap: MutableLiveData<Bitmap>) {
    private var cameraResultListener: ActivityResultLauncher<Intent>

    init {
        if (CameraOutputBitmap.value == null) {
            CameraOutputBitmap.value = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        }
        cameraResultListener = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null && it.data!!.extras != null) {
                    CameraOutputBitmap.value = it.data!!.extras!!.get("data") as Bitmap
                }
            }
        }
    }

    fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultListener.launch(cameraIntent)
    }
}