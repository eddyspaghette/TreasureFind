package com.AERYZ.treasurefind.main.ui.treasureadding

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.AERYZ.treasurefind.main.services.ServiceViewModel

class TreasureAddingViewModel : ServiceViewModel() {
    var treasurePhoto = MutableLiveData<Bitmap>()

}