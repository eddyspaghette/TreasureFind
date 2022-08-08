package com.AERYZ.treasurefind.db

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.AERYZ.treasurefind.R
import com.AERYZ.treasurefind.main.ui.feed.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
    var in_session: String = "",
    @Exclude @set:Exclude @get:Exclude var profileImage: Bitmap? = null,
    var profileImagePath: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var status: Int = 0,
    var score: Int = 0,
    var ownedList: ArrayList<String> = arrayListOf<String>(),
    var foundList: ArrayList<String> = arrayListOf<String>(),
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
    @Exclude @set:Exclude @get:Exclude var distance: Double? = 0.0,
    @Exclude @set:Exclude @get:Exclude var distanceText: String? = "",
)


data class SR(var tid:String = "",
              var sid:String = "",
              var latitude: Double = 0.0,
              var longitude: Double = 0.0,
              @Exclude @set:Exclude @get:Exclude var sRImage: Bitmap? = null) {
}

class MyFirebase {
    private var storage = Firebase.storage
    private var db = Firebase.firestore
    private var storageReference = storage.reference

    interface FirebaseFeedListener {
        fun onSuccess(snapshot: QuerySnapshot)
        fun onFailure(exception: Exception)
    }

    interface ImageInsertionListener {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    interface TreasureInsertionListener {
        fun onSuccess(tid: String)
        fun onFailure(exception: Exception)
    }
    interface UserInsertionListener {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    interface DeletionImageListener {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    interface RankInterfaceListener {
        fun onSuccess(size: Int)
        fun onFailure(exception: Exception)
    }

    interface ImageGetListener {
        fun onSuccess()
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

    private fun insertImageToFirebaseStorage(bitmap: Bitmap, path: String, listener: ImageInsertionListener? = null) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val reference = storageReference.child(path)
        val uploadTask = reference.putBytes(data)
        uploadTask.addOnFailureListener {
            listener?.onFailure(it)
        }.addOnSuccessListener {
            listener?.onSuccess()
        }
    }

