package com.AERYZ.treasurefind.main.ui.map

import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.ServiceViewModel

class MapsViewModel: ServiceViewModel() {
    var treasure = MutableLiveData<Treasure>()
}