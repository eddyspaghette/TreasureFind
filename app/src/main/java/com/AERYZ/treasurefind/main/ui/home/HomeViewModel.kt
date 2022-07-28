package com.AERYZ.treasurefind.main.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var CameraOutputBitmap = MutableLiveData<Bitmap>()
}