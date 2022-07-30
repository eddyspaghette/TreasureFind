package com.AERYZ.treasurefind.main.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileViewModel : ViewModel() {
    var profilePicture = MutableLiveData<Bitmap>()

    init {
        val uid = getUid()
        if (uid != "") {
            val storage = Firebase.storage
            val profileImageRef = storage.reference.child("images/profile/${uid}")
            // TODO download the file and store it into the imageview
        }
    }


    // returns a UID if profile picture exists
    private fun getUid(): String {
        val fbInstance = FirebaseAuth.getInstance().currentUser
        // return uid if it exists
        return fbInstance?.uid  ?: ""
    }
}