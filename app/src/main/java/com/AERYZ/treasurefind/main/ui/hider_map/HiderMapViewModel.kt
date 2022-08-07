package com.AERYZ.treasurefind.main.ui.hider_map

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.ServiceViewModel
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class HiderMapViewModel(private val tid: String): ServiceViewModel() {
    var treasure = MutableLiveData<Treasure>()

    var seekers = hashMapOf<String, MutableLiveData<MyUser>>()
    var markers = hashMapOf<String, Marker>()
    var isInteract = MutableLiveData(true)
    val db = Firebase.firestore
    val myFirebase = MyFirebase()

    init {
        SeekerChangeListener(tid)
    }

    fun SeekerChangeListener(tid:String) {
        var docRef = db.collection("treasures").document(tid)
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

    fun SeekerUpdateListener(sid: String) {
        var docRef = db.collection("users").document(sid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Debug", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                myFirebase.getUserDocument(sid)
                    .get()
                    .addOnCompleteListener {
                        if (seekers[sid] != null) {
                            seekers[sid]!!.value = it.result.toObject<MyUser>()
                        }
                    }
                Log.d("Debug", "Current data: ${snapshot.data}")
            } else {
                Log.d("Debug", "Current data: null")
            }
        }
    }

}

class HiderMapViewModelFactory(private val tid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HiderMapViewModel::class.java))
            return HiderMapViewModel(tid) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
