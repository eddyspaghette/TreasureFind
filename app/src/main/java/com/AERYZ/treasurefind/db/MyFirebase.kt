package com.AERYZ.treasurefind.db

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class User(var uid: String, var userName: String, var email: String, var profileImage: Bitmap) {
    var profileImagePath = "images/profile/$uid.jpeg"
}

class Treasure(var oid: String, var location: LatLng, var image: Bitmap, var wid: String = "") {
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



//    fun insert(user: User) {
//        val doc = Document(user.uid, hashMapOf(
//            "username" to user.userName,
//            "email" to user.email,
//            "profile_image_path" to user.profileImagePath
//        ))
//        insertData(doc, "users")
//        insertToFirebaseStorage(user.profileImage, user.profileImagePath)
//    }

    fun insert(treasure: Treasure, progressBar: CircularProgressIndicator? = null) {
        val data = hashMapOf(
            "oid" to treasure.oid,
            "lat" to treasure.location.latitude.toString(),
            "long" to treasure.location.longitude.toString(),
            "wid" to treasure.wid,
            "treasure_image_path" to ""
        )
        db.collection("treasures")
            .add(data)
            .addOnSuccessListener {
                val treasureImagePath = "images/treasures/${it.id}/image.jpg"
                it.update("treasure_image_path", treasureImagePath)
                insertToFirebaseStorage(treasure.image, treasureImagePath, progressBar)
            }
    }
}