    private fun insertToFirebaseStorage(bitmap: Bitmap, path: String, id: String? = null, dialog: Dialog? = null, successDialog: Dialog? = null, listener: TreasureInsertionListener?=null) {
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
                        if (listener != null && id != null) {
                            listener.onSuccess(id)
                        }
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

    // returns document reference, caller has to implement listeners
    fun getTreasureDocument(tid: String): DocumentReference {
        val docRef = db.collection("treasures").document(tid)
        return docRef
    }

    fun insert(myUser: MyUser, listener: UserInsertionListener?= null) {
        val profileImagePath = "images/profile/${myUser.uid}.jpg"
        myUser.profileImagePath = profileImagePath
        db.collection("users").document(myUser.uid).set(myUser)
            .addOnCompleteListener {
                Log.d("Debug", "Inserting user sucess!")
                listener?.onSuccess()
            }
            .addOnCanceledListener {
                Log.d("Debug", "Inserting user failed!")
            }
        insertToFirebaseStorage(myUser.profileImage!!, profileImagePath)
    }

    fun insert(treasure: Treasure, dialog: Dialog? = null, successDialog: Dialog? = null, listener: TreasureInsertionListener? = null) {
        db.collection("treasures")
            .add(treasure)
            .addOnSuccessListener {
                val treasureImagePath = "images/treasures/${it.id}/image.jpg"
                it.update("treasureImagePath", treasureImagePath)
                it.update("tid", it.id)
                addToOwnedList(treasure.oid!!, it.id)
                treasure.treasureImage?.let { it1 ->
                    insertToFirebaseStorage(it1, treasureImagePath, it.id, dialog, successDialog, listener)
                }
            }
    }

    fun getTreasure(tid: String, mutableLiveData: MutableLiveData<Treasure>) {
        val docRef = db.collection("treasures").document(tid)
        val source = Source.CACHE
        docRef.get(source).addOnCompleteListener() {
            if (it.isSuccessful) {
                mutableLiveData.value = it.result.toObject<Treasure>()
            }

        }.addOnFailureListener() {
            Log.d("Debug", "Failed to achieve data")
        }
    }

    fun getImage(activity: Activity, imagePath: String, imageMutableLiveData: MutableLiveData<Bitmap>, listener: ImageGetListener?=null) {
        val reference = storageReference.child(imagePath)
        imageMutableLiveData.value = BitmapFactory.decodeResource(activity.resources, R.drawable.tf_logo)
        CoroutineScope(IO).launch {
            val bitmap = GlideApp.with(activity)
                .asBitmap()
                .error(R.drawable.tf_logo)
                .load(reference)
                .submit()
                .get()
            imageMutableLiveData.postValue(bitmap)
            listener?.onSuccess()
        }
    }

    fun getProfileImage(activity: FragmentActivity, uid: String, imageMutableLiveData: MutableLiveData<Bitmap>, listener: ImageGetListener?=null) {
        var profileImagePath = "images/profile/${uid}.jpg"
        getImage(activity, profileImagePath, imageMutableLiveData, listener)
    }

    fun getTreasureImage(activity: Activity, tid: String, imageMutableLiveData: MutableLiveData<Bitmap>, listener: ImageGetListener?=null) {
        var treasureImagePath = "images/treasures/${tid}/image.jpg"
        getImage(activity, treasureImagePath, imageMutableLiveData, listener)
    }
    fun getSRImage(activity: Activity, tid: String, sid: String, imageMutableLiveData: MutableLiveData<Bitmap>) {
        var treasureImagePath = "images/treasures/${tid}/${sid}.jpg"
        getImage(activity, treasureImagePath, imageMutableLiveData)
    }

    fun updateUser(uid: String, field: String, value: Any?) {
        db.collection("users").document(uid).update(field, value)
    }

    fun updateTreasure(tid: String, field: String, value: String) {
        db.collection("treasures").document(tid).update(field, value)
    }

    fun addToFoundList(uid: String, tid: String) {
        db.collection("users").document(uid).update("foundList", FieldValue.arrayUnion(tid))
    }

    private fun addToOwnedList(uid: String, tid: String) {
        db.collection("users").document(uid).update("ownedList", FieldValue.arrayUnion(tid))
    }

    fun addSeeker(tid: String, sid: String) {
        db.collection("treasures").document(tid).update("seekers", FieldValue.arrayUnion(sid))
    }

    fun removeSeeker(tid: String, sid: String) {
        db.collection("treasures").document(tid).update("seekers", FieldValue.arrayRemove(sid))
    }

    fun addSR(resources: Resources, sR: SR) {
        val sRImagePath =  "images/treasures/${sR.tid}/${sR.sid}.jpg"
        if (sR.sRImage == null) {
            sR.sRImage = BitmapFactory.decodeResource(resources, R.drawable.tf_logo)
        }
        insertImageToFirebaseStorage(sR.sRImage!!, sRImagePath, object: ImageInsertionListener {
            override fun onFailure(exception: Exception) {
//                TODO("Not yet implemented")
            }
            override fun onSuccess() {
                db.collection("treasures").document(sR.tid).update("sr", FieldValue.arrayUnion(sR.sid))
                db.collection("submit_requests").document(sR.sid).set(sR)
            }
        })

    }
    fun removeSR(tid: String, sid: String, listener: DeletionImageListener?= null) {
        db.collection("treasures").document(tid).update("sr", FieldValue.arrayRemove(sid))

        db.collection("submit_requests").document(sid).delete()

        val sRImagePath =  "images/treasures/${tid}/${sid}.jpg"
        deleteImage(sRImagePath, listener)
    }

    fun updateProfileImage(uid: String, image: Bitmap) {
        var profileImagePath = "images/profile/${uid}.jpg"
        insertToFirebaseStorage(image, profileImagePath)
    }

    fun deleteImage(path: String, listener: DeletionImageListener? = null) {
        val deleteRef = storageReference.child(path)
        deleteRef.delete().addOnSuccessListener {
            listener.let{
                it?.onSuccess()
            }
        }.addOnFailureListener {
            // TODO: implement failure
        }
    }

    fun returnRank(uid: String, listener: RankInterfaceListener) {
        val userRef = getUserDocument(uid)
        userRef
            .get()
            .addOnSuccessListener {
                val myUser = it.toObject<MyUser>()
                val query = db.collection("users")
                    .whereGreaterThanOrEqualTo("score", myUser!!.score)
                    .orderBy("score", Query.Direction.DESCENDING)
                query
                    .get()
                    .addOnSuccessListener { snapshot ->
                        listener.onSuccess(snapshot.size())
                    }
            }
            .addOnFailureListener{
                listener.onFailure(it)
            }
    }


}