package com.AERYZ.treasurefind.db

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.feed.GlideApp
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class User(var uid: String, var userName: String, var email: String, var profileImage: Bitmap) {
}

class Treasure(var oid: String, var title: String,
               var desc: String, var location: LatLng,
               var treasureImage: Bitmap, var wid: String = "",
               var startTime: String? = null, var length: String? = null) {
}

class SR(var sid: String, var sRImage: Bitmap) {
}

class MyFirebase {
    private var storage = Firebase.storage
    private var db = Firebase.firestore
    private var storageReference = storage.reference

    private fun insertToFirebaseStorage(bitmap: Bitmap, path: String, progressBar: CircularProgressIndicator?) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val reference = storageReference.child(path)
        val uploadTask = reference.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.d("DEBUG: failed upload", "$it")
        }.addOnSuccessListener {
            progressBar?.let {
                progressBar.visibility = View.INVISIBLE
            }
            Log.d("DEBUG: uploaded successfully", "$it")
        }.addOnProgressListener {
            progressBar?.let {
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    fun insert(user: User, progressBar: CircularProgressIndicator? = null) {
        var profileImagePath = "images/profile/${user.uid}.jpg"
        val data = hashMapOf(
            "username" to user.userName,
            "email" to user.email,
            "profile_image_path" to profileImagePath
        )

        db.collection("users").document(user.uid).set(data)
        insertToFirebaseStorage(user.profileImage, profileImagePath, progressBar)
    }

    fun insert(treasure: Treasure, progressBar: CircularProgressIndicator? = null) {
        val data = hashMapOf(
            "title" to treasure.title,
            "desc" to treasure.desc,
            "oid" to treasure.oid,
            "lat" to treasure.location.latitude.toString(),
            "long" to treasure.location.longitude.toString(),
            "wid" to treasure.wid,
            "seekers" to arrayListOf<String>(),
            "sr" to arrayListOf<String>(),
            "treasure_image_path" to ""
        )
        db.collection("treasures")
            .add(data)
            .addOnSuccessListener {
                val treasureImagePath = "images/treasures/${it.id}/image.jpg"
                it.update("treasure_image_path", treasureImagePath)
                insertToFirebaseStorage(treasure.treasureImage, treasureImagePath, progressBar)
            }
    }

    fun updateSeeker(tid: String, sid: String) {
        db.collection("treasures").document(tid).update("seekers", FieldValue.arrayUnion(sid))
    }

    fun updateSR(tid: String, sR: SR, progressBar: CircularProgressIndicator? = null) {
        db.collection("treasures").document(tid).update("sr", FieldValue.arrayUnion(sR.sid))
        val sRImagePath =  "images/treasures/${tid}/${sR.sid}.jpg"
        insertToFirebaseStorage(sR.sRImage, sRImagePath, progressBar)
    }

    fun updateProfileImage(uid: String, image: Bitmap, progressBar: CircularProgressIndicator? = null) {
        var profileImagePath = "images/profile/${uid}.jpg"
        insertToFirebaseStorage(image, profileImagePath, progressBar)

    }

    fun getProfileImage(activity: FragmentActivity, uid: String, mutableLiveData: MutableLiveData<Bitmap>) {
        var profileImagePath = "images/profile/${uid}.jpg"
        val reference = storageReference.child(profileImagePath)
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
            }
        }
    }

}