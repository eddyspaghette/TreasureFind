package com.AERYZ.treasurefind.main.ui.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.component3

class FeedViewModel : ViewModel() {
    private val storage = Firebase.storage
    var listImagesURI = MutableLiveData<List<StorageReference>>()

    companion object {
        private const val MAX_ITEMS = 5
    }

    init {
        listImagesURI.value = listOf()
        listAllPaginated(null)
    }

    private fun processResults(items: List<StorageReference>, prefixes: List<StorageReference>) {
        if (listImagesURI.value!!.isEmpty()) {
            listImagesURI.value = items
        } else {
            listImagesURI.value = listImagesURI.value!! + items
        }
        println("DEBUG $items")
    }

    private fun listAllPaginated(pageToken: String?) {
        val storage = Firebase.storage
        val listRef = storage.reference.child("images/")

        // Fetch the next page of results, using the pageToken if we have one.
        val listPageTask = if (pageToken != null) {
            listRef.list(MAX_ITEMS, pageToken)
        } else {
            listRef.list(MAX_ITEMS)
        }

        listPageTask
            .addOnSuccessListener { (items, prefixes, pageToken) ->
                // Process page of results
                processResults(items, prefixes)

                // Recurse onto next page
                pageToken?.let {
                    listAllPaginated(it)
                }
            }.addOnFailureListener {
                Log.d("DEBUG", "$it")
            }
    }

}