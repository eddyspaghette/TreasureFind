package com.AERYZ.treasurefind.main.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.AERYZ.treasurefind.db.MyFirebase
import com.AERYZ.treasurefind.db.MyUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileViewModel(private val activity: FragmentActivity) : ViewModel() {
    var profilePicture = MutableLiveData<Bitmap>()
    var ownedList = MutableLiveData<ArrayList<String>>()
    var foundList = MutableLiveData<ArrayList<String>>()
    private val myFirebase = MyFirebase()

    init {
        val uid = getUid()
        if (uid != "") {
            myFirebase.getProfileImage(activity, uid, profilePicture)
        }
        populateLists()
    }

    // populate both ownedList and foundList
    private fun populateLists() {
        val userDocumentRef = myFirebase.getUserDocument(getUid())
        userDocumentRef
            .get()
            .addOnSuccessListener {
                val userObject = it.toObject<MyUser>()
                ownedList.value = userObject!!.ownedList
                foundList.value = userObject.foundList
            }
            .addOnFailureListener {
                // TODO: handle exception
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
