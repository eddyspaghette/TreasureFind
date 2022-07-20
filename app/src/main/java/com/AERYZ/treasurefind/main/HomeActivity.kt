package com.AERYZ.treasurefind.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.AERYZ.treasurefind.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val addBtn = findViewById<Button>(R.id.add_btn)
        val db = Firebase.firestore
        addBtn.setOnClickListener {

            val user = hashMapOf(
                "first" to "Ada",
                "last" to "Lovelace",
                "born" to 1815
            )

// Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("F", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("F", "Error adding document", e)
                }

        }

        val retrieveBtn = findViewById<Button>(R.id.retrieve_btn)
        retrieveBtn.setOnClickListener {
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    val arrList = ArrayList<String>()
                    for (document in result) {
                        Log.d("F", "${document.id} => ${document.data}")
                        arrList.add(msg)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("F", "Error getting documents.", exception)
                }
        }
    }
}