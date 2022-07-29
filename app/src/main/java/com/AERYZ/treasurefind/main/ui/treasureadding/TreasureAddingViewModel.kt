package com.AERYZ.treasurefind.main.ui.treasureadding

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TreasureAddingViewModel : ViewModel() {
    var treasurePhoto = MutableLiveData<Bitmap>()

    init {
        treasurePhoto.value =  Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
    }
}