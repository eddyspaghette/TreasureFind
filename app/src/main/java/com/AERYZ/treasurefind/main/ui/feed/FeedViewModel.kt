package com.AERYZ.treasurefind.main.ui.feed

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.util.Util
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject

import java.lang.Exception

class FeedViewModel(private val activity: FragmentActivity): ViewModel(), MyFirebase.SnapshotListener {
    var feedList = MutableLiveData<ArrayList<Treasure>>()
    private val myFirebase = MyFirebase()

    // when treasure data transfer fails
    override fun onFailure(exception: Exception) {
        println("DEBUG error: $exception")
        feedList.value = arrayListOf()
    }

    // this function is called when all treasure data is successfully loaded
    override fun onSuccess(snapshot: QuerySnapshot) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val currentLocation: LatLng = Util.getCurrentLocation(activity)
        val list: ArrayList<Treasure> = arrayListOf()
        for (treasure in snapshot) {
            val retTreasure = treasure.toObject<Treasure>()
            if (retTreasure.wid == "" && retTreasure.oid != uid) {
                val treasureLatLng = LatLng(retTreasure.latitude!!, retTreasure.longitude!!)
                val distance = Util.calculateDistance(currentLocation, treasureLatLng)
                var distanceToTreasure = String.format("%.2fm", distance)
                // convert to KM if > 1000 meters
                if (distance > 1000) {
                    distanceToTreasure = String.format("%.2fkm", distance/1000)
                }
                retTreasure.distance = distance.toDouble()
                retTreasure.distanceText = distanceToTreasure
                list.add(retTreasure)
            }
        }
        list.sortBy{it.distance}
        feedList.value = list
    }

    init {
        // this retrieves all documents in the treasure collection
        // fires off a callback once it is ready
        myFirebase.getAllTreasures(this)
    }
}


class FeedFragmentViewModelFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java))
            return FeedViewModel(activity) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}