package com.AERYZ.treasurefind.main.ui.hider_place

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.services.ServiceViewModel

class HiderPlaceViewModel : ServiceViewModel() {
    var treasurePhoto = MutableLiveData<Bitmap>()
}