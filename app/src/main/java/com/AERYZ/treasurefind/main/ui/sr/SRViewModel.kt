package com.AERYZ.treasurefind.main.ui.sr

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.main.services.ServiceViewModel

class SRViewModel: ServiceViewModel() {
    var image = MutableLiveData<Bitmap>()
}