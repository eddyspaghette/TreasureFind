package com.AERYZ.treasurefind.main.ui.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.Treasure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject

import java.lang.Exception

class FeedViewModel : ViewModel(), MyFirebase.FirebaseFeedListener {
    var feedList = MutableLiveData<ArrayList<Treasure>>()

    // when treasure data transfer fails
    override fun onFailure(exception: Exception) {
        println("DEBUG error: $exception")
        feedList.value = arrayListOf()
    }

    // this function is called when all treasure data is successfully loaded
    override fun onSuccess(snapshot: QuerySnapshot) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val list: ArrayList<Treasure> = arrayListOf()
        for (treasure in snapshot) {
            val retTreasure = treasure.toObject<Treasure>()
            if (retTreasure.wid == "" && retTreasure.oid != uid) {
                list.add(retTreasure)
            }
        }
        feedList.value = list
    }

    init {
        // this retrieves all documents in the treasure collection
        val myFirebase = MyFirebase()
        myFirebase.getAllTreasures(this)
    }



}