package com.AERYZ.treasurefind.main.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileViewModel(private val activity: FragmentActivity) : ViewModel() {
    var profilePicture = MutableLiveData<Bitmap>()
    private val myFirebase = MyFirebase()

    init {
        val uid = getUid()
        if (uid != "") {
            println("DEBUG: uid $uid")
            myFirebase.getProfileImage(activity, uid, profilePicture)
        }
    }


    // returns a UID if profile picture exists
    private fun getUid(): String {
        val fbInstance = FirebaseAuth.getInstance().currentUser
        // return uid if it exists
        return fbInstance?.uid  ?: ""
    }
}

class ProfileFragmentViewModelFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(activity) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
