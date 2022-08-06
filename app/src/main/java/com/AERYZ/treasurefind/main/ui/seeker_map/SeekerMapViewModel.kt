package com.AERYZ.treasurefind.main.ui.seeker_map

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.ServiceViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SeekerMapViewModel(private val tid: String): ServiceViewModel() {
    var treasure = MutableLiveData<Treasure>()
    var isInteract = MutableLiveData(true)
    var curLocation = LatLng(0.0, 0.0)
    val db = Firebase.firestore
    val myFirebase = MyFirebase()

    init {
        SeekerChangeListener(tid)
    }

    fun updateSeekerLocation(location: LatLng) {
        val uid = FirebaseAuth.getInstance().uid!!
        myFirebase.updateUser(uid, "latitude", location.latitude)
        myFirebase.updateUser(uid, "longitude", location.longitude)
    }

    fun SeekerChangeListener(tid:String) {
        val docRef = db.collection("treasures").document(tid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Debug", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                myFirebase.getTreasure(tid, treasure)
                Log.d("Debug", "Current data: ${snapshot.data}")
            } else {
                Log.d("Debug", "Current data: null")
            }
        }
    }

}
class SeekerMapViewModelFactory(private val tid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SeekerMapViewModel::class.java))
            return SeekerMapViewModel(tid) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
