package com.AERYZ.treasurefind.main.ui.treasuredetails

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.ServiceViewModel

class TreasureDetailsViewModel: ServiceViewModel() {
    var treasure = MutableLiveData(Treasure())
    var image = MutableLiveData<Bitmap>()
}