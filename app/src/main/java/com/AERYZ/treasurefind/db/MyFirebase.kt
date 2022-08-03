package com.AERYZ.treasurefind.db

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.feed.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

data class MyUser(
    var uid: String = "",
    var userName: String = "",
    var email: String = "",
    @Exclude @set:Exclude @get:Exclude var profileImage: Bitmap? = null,
    var profileImagePath: String = ""
)

data class Treasure(
    var oid: String? = "",
    var tid: String? = "",
    var title: String? = "",
    var desc: String? = "",
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    // this prevents firebase collections from saving the bitmap
    @Exclude @set:Exclude @get:Exclude var treasureImage: Bitmap? = null,
    var wid: String? = "",
    var startTime: String? = "",
    var seekers: ArrayList<String> = arrayListOf<String>(),
    var sr: ArrayList<String> = arrayListOf<String>(),
    var length: String? = "",
    var treasureImagePath: String? = "treasureImagePath",
)


data class SR(var sid: String, var sRImage: Bitmap) {
}

class MyFirebase {
    private var storage = Firebase.storage
    private var db = Firebase.firestore
    private var storageReference = storage.reference

    interface FirebaseFeedListener {
        fun onSuccess(snapshot: QuerySnapshot)
        fun onFailure(exception: Exception)
    }

    fun getAllTreasures(listener: FirebaseFeedListener) {
        db.collection("treasures")
            .get()
            .addOnSuccessListener { result ->
                Log.d("DEBUG: result", "$result")
                listener.onSuccess(result)
            }.addOnFailureListener { exception ->
                Log.d("DEBUG: failure", "$exception")
                listener.onFailure(exception)
            }
    }



    private fun insertToFirebaseStorage(bitmap: Bitmap, path: String, dialog: Dialog? = null, successDialog: Dialog? = null) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val reference = storageReference.child(path)
        val uploadTask = reference.putBytes(data)
        if (dialog != null) {
            uploadTask.addOnFailureListener {
                dialog.dismiss()
                Log.d("DEBUG: failed upload", "$it")
            }.addOnSuccessListener {
                dialog.dismiss()
                successDialog?.let {
                    successDialog.show()
                    Timer().schedule(2400) {
                        successDialog.dismiss()
                    }
                }
                Log.d("DEBUG: uploaded successfully", "$it")
            }.addOnProgressListener {
                dialog.show()
            }
        } else {
            uploadTask.addOnFailureListener {
                Log.d("DEBUG: failed upload", "$it")
            }.addOnSuccessListener {
                Log.d("DEBUG: uploaded successfully", "$it")
            }
        }
    }

    // returns document reference, caller has to implement listeners
    fun getUserDocument(uid: String): DocumentReference {
        val docRef = db.collection("users").document(uid)
        return docRef
    }

    fun insert(myUser: MyUser) {
        val profileImagePath = "images/profile/${myUser.uid}.jpg"
        myUser.profileImagePath = profileImagePath
        db.collection("users").document(myUser.uid).set(myUser)
        insertToFirebaseStorage(myUser.profileImage!!, profileImagePath)
    }

    fun insert(treasure: Treasure, dialog: Dialog? = null, successDialog: Dialog? = null) {
        db.collection("treasures")
            .add(treasure)
            .addOnSuccessListener {
                val treasureImagePath = "images/treasures/${it.id}/image.jpg"
                it.update("treasureImagePath", treasureImagePath)
                it.update("tid", it.id)
                treasure.treasureImage?.let { it1 -> insertToFirebaseStorage(it1, treasureImagePath, dialog, successDialog) }
            }
    }
    fun getTreasure(tid: String, mutableLiveData: MutableLiveData<Treasure>) {
        val docRef = db.collection("treasures").document(tid)
        val source = Source.CACHE
        docRef.get(source).addOnCompleteListener() {
            if (it.isSuccessful) {
                val treasure = Treasure(it.result.get("oid").toString(),
                    it.result.get("title").toString(),
                    it.result.get("desc").toString())
                mutableLiveData.value = treasure
                Log.d("Debug", "${treasure.title} ${treasure.desc}")
            }

        }.addOnFailureListener() {
            Log.d("Debug", "Failed to achieve data")
        }
    }

    fun getTreasureImage(activity: Activity, tid: String, mutableLiveData: MutableLiveData<Bitmap>) {
        var treasureImagePath = "images/treasures/${tid}/image.jpg"
        val reference = storageReference.child(treasureImagePath)
        mutableLiveData.value = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        CoroutineScope(IO).launch {
            val bitmap = GlideApp.with(activity)
                .asBitmap()
                .error(R.drawable.tf_logo)
                .load(reference)
                .submit()
                .get()
            withContext(Main) {
                mutableLiveData.value = bitmap
                Log.d("Debug", "Got Loading treasure image")
            }
        }
    }

    fun updateSeeker(tid: String, sid: String) {
        db.collection("treasures").document(tid).update("seekers", FieldValue.arrayUnion(sid))
    }

    fun updateSR(tid: String, sR: SR) {
        db.collection("treasures").document(tid).update("sr", FieldValue.arrayUnion(sR.sid))
        val sRImagePath =  "images/treasures/${tid}/${sR.sid}.jpg"
        insertToFirebaseStorage(sR.sRImage, sRImagePath)
    }

    fun updateProfileImage(uid: String, image: Bitmap) {
        var profileImagePath = "images/profile/${uid}.jpg"
        insertToFirebaseStorage(image, profileImagePath)

    }

    fun getProfileImage(activity: FragmentActivity, uid: String, mutableLiveData: MutableLiveData<Bitmap>) {
        var profileImagePath = "images/profile/${uid}.jpg"
        val reference = storageReference.child(profileImagePath)
        mutableLiveData.value = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        CoroutineScope(IO).launch {
            val bitmap = GlideApp.with(activity)
                .asBitmap()
                .error(com.google.android.material.R.drawable.ic_clock_black_24dp)
                .load(reference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .submit()
                .get()
                mutableLiveData.postValue(bitmap)
        }
    }

}