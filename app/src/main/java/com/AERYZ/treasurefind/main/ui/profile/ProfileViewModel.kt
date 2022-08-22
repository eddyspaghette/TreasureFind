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
import java.lang.Exception

class ProfileViewModel(private val activity: FragmentActivity) : ViewModel(), MyFirebase.IntListener {
    var profilePicture = MutableLiveData<Bitmap>()
    var ownedList = MutableLiveData<ArrayList<String>>()
    var foundList = MutableLiveData<ArrayList<String>>()
    var foundNumber = MutableLiveData<Int>()
    var ownNumber = MutableLiveData<Int>()
    var rank = MutableLiveData<Int>()

    private val myFirebase = MyFirebase()

    init {
        val uid = getUid()
        if (uid != "") {
            myFirebase.getProfileImage(activity, uid, profilePicture)
        }
        myFirebase.returnRank(uid, this)
        populateLists()
    }

    // populate both ownedList and foundList
    private fun populateLists() {
        val userDocumentRef = myFirebase.getUserDocument(getUid())
        userDocumentRef
            .get()
            .addOnSuccessListener {
                val userObject = it.toObject<MyUser>()
                val retOwnedList = userObject!!.ownedList
                val retFoundList = userObject.foundList
                ownedList.value = retOwnedList
                foundList.value = retFoundList
                foundNumber.value = retFoundList.size
                ownNumber.value = retOwnedList.size
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

    override fun onSuccess(size: Int) {
        rank.value = size
        println("DEBUG: size $size")
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }
}

class ProfileFragmentViewModelFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(activity) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
