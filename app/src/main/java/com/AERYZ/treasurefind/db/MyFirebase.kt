package com.AERYZ.treasurefind.db

import android.app.Dialog
import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.feed.GlideApp
import com.google.android.gms.maps.model.LatLng
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
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask


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

    fun insert(user: User) {
        var profileImagePath = "images/profile/${user.uid}.jpg"
        val data = hashMapOf(
            "username" to user.userName,
            "email" to user.email,
            "profile_image_path" to profileImagePath
        )

        db.collection("users").document(user.uid).set(data)
        insertToFirebaseStorage(user.profileImage, profileImagePath)
    }

    fun insert(treasure: Treasure, dialog: Dialog? = null, successDialog: Dialog? = null) {
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
                insertToFirebaseStorage(treasure.treasureImage, treasureImagePath, dialog, successDialog)
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