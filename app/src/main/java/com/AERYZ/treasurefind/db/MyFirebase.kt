package com.AERYZ.treasurefind.db

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Document(var name: String, var data: HashMap<String, String>) {
}

class User(var uid: String, var userName: String, var email: String, var image: Bitmap) {
    var profileImagePath = "images/profile/$uid.jpg"
}

class Treasure(var tid: String, var oid: String, var location: LatLng, var image: Bitmap, var wid: String = "") {
    var treasureImagePath = "images/treasures/$tid/image.jpg"
}

class MyFirebase {
    private var storage = Firebase.storage
    private var db = Firebase.firestore

    private fun insertData(document: Document, collection: String) {
        db.collection(collection).document(document.name)
            .set(document.data)
            .addOnSuccessListener { Log.d("Debugging insertion", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("Debugging insertion", "Error writing document", e) }
    }

    private fun insert(user: User) {
        val doc = Document(user.uid, hashMapOf(
            "username" to user.userName,
            "email" to user.email,
            "profile_image_path" to user.profileImagePath
        ))
        insertData(doc, "users")
    }

    private fun insert(treasure: Treasure) {
        val doc = Document(treasure.tid, hashMapOf(
            "oid" to treasure.oid,
            "lat" to treasure.location.latitude.toString(),
            "long" to treasure.location.longitude.toString(),
            "wid" to treasure.wid,
            "treasure_image_path" to treasure.treasureImagePath
        ))
        insertData(doc, "treasures")
    }
}