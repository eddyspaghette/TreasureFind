package com.AERYZ.treasurefind.main.ui.seeker_map

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.AERYZ.treasurefind.db.Treasure
import com.AERYZ.treasurefind.main.services.ServiceViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class SeekerMapViewModel(private val tid: String): ServiceViewModel() {
    var treasure = MutableLiveData<Treasure>()
    var seekers = hashMapOf<String, MutableLiveData<MyUser>>()
    var seekersImage = hashMapOf<String, MutableLiveData<Bitmap>>()
    var seekers_size = MutableLiveData<Int>(0)
    var markers = hashMapOf<String, Marker>()
    var myUser = MutableLiveData<MyUser>()
    var treasureFakeLocation = LatLng(0.0, 0.0)
    var isInteract = MutableLiveData(true)
    var hiderStatus = MutableLiveData<Int>(0)
    val db = Firebase.firestore
    private val myFirebase = MyFirebase()

    init {
        SeekerChangeListener(tid)
        HostStatusListener(tid)
        MyUserListener()
    }

    fun MyUserListener() {
        val uid = FirebaseAuth.getInstance().uid!!
        var docRef = db.collection("users").document(uid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Debug", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                myFirebase.getUserDocument(uid)
                    .get()
                    .addOnCompleteListener {
                        myUser.value = it.result.toObject<MyUser>()

                    }
                Log.d("Debug", "Current data: ${snapshot.data}")
            } else {
                Log.d("Debug", "Current data: null")
            }
        }
    }

    fun updateSeekerLocation(location: LatLng) {
        val uid = FirebaseAuth.getInstance().uid!!
        myFirebase.updateUser(uid, "latitude", location.latitude)
        myFirebase.updateUser(uid, "longitude", location.longitude)
    }

    fun HostStatusListener(tid: String) {
        myFirebase.getTreasureDocument(tid)
            .get()
            .addOnCompleteListener {
                val myTreasure = it.result.toObject<Treasure>()
                if (myTreasure != null) {
                    val docRef = db.collection("users").document(myTreasure.oid!!)
                    docRef.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            myFirebase.getUserDocument(myTreasure.oid!!)
                                .get()
                                .addOnCompleteListener {
                                    val hider = it.result.toObject<MyUser>()
                                    if (hider != null) {
                                        hiderStatus.postValue(hider.status)
                                    }
                                }
                        }
                    }
                }
            }

    }

    fun SeekerChangeListener(tid:String) {
        val docRef = db.collection("treasures").document(tid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                myFirebase.getTreasure(tid, treasure)
            }
        }
    }
    fun SeekerUpdateListener(sid: String) {
        var docRef = db.collection("users").document(sid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
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